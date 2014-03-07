/*******************************************************************************
 * Copyright (c) 2011 itemis GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nirmal Sasidharan - initial API and implementation
 ******************************************************************************/

package org.eclipse.rmf.rif12.resource;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.BasicExtendedMetaData;
import org.eclipse.emf.ecore.xmi.XMLResource;

import org.eclipse.rmf.rif12.ExchangeFile.util.ExchangeFileResourceImpl;
import org.eclipse.rmf.rif12.resource.converters.Rif2XMLConverter;
import org.eclipse.rmf.rif12.resource.converters.XML2RifConverter;
import org.eclipse.rmf.rif12.xsd.DocumentRoot;
import org.eclipse.rmf.rif12.xsd.RIF;
import org.eclipse.rmf.rif12.xsd.RifFactory;
import org.eclipse.rmf.rif12.xsd.RifPackage;
import org.eclipse.rmf.rif12.xsd.util.RifResourceFactoryImpl;

/**
 * Custom Resource implementation for RIF 1.2.
 * 
 * Loads RIF XML into RIF Ecore model. The resource implementation loads the RIF
 * file using the XML resource loader of RIF and does a model to model
 * transformation to convert it to the RIF Ecore model. Serialization does the
 * reverse.
 * 
 * It has been done this way as the RIF metamodel has slight differences to its
 * persistant XML format and customizing the XML loader and serializer of EMF is
 * not all that easy. Also the RIF model has references to other schemas like
 * XHTML (as well as custom schemas for Tool extensions). This information needs
 * to be loaded and retained in the RIF model.
 * 
 * 
 * @author Nirmal Sasidharan, itemis
 * 
 */
public class Rif12ResourceImpl extends ExchangeFileResourceImpl {

	public static final String RIF_URI = "http://automotive-his.de/200807/rif";
	public static final String RIF_SCHEMA_URI = "http://automotive-his.de/200807/rif";
	public static final String RIF_SCHEMA_LOCATION = "rif.xsd";
	public static final String RIF_XHTML_SCHEMA_URI = "http://automotive-his.de/200706/rif-xhtml";
	public static final String RIF_XHTML_SCHEMA_LOCATION = "rif-xhtml.xsd";
	
	private DocumentRoot documentRoot = null;

	public Rif12ResourceImpl(URI uri) {
		super(uri);
	}

	@Override
	public void load(Map<?, ?> options) throws IOException {

		if (!isLoaded) {

			Notification notification = setLoaded(true);
			isLoading = true;

			if (errors != null) {
				errors.clear();
			}

			if (warnings != null) {
				warnings.clear();
			}

			try {

				if (options == null)
					options = Collections.EMPTY_MAP;

				// No new XML loader is created, but the resource is loaded
				// using RIF XML Resource
				XMLResource rifXMLResource = loadRifXMLResource(options);

				if (!rifXMLResource.getContents().isEmpty()) {
					setDocumentRoot((DocumentRoot) rifXMLResource.getContents()
							.get(0));
					Collection<? extends EObject> rifEcoreContents = convertRifXML2RifEcore(((EObject) getDocumentRoot())
							.eContents());
					if (!rifEcoreContents.isEmpty())
						this.getContents().add(
								(EObject) rifEcoreContents.toArray()[0]);
				}

			} finally {

				isLoading = false;

				if (notification != null) {
					eNotify(notification);
				}

				setModified(false);

			}
		}
	}

	protected XMLResource loadRifXMLResource(Map<?, ?> options) {

		ResourceSet rifXMLResourceSet = new ResourceSetImpl();

		// Register RIF XML model
		rifXMLResourceSet.getResourceFactoryRegistry()
				.getExtensionToFactoryMap()
				.put("rif", new RifResourceFactoryImpl());

		rifXMLResourceSet.getLoadOptions().putAll(options);

/*		rifXMLResourceSet.getLoadOptions().put(
				XMLResource.OPTION_SUPPRESS_DOCUMENT_ROOT, Boolean.TRUE);*/

		rifXMLResourceSet.getLoadOptions().put(OPTION_EXTENDED_META_DATA,
				new BasicExtendedMetaData() {
					@Override
					public EPackage getPackage(String namespace) {
						// Change references to RIF namespace to RIF XML
						// namespace
						if (namespace != null && namespace.equals(RIF_URI))
							return RifPackage.eINSTANCE;
						else
							return super.getPackage(namespace);
					}

				});

		XMLResource rifXMLResource = (XMLResource) rifXMLResourceSet
				.getResource(uri, true);

		// TODO: Check if more attributes needs to be set.
		setEncoding(rifXMLResource.getEncoding());
		setTimeStamp(rifXMLResource.getTimeStamp());

		return rifXMLResource;

	}
	
	protected Collection<? extends EObject> convertRifXML2RifEcore(
			EList<EObject> contents) {

		XML2RifConverter xml2RifConverter = new XML2RifConverter();
		Collection<? extends EObject> result = xml2RifConverter
				.convert(contents);

		return result;
	}

	@Override
	public void save(Map<?, ?> options) throws IOException {

		if (errors != null) {
			errors.clear();
		}

		if (warnings != null) {
			warnings.clear();
		}

		try {

			if (options == null) {
				options = Collections.EMPTY_MAP;
			}

			// No new XMLSave is created, but the resource is saved using RIF
			// XML Resource
			Collection<? extends EObject> rifXMLContents = convertRifEcore2RifXML(this
					.getContents());
			saveRifXMLResource(rifXMLContents, options);

		} finally {

			setModified(false);

		}

	}

	protected Collection<? extends EObject> convertRifEcore2RifXML(
			EList<EObject> contents) {
		Rif2XMLConverter reqIf2XMLConverter = new Rif2XMLConverter();
		Collection<? extends EObject> result = reqIf2XMLConverter.convert(this
				.getContents());
		return result;
	}

	protected void saveRifXMLResource(Collection<? extends EObject> contents,
			Map<?, ?> options) throws IOException {
		ResourceSet rifXMLResourceSet = new ResourceSetImpl();

		// Register new ResourceFactory for "rif" extension
		rifXMLResourceSet.getResourceFactoryRegistry()
				.getExtensionToFactoryMap()
				.put("rif", new RifResourceFactoryImpl());

		XMLResource rifXMLResource = (XMLResource) rifXMLResourceSet
				.createResource(uri);

		rifXMLResource.getDefaultSaveOptions().put(XMLResource.OPTION_ENCODING,
				getEncoding());
		
		rifXMLResource.getContents().add(getDocumentRoot());

		if (!contents.isEmpty())
			getDocumentRoot().setRIF((RIF) contents.toArray()[0]);
		
		rifXMLResource.save(options);
		
		setTimeStamp(rifXMLResource.getTimeStamp());

	}
	
	protected DocumentRoot getDocumentRoot(){
		
		if (documentRoot == null) {
			documentRoot = RifFactory.eINSTANCE.createDocumentRoot();
			documentRoot.getXMLNSPrefixMap().put("", RIF_URI);
			documentRoot.getXMLNSPrefixMap().put(XSI_NS, XSI_URI);
			documentRoot.getXSISchemaLocation().put(RIF_SCHEMA_URI,
					RIF_SCHEMA_LOCATION);
			documentRoot.getXSISchemaLocation().put(RIF_XHTML_SCHEMA_URI,
					RIF_XHTML_SCHEMA_LOCATION);
		}
		
		return documentRoot;
		
	}
	
	protected void setDocumentRoot(DocumentRoot documentRoot){
		this.documentRoot = documentRoot;
	}

} // ReqIfResourceImpl
