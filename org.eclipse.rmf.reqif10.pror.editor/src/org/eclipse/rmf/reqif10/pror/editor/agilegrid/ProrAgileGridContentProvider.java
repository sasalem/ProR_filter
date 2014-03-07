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
package org.eclipse.rmf.reqif10.pror.editor.agilegrid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.agilemore.agilegrid.AbstractContentProvider;
import org.agilemore.agilegrid.AgileGrid;
import org.agilemore.agilegrid.IContentProvider;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.rmf.reqif10.AttributeValue;
import org.eclipse.rmf.reqif10.Identifiable;
import org.eclipse.rmf.reqif10.ReqIF;
import org.eclipse.rmf.reqif10.SpecElementWithAttributes;
import org.eclipse.rmf.reqif10.SpecHierarchy;
import org.eclipse.rmf.reqif10.SpecObject;
import org.eclipse.rmf.reqif10.SpecRelation;
import org.eclipse.rmf.reqif10.Specification;
import org.eclipse.rmf.reqif10.common.util.ReqIF10Util;
import org.eclipse.rmf.reqif10.pror.configuration.ProrSpecViewConfiguration;
import org.eclipse.rmf.reqif10.pror.editor.agilegrid.ProrRow.ProrRowSpecHierarchy;
import org.eclipse.rmf.reqif10.pror.editor.agilegrid.ProrRow.ProrRowSpecRelation;

/**
 * This ContentProvider manages a {@link Specification}, to be displayed in an
 * {@link AgileGrid}.
 */
public class ProrAgileGridContentProvider extends AbstractContentProvider {

	private final Specification root;
	private final ProrSpecViewConfiguration specViewConfig;
	private ArrayList<ProrRow> cache = null;
	private Map<Identifiable, ProrRow> rowMap = new HashMap<Identifiable, ProrRow>();

	private boolean showSpecRelations;

	public ProrAgileGridContentProvider(Specification specification,
			ProrSpecViewConfiguration specViewConfig) {
		this.root = specification;
		this.specViewConfig = specViewConfig;

		// TODO We want to be more nuanced.
		specification.eAdapters().add(new EContentAdapter() {
			@Override
			public void notifyChanged(Notification notification) {
				super.notifyChanged(notification);
				if (notification.getEventType() == Notification.ADD
						|| notification.getEventType() == Notification.ADD_MANY
						|| notification.getEventType() == Notification.MOVE
						|| notification.getEventType() == Notification.REMOVE
						|| notification.getEventType() == Notification.REMOVE_MANY
						|| notification.getEventType() == Notification.SET
						|| notification.getEventType() == Notification.UNSET) {
					flushCache();
				}
			}
		});
	}

	/**
	 * Returns the {@link AttributeValue} for the given column for the element
	 * associated with the row. May return null.
	 */
	@Override
	public Object doGetContentAt(int row, int col)
			throws IndexOutOfBoundsException {
		if (row >= getCache().size()) {
			throw new IndexOutOfBoundsException("Row does not exist: " + row);
		}

		SpecElementWithAttributes element = getCache().get(row).getSpecElement();

		if (col == specViewConfig.getColumns().size()) {
			// For the Link column, we return the linked element.
			return element instanceof SpecElementWithAttributes ? element
					: null;
		} else if (col <= specViewConfig.getColumns().size()) {
			// we return the AttributeValue.
			return getValueForColumn(element, row, col);
		} else {
			throw new IndexOutOfBoundsException("Column does not exist: " + col);
		}
	}

	/**
	 * Changes the Value through the editing domain if it has changed.
	 * <p>
	 * 
	 * We don't need to change anything here, as changing the
	 * {@link AttributeValue} automagically updates the model.
	 */
	@Override
	public void doSetContentAt(int row, int col, Object newValue) {
	}

	/**
	 * Whether to show {@link SpecRelation}s as part of the Content.
	 * 
	 * @param status
	 */
	public void setShowSpecRelations(boolean status) {
		this.showSpecRelations = status;
		for (ProrRow row : getCache()) {
			if (row instanceof ProrRowSpecHierarchy)
				((ProrRowSpecHierarchy) row).setShowSpecRelation(status);
		}
		flushCache();
	}

	/**
	 * Whether to show {@link SpecRelation}s as part of the Content.
	 */
	public boolean getShowSpecRelations() {
		return this.showSpecRelations;
	}

	/**
	 * Finds the Object for the given row, which may be a SpecHierarchy or
	 * SpecRelation.
	 */
	ProrRow getProrRow(int row) {
		return getCache().get(row);
	}

	public void flushCache(){
		cache = null;
	}
	
	/**
	 * Uses a Job to provider feedback to the user.
	 * 
	 * @return
	 */
	private ArrayList<ProrRow> getCache() {
		if (cache == null) {
			ArrayList<ProrRow> tmpCache = new ArrayList<ProrRow>();
			recurseSpecHierarchyForRow(0, 0, root.getChildren(), tmpCache);
			cache = tmpCache;
		}
		return cache;
	}

	private ProrRow getProrRowForSpecElement(Identifiable e, int row, int level) {
		ProrRow prorRow = rowMap.get(e);
		if (prorRow == null) {
			prorRow = ProrRow.createProrRow(e, row, level);
			rowMap.put(e, prorRow);
		} else {
			prorRow.setLevel(row);
			prorRow.setLevel(level);
		}
		return prorRow;
	}
	
	/**
	 * 
	 * @param current
	 *            The current counter
	 * @param elements
	 *            The {@link SpecHierarchy}s to traverse, Can be SpecHierarchies
	 *            or SpecRelations
	 * @param tmpCache 
	 * @return either the {@link ProrRow} with the given row, or the new current
	 *         row
	 */
	private int recurseSpecHierarchyForRow(int current, int depth,
			List<SpecHierarchy> elements, ArrayList<ProrRow> tmpCache) {
		for (SpecHierarchy element : elements) {
			ProrRowSpecHierarchy prorRowSH = (ProrRowSpecHierarchy) getProrRowForSpecElement(
					element, current, depth);
			tmpCache.add(current, prorRowSH);
			if (prorRowSH.isShowSpecRelation()) {
				for (SpecRelation specRelation : getSpecRelationsFor(element)) {
					++current;
					ProrRowSpecRelation prorRowSR = (ProrRowSpecRelation) getProrRowForSpecElement(
							specRelation, current, depth + 1);
					tmpCache.add(current, prorRowSR);
				}
			}
			int result = recurseSpecHierarchyForRow(++current, depth + 1,
					element.getChildren(), tmpCache);
			current = result;
		}
		return current;
	}

	/**
	 * Returns the actual {@link AttributeValue} for the given Column and the
	 * given {@link SpecElementWithUserDefinedAttributes}
	 */
	AttributeValue getValueForColumn(SpecElementWithAttributes element,
			int row, int col) {
		// Knock-out criteria
		if (element == null)
			return null;
		if (col >= specViewConfig.getColumns().size())
			return null;

		String label = specViewConfig.getColumns().get(col).getLabel();

		return ReqIF10Util.getAttributeValueForLabel(element,
				label);
	}

	/**
	 * Returns the SpecRelations that use the given SpecObject (via the given
	 * SpecHierarchy) as a source. This method checks {@link #showSpecRelations}
	 * and returns immediately if it is false.
	 */
	private List<SpecRelation> getSpecRelationsFor(SpecHierarchy specHierarchy) {
		if (specHierarchy.getObject() == null)
			return Collections.emptyList();
		SpecObject source = specHierarchy.getObject();
		ReqIF reqif = ReqIF10Util.getReqIF(source);
		// Can happen if source is detached from the reqif model (e.g. just
		// being deleted)
		if (reqif == null)
			return Collections.emptyList();
		List<SpecRelation> list = new ArrayList<SpecRelation>();
		for (SpecRelation relation : reqif.getCoreContent().getSpecRelations()) {
			if (source.equals(relation.getSource())) {
				list.add(relation);
			}
		}
		return list;
	}

	void updateElement(SpecElementWithAttributes element) {
		recurseUpdateElement(0, element, root.getChildren());
		flushCache();
	}

	/**
	 * Recurses over all SpecHierarchies and updates wherever it finds the given
	 * specObject. As a specObject can appear multiple time, we have to cover
	 * the whole tree.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private int recurseUpdateElement(int row,
			SpecElementWithAttributes element, List list) {
		for (Object entry : list) {
			List children = new ArrayList();
			boolean refresh = false;
			if (element instanceof SpecRelation && element.equals(entry)) {
				refresh = true;
			} else if (entry instanceof SpecHierarchy) {
				SpecHierarchy specHierarchy = (SpecHierarchy) entry;
				children.addAll(specHierarchy.getChildren());
				
				ProrRow prorRow = rowMap.get(specHierarchy);
				if (prorRow != null && prorRow instanceof ProrRowSpecHierarchy) {
					if (((ProrRowSpecHierarchy) prorRow).isShowSpecRelation())
						children.addAll(getSpecRelationsFor(specHierarchy));
				}
				
				if (element.equals(specHierarchy.getObject())) {
					refresh = true;
				}
			}
			// Workaround: provide null for "old value" to force a recognition
			// of
			// the change.
			if (refresh) {
				firePropertyChange(IContentProvider.Content, row, 0, null,
						entry);
			}
			row++;
			row = recurseUpdateElement(row, element, children);
		}
		return row;
	}

	public int getRowCount() {
		return getCache().size();
	}
	
}
