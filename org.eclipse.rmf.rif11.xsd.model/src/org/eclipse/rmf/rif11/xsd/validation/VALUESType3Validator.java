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

package org.eclipse.rmf.rif11.xsd.validation;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.rmf.rif11.xsd.ATTRIBUTEVALUEEMBEDDEDDOCUMENT;
import org.eclipse.rmf.rif11.xsd.ATTRIBUTEVALUEEMBEDDEDFILE;
import org.eclipse.rmf.rif11.xsd.ATTRIBUTEVALUEENUMERATION;
import org.eclipse.rmf.rif11.xsd.ATTRIBUTEVALUEFILEREFERENCE;
import org.eclipse.rmf.rif11.xsd.ATTRIBUTEVALUESIMPLE;
import org.eclipse.rmf.rif11.xsd.ATTRIBUTEVALUEXMLDATA;


/**
 * A sample validator interface for {@link org.eclipse.rmf.rif11.xsd.VALUESType3}.
 * This doesn't really do anything, and it's not a real EMF artifact.
 * It was generated by the org.eclipse.emf.examples.generator.validator plug-in to illustrate how EMF's code generator can be extended.
 * This can be disabled with -vmargs -Dorg.eclipse.emf.examples.generator.validator=false.
 */
public interface VALUESType3Validator {
	boolean validate();

	boolean validateGroup(FeatureMap value);
	boolean validateATTRIBUTEVALUEEMBEDDEDDOCUMENT(EList<ATTRIBUTEVALUEEMBEDDEDDOCUMENT> value);
	boolean validateATTRIBUTEVALUEEMBEDDEDFILE(EList<ATTRIBUTEVALUEEMBEDDEDFILE> value);
	boolean validateATTRIBUTEVALUEENUMERATION(EList<ATTRIBUTEVALUEENUMERATION> value);
	boolean validateATTRIBUTEVALUEFILEREFERENCE(EList<ATTRIBUTEVALUEFILEREFERENCE> value);
	boolean validateATTRIBUTEVALUESIMPLE(EList<ATTRIBUTEVALUESIMPLE> value);
	boolean validateATTRIBUTEVALUEXMLDATA(EList<ATTRIBUTEVALUEXMLDATA> value);
}
