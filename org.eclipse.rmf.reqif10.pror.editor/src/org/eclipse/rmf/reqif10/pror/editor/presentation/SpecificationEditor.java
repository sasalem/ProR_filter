/*******************************************************************************
 * Copyright (c) 2011 Formal Mind GmbH and University of Dusseldorf.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Michael Jastram - initial API and implementation
 ******************************************************************************/
package org.eclipse.rmf.reqif10.pror.editor.presentation;

import java.util.Collection;
import java.util.EventObject;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.common.command.CommandStackListener;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.emf.edit.ui.provider.UnwrappingSelectionProvider;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.rmf.reqif10.ReqIF10Package;
import org.eclipse.rmf.reqif10.SpecHierarchy;
import org.eclipse.rmf.reqif10.SpecRelation;
import org.eclipse.rmf.reqif10.Specification;
import org.eclipse.rmf.reqif10.pror.editor.actions.SpecificationWebPrintAction;
import org.eclipse.rmf.reqif10.pror.editor.agilegrid.ProrAgileGrid;
import org.eclipse.rmf.reqif10.pror.editor.agilegrid.ProrAgileGridViewer;
import org.eclipse.rmf.reqif10.pror.util.ProrUtil;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;

/**
 * @author Lukas Ladenberger
 * @author Michael Jastram
 */
public class SpecificationEditor extends EditorPart implements
		IEditingDomainProvider, IMenuListener, ISelectionProvider {

	public static final String EDITOR_ID = "org.eclipse.rmf.reqif10.pror.SpecificationEditor";

	/**
	 * The {@link Specification} associated with this Editor.
	 */
	private Specification specification;

	/**
	 * The {@link ProrAgileGridViewer} for this Editor.
	 */
	private ProrAgileGridViewer prorAgileGridViewer;

	private Reqif10ActionBarContributor reqifActionBarContributor;
	
	/**
	 * The {@link Reqif10Editor} of the owning {@link ReqIf} object. We keep a
	 * reference, as we reuse a number of elements from that editor (Property
	 * View, Outline View, EditingDomain). We keep the save status of the
	 * Editors synchronized via the EditingDomain's Command Stack.
	 */
	private Reqif10Editor reqifEditor;

	// A number of Listeners
	private ISelectionChangedListener selectionChangedListener;
	private CommandStackListener commandStackListener;
	private AdapterImpl changeNameListener;
	private AdapterImpl deleteSpecListener;

	private ISelectionListener iSelectionListener;

	/**
	 * Initializes the Editor.
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		// Sanity Check
		if (!(input instanceof ReqifSpecificationEditorInput)) {
			throw new IllegalArgumentException("Wrong input type: " + input);
		}

		// Extracting Info from the input
		reqifEditor = ((ReqifSpecificationEditorInput)input).getReqifEditor();
		specification = ((ReqifSpecificationEditorInput)input).getSpec();
		
		reqifActionBarContributor = (Reqif10ActionBarContributor) site.getActionBarContributor();

		// Part Setup
		setSite(site);
		setInputWithNotify(input);
		setPartName(input.getName());
		site.setSelectionProvider(this);
		
		site.getActionBars().setGlobalActionHandler(
				ActionFactory.PRINT.getId(),
				new SpecificationWebPrintAction(reqifEditor.getEditingDomain(),
						reqifEditor.getAdapterFactory()));
	}

	/**
	 * Builds the Part, which is an {@link ProrAgileGridViewer} and registers a
	 * number of Listeners.
	 */
	@Override
	public void createPartControl(final Composite parent) {
		createSpecificationPart(parent);		

		// Order matters!
		registerChangeNameListener();
		registerDeleteListener();
		registerSelectionChangedListener();
		registerCommandStackListener(parent);
	}

	/**
	 * Builds the actual {@link ProrAgileGridViewer}
	 * @param containter
	 */
	private void createSpecificationPart(Composite containter) {
		prorAgileGridViewer = new ProrAgileGridViewer(containter,
				reqifEditor.getAdapterFactory(), getEditingDomain(),
				reqifActionBarContributor.getAgileCellEditorActionHandler());
		prorAgileGridViewer.setInput(specification);
		buildContextMenu();
	}

	/**
	 * Registers a command stack listener that updates the save state and
	 * updates the selection.
	 */
	private void registerCommandStackListener(final Composite parent) {
		commandStackListener = new CommandStackListener() {
			public void commandStackChanged(final EventObject event) {
				parent.getDisplay().asyncExec(new Runnable() {
					public void run() {
						firePropertyChange(IEditorPart.PROP_DIRTY);
						// Try to select the affected objects.
						Command mostRecentCommand = ((CommandStack) event
								.getSource()).getMostRecentCommand();
						if (mostRecentCommand != null) {
							Collection<?> affectedObjects = mostRecentCommand
									.getAffectedObjects();
							setSelectionToViewer(affectedObjects);
						}
					}
				});
			}
		};
		getEditingDomain().getCommandStack().addCommandStackListener(
				commandStackListener);
	}

	/**
	 * Upon a change of the name of the Specification, the Part must be renamed.
	 */
	private void registerChangeNameListener() {
		changeNameListener = new AdapterImpl() {
			@Override
			public void notifyChanged(Notification notification) {
				if (notification.getFeature() == ReqIF10Package.Literals.SPEC_ELEMENT_WITH_ATTRIBUTES__VALUES) {
					ItemProviderAdapter ip = ProrUtil.getItemProvider(reqifEditor.getAdapterFactory(), specification);
					setPartName(ip.getText(specification));
				}
			}
		};
		specification.eAdapters().add(changeNameListener);
	}

	/**
	 * If the Specification is deleted, we must close the editor.
	 */
	private void registerDeleteListener() {
		final EObject container = specification.eContainer();
		deleteSpecListener = new AdapterImpl() {
			@Override
			public void notifyChanged(Notification msg) {
				if (msg.getFeature() == ReqIF10Package.Literals.SPECIFICATION__CHILDREN
						|| msg.getEventType() == Notification.REMOVE
						&& msg.getOldValue() == specification) {

					getEditorSite().getPage().closeEditor(
							SpecificationEditor.this, false);
				}
			}
		};
		container.eAdapters().add(deleteSpecListener);
	}

	/**
	 * This sets the selection into whichever viewer is active. Code taken from
	 * the generated {@link Reqif10Editor#setSelectionToViewer(Collection)}
	 */
	private void setSelectionToViewer(Collection<?> collection) {
		final Collection<?> theSelection = collection;
		// Make sure it's okay.
		//
		if (theSelection != null && !theSelection.isEmpty()) {
			Runnable runnable =
				new Runnable() {
					public void run() {

						// Try to select the items in the current content viewer of the editor.
						//
						if (prorAgileGridViewer != null) {
							prorAgileGridViewer.setSelection(new StructuredSelection(theSelection.toArray()), true);
						}

					}
				};
			getSite().getShell().getDisplay().syncExec(runnable);
		}
	}

	private void registerSelectionChangedListener() {
		selectionChangedListener = new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if(event.getSource() != prorAgileGridViewer){
				SpecificationEditor.this.setSelection(event.getSelection());
			}}
		};
		prorAgileGridViewer.addSelectionChangedListener(selectionChangedListener);
		iSelectionListener = new ISelectionListener() {

			public void selectionChanged(IWorkbenchPart part,
					ISelection selection) {
				// Only apply selection if it contains at least one SpecHierarchy
				if (selection instanceof IStructuredSelection) {
					
					for (Iterator<?> i = ((IStructuredSelection)selection).iterator(); i.hasNext();) {
						Object item = i.next();
						if (item instanceof SpecHierarchy) {
							SpecificationEditor.this.setSelection(selection);	
							((ProrAgileGrid)SpecificationEditor.this.prorAgileGridViewer.getControl()).scrollToFocus();
							return;
						}
					}
				}
			}
			
		};
		getSite().getPage().addSelectionListener(iSelectionListener);
	}

	/**
	 * Delegate populating the context menu to EMF.
	 */
	private MenuManager buildContextMenu() {
		MenuManager contextMenu = new MenuManager("#PopUp");
		contextMenu.add(new Separator("additions"));
		contextMenu.setRemoveAllWhenShown(true);
		contextMenu.addMenuListener(this);

		Menu menu = contextMenu.createContextMenu(prorAgileGridViewer
				.getControl());
		prorAgileGridViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(contextMenu,
				new UnwrappingSelectionProvider(prorAgileGridViewer));

		return contextMenu;
	}

	/**
	 * We use the outline and property view from {@link Reqif10Editor}.
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class key) {
		if (key.equals(IContentOutlinePage.class)) {
			return reqifEditor.getContentOutlinePage();
		} else if (key.equals(IPropertySheetPage.class)) {
			return reqifEditor.getPropertySheetPage();
		} else {
			return super.getAdapter(key);
		}
	}

	/**
	 * Delegate selection management to {@link #prorAgileGridViewer}.
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		prorAgileGridViewer.addSelectionChangedListener(listener);
	}

	/**
	 * Delegate selection management to {@link #prorAgileGridViewer}.
	 */
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		prorAgileGridViewer.removeSelectionChangedListener(listener);
	}

	/**
	 * Delegate selection management to {@link #prorAgileGridViewer}.
	 */
	public ISelection getSelection() {
		return prorAgileGridViewer.getSelection();
	}

	/**
	 * Delegate selection management to {@link #prorAgileGridViewer}.
	 */
	public void setSelection(ISelection selection) {
		prorAgileGridViewer.setSelection(selection);
		reqifEditor.setStatusLineManager(selection);
}

	/**
	 * This implements {@link org.eclipse.jface.action.IMenuListener} to help
	 * fill the context menus with contributions from the Edit menu.
	 */
	public void menuAboutToShow(IMenuManager menuManager) {
		((IMenuListener)getEditorSite().getActionBarContributor()).menuAboutToShow(menuManager);
	}

	/**
	 * The {@link EditingDomain} from the {@link Reqif10Editor}.
	 */
	public EditingDomain getEditingDomain() {
		return reqifEditor.getEditingDomain();
	}

	/**
	 * Synchronized with {@link Reqif10Editor}
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		reqifEditor.doSave(monitor);
		firePropertyChange(IEditorPart.PROP_DIRTY);
	}

	/**
	 * Synchronized with {@link Reqif10Editor}
	 */
	@Override
	public void doSaveAs() {
		reqifEditor.doSaveAs();
		firePropertyChange(IEditorPart.PROP_DIRTY);
	}

	/**
	 * Synchronized with the {@link EditingDomain} from the {@link Reqif10Editor}
	 */
	@Override
	public boolean isDirty() {
		return ((BasicCommandStack)getEditingDomain().getCommandStack()).isSaveNeeded();
	}

	/**
	 * @return true
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * Delegated to {@link #prorAgileGridViewer}.
	 */
	@Override
	public void setFocus() {
		prorAgileGridViewer.getControl().setFocus();
	}

	/**
	 * Detach all listeners.
	 */
	@Override
	public void dispose() {
		prorAgileGridViewer.dispose();

		if (selectionChangedListener != null) {
			prorAgileGridViewer.removeSelectionChangedListener(selectionChangedListener);
			selectionChangedListener = null;
		}
		
		if (iSelectionListener != null) {
			getSite().getPage().removeSelectionListener(iSelectionListener);
			iSelectionListener = null;
		}
		if (commandStackListener != null) {
			getEditingDomain().getCommandStack().removeCommandStackListener(commandStackListener);
			commandStackListener = null;
		}
		if (changeNameListener != null) {
			specification.eAdapters().remove(changeNameListener);
			changeNameListener = null;
		}
		if (deleteSpecListener != null) {
			specification.eAdapters().remove(deleteSpecListener);
			deleteSpecListener = null;
		}

		if (reqifEditor.getActionBarContributor().getActiveEditor() == this) {
			reqifEditor.getActionBarContributor().setActiveEditor(null);
		}
	
		super.dispose();
	}

	// TODO I would like to remove this.
	public Reqif10Editor getReqifEditor() {
		return reqifEditor;
	}

	/**
	 * Forward requests to show or hide SpecRelations.
	 */
	public void setShowSpecRelations(boolean checked) {

		ISelection sel = prorAgileGridViewer.getSelection();

		prorAgileGridViewer.setShowSpecRelations(checked);

		// Set the correct selection after showing/hiding SpecRelations
		if (sel instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) sel;
			Object firstElement = selection.getFirstElement();
			// If a SpecRelation was selected, select after hiding the
			// SpecRealtions the first SpecHierarchy of the Specification
			if (firstElement instanceof SpecRelation) {
				selection = new StructuredSelection(specification.getChildren()
						.get(0));
			}
			prorAgileGridViewer.setSelection(selection);
		}

	}

	/**
	 * Only the "Main" Editor ({@link Reqif10Editor}) requires saving.
	 */
	@Override
	public boolean isSaveOnCloseNeeded() {
		return false;
	}

	public Reqif10ActionBarContributor getReqifActionBarContributor() {
		return reqifActionBarContributor;
	}

	public AdapterFactory getAdapterFactory() {
		return reqifEditor.getAdapterFactory();
	}

}
