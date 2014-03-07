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

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.INotifyChangedListener;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.rmf.reqif10.ReqIF;
import org.eclipse.rmf.reqif10.ReqIF10Factory;
import org.eclipse.rmf.reqif10.ReqIF10Package;
import org.eclipse.rmf.reqif10.ReqIFContent;
import org.eclipse.rmf.reqif10.ReqIFHeader;
import org.eclipse.rmf.reqif10.Specification;
import org.eclipse.rmf.reqif10.pror.provider.ReqIFContentItemProvider;
import org.eclipse.rmf.reqif10.pror.provider.VirtualSpecificationsItemProvider;
import org.eclipse.rmf.reqif10.pror.util.ProrUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.ide.IDE;

/**
 * 
 * TODO Undoing typing doesn't work yet!
 * 
 * @author jastram
 * 
 */
public class ReqifMainForm {

	private final FormToolkit toolkit;

	private final ScrolledForm form;

	private final ReqIF reqif;

	private final Reqif10Editor reqifEditor;

	private ComposedAdapterFactory getAdapterFactory() {
		return (ComposedAdapterFactory) reqifEditor.getAdapterFactory();
	}

	public ReqifMainForm(Composite parent, Reqif10Editor rifEditor) {

		this.reqifEditor = rifEditor;
		this.reqif = rifEditor.getReqif();

		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		form.setText(getString("_UI_ReqifForm_ReqIfFile") + ": " + URI.decode(reqif.eResource().getURI().lastSegment()));
		TableWrapLayout layout = new TableWrapLayout();
		form.getBody().setLayout(layout);

		Section helpSection = createSection(0);

		helpSection.setText(getString("_UI_Help_Get_Started_Title"));
		Text helpSectionClient = toolkit.createText(helpSection,
				getString("_UI_Help_Get_Started"), SWT.MULTI | SWT.WRAP
						| SWT.READ_ONLY);
		helpSection.setClient(helpSectionClient);

		createDocSection();
		createSpecSection();

		form.reflow(true);
	}

	private void createSpecSection() {

		final Section specSection = createSection(Section.EXPANDED
				| Section.DESCRIPTION);
		specSection.setText(getString("_UI_ReqifForm_Specifications"));
		specSection.setDescription(getString("_UI_ReqifForm_DblClickSpecifications"));

		Composite client = toolkit.createComposite(specSection);
		final ListViewer list = new ListViewer(client, SWT.SINGLE | SWT.BORDER){
			
			@Override
			public void update(Object element, String[] properties) {
				super.update(element, properties);
				specSection.layout();
			}
			
			@Override
			public void update(Object[] elements, String[] properties) {
				super.update(elements, properties);
				specSection.layout();
			}
			
		};		
		
		list.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (!event.getSelection().isEmpty()) {
					reqifEditor.setSelection(event.getSelection());
				}
				//specSection.layout();
			}
		});

		TableWrapLayout layout = new TableWrapLayout();
		client.setLayout(layout);
		specSection.setClient(client);

		// Because we established a virtual hierarchy, we have to use the
		// ItemProvider-Facility to get the root element holding
		// Specifications
		ReqIFContent coreContent = reqif.getCoreContent();
		if (coreContent == null) {
			coreContent = ReqIF10Factory.eINSTANCE.createReqIFContent();
			reqifEditor
					.getEditingDomain()
					.getCommandStack()
					.execute(
							SetCommand.create(
									reqifEditor.getEditingDomain(),
									reqif,
									ReqIF10Package.Literals.REQ_IF__CORE_CONTENT,
									coreContent));
		}
		ReqIFContentItemProvider ip = (ReqIFContentItemProvider) ProrUtil
				.getItemProvider(getAdapterFactory(), coreContent);
		ip.getChildren(coreContent);
		VirtualSpecificationsItemProvider root = (VirtualSpecificationsItemProvider) ip
				.getVirtualSpecifications(coreContent);
		
		// FIXME ???
//		list.setContentProvider(new ProRAdapterFactoryContentProvider(
//				adapterFactory));
		list.setContentProvider(new AdapterFactoryContentProvider(getAdapterFactory()));
		list.setLabelProvider(new AdapterFactoryLabelProvider(getAdapterFactory()));
		list.setInput(root);

		root.addListener(new INotifyChangedListener() {
			public void notifyChanged(Notification notification) {
				if (list != null && !(list.getControl().isDisposed())) {
					list.refresh();
					form.reflow(true);
				}
			}
		});

		list.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				Object element = ((IStructuredSelection) event.getSelection())
						.getFirstElement();
				if (element instanceof Specification) {
					openSpec((Specification) element);
				}
			}
		});
	}

	private void createDocSection() {
		final ReqIFHeader header;
		if (reqif.getTheHeader() == null) {
			header = ReqIF10Factory.eINSTANCE.createReqIFHeader();
			Command cmd = SetCommand.create(reqifEditor.getEditingDomain(), reqif,
					ReqIF10Package.Literals.REQ_IF__THE_HEADER, header);
			reqifEditor.getEditingDomain().getCommandStack().execute(cmd);
		} else {
			header = reqif.getTheHeader();
		}

		final Section docSection = createSection(0);
		docSection.setText(getString("_UI_ReqifForm_DocumentProperties"));
		docSection.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		Composite client = toolkit.createComposite(docSection);
		docSection.setClient(client);
		TableWrapLayout layout = new TableWrapLayout();
		client.setLayout(layout);

		toolkit.createLabel(client, getString("_UI_ReqifForm_Title")+": ");
		Text text = toolkit.createText(client, header.getTitle(), SWT.BORDER);
		text.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		addModifyListener(text, header,
				ReqIF10Package.Literals.REQ_IF_HEADER__TITLE);

		toolkit.createLabel(client, getString("_UI_ReqifForm_Comment") + ": ");
		text = toolkit.createText(client, header.getComment(), SWT.BORDER
				| SWT.MULTI | SWT.WRAP);
		text.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		// Necessary to always have the proper vertical size.
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				form.reflow(false);
			}
		});
		addModifyListener(text, header,
				ReqIF10Package.Literals.REQ_IF_HEADER__COMMENT);

		toolkit.createLabel(client, getString("_UI_ReqifForm_CreateTime")+": ");
		text = toolkit.createText(client, header.getCreationTime() + "",
				SWT.BORDER);
		text.setEnabled(false);
		text.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		toolkit.createLabel(client, getString("_UI_ReqifForm_SourceToolId") + ": ");
		text = toolkit.createText(client, header.getSourceToolId(), SWT.BORDER);
		text.setEnabled(false);
		text.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		toolkit.createLabel(client, getString("_UI_ReqifForm_ReqIfToolId") + ": ");
		text = toolkit.createText(client, header.getReqIFToolId(), SWT.BORDER);
		text.setEnabled(false);
		text.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		toolkit.createLabel(client, getString("_UI_ReqifForm_ReqIfVersion") + ": ");
		text = toolkit.createText(client, header.getReqIFVersion(), SWT.BORDER);
		text.setEnabled(false);
		text.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		toolkit.createLabel(client, getString("_UI_ReqifForm_Identifier") + ": ");
		text = toolkit.createText(client, header.getIdentifier(), SWT.BORDER);
		text.setEnabled(false);
		text.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		toolkit.createLabel(client, getString("_UI_ReqifForm_RepositoryId") + ": ");
		text = toolkit.createText(client, header.getRepositoryId(), SWT.BORDER);
		text.setEnabled(false);
		text.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
	}

	/**
	 * Adds a ModifyListener to the given {@link Text} to update the model
	 * whenever the text changes.
	 * 
	 * @param text
	 *            The control to listen to.
	 * @param feature
	 *            The Feature from {@link ExchangeFilePackage} to be modified
	 *            (must belong to {@link RIFHeader}.
	 */
	private void addModifyListener(final Text text, final ReqIFHeader header,
			final EAttribute feature) {

		// When the Text is modified, update the model...
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				Command cmd = SetCommand.create(reqifEditor.getEditingDomain(), reqif.getTheHeader(),
						feature, text.getText());
				reqifEditor.getEditingDomain().getCommandStack().execute(cmd);
			}
		});

	}

	private Section createSection(int flags) {
		Section section = toolkit.createSection(form.getBody(),
				Section.CLIENT_INDENT | Section.TITLE_BAR | Section.TWISTIE
						| flags);
		section.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		return section;
	}

	public ScrolledForm getForm() {
		return form;
	}

	private void openSpec(Specification spec) {
		try {
			IWorkbenchPage page = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();
			ReqifSpecificationEditorInput editorInput = new ReqifSpecificationEditorInput(
					reqifEditor, spec);
			IDE.openEditor(page, editorInput, SpecificationEditor.EDITOR_ID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	private String getString(String key) {
		return Reqif10EditorPlugin.INSTANCE.getString(key);
	}

}
