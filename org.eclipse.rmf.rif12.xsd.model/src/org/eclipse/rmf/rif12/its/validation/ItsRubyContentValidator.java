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

package org.eclipse.rmf.rif12.its.validation;

import org.eclipse.rmf.rif12.its.RbType;
import org.eclipse.rmf.rif12.its.RbcType;
import org.eclipse.rmf.rif12.its.RpType;
import org.eclipse.rmf.rif12.its.RtType;
import org.eclipse.rmf.rif12.its.RtcType;

/**
 * A sample validator interface for {@link org.eclipse.rmf.rif12.its.ItsRubyContent}.
 * This doesn't really do anything, and it's not a real EMF artifact.
 * It was generated by the org.eclipse.emf.examples.generator.validator plug-in to illustrate how EMF's code generator can be extended.
 * This can be disabled with -vmargs -Dorg.eclipse.emf.examples.generator.validator=false.
 */
public interface ItsRubyContentValidator {
	boolean validate();

	boolean validateRb(RbType value);
	boolean validateRt(RtType value);
	boolean validateRp(RpType value);
	boolean validateRt1(RtType value);
	boolean validateRp1(RpType value);
	boolean validateRbc(RbcType value);
	boolean validateRtc(RtcType value);
	boolean validateRtc1(RtcType value);
}
