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

package org.eclipse.rmf.reqif10.pror.presentation.id.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.rmf.reqif10.ReqIF10Package;
import org.eclipse.rmf.reqif10.datatypes.DatatypesPackage;
import org.eclipse.rmf.reqif10.pror.configuration.ConfigurationPackage;
import org.eclipse.rmf.reqif10.pror.presentation.id.IdConfiguration;
import org.eclipse.rmf.reqif10.pror.presentation.id.IdFactory;
import org.eclipse.rmf.reqif10.pror.presentation.id.IdPackage;
import org.eclipse.rmf.reqif10.pror.presentation.id.IdVerticalAlign;
import org.eclipse.rmf.reqif10.xhtml.XhtmlPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class IdPackageImpl extends EPackageImpl implements IdPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass idConfigurationEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum idVerticalAlignEEnum = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.eclipse.rmf.reqif10.pror.presentation.id.IdPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private IdPackageImpl() {
		super(eNS_URI, IdFactory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 * 
	 * <p>This method is used to initialize {@link IdPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static IdPackage init() {
		if (isInited) return (IdPackage)EPackage.Registry.INSTANCE.getEPackage(IdPackage.eNS_URI);

		// Obtain or create and register package
		IdPackageImpl theIdPackage = (IdPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof IdPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new IdPackageImpl());

		isInited = true;

		// Initialize simple dependencies
		ConfigurationPackage.eINSTANCE.eClass();
		ReqIF10Package.eINSTANCE.eClass();
		XhtmlPackage.eINSTANCE.eClass();
		DatatypesPackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theIdPackage.createPackageContents();

		// Initialize created meta-data
		theIdPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theIdPackage.freeze();

  
		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(IdPackage.eNS_URI, theIdPackage);
		return theIdPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getIdConfiguration() {
		return idConfigurationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIdConfiguration_Prefix() {
		return (EAttribute)idConfigurationEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIdConfiguration_Count() {
		return (EAttribute)idConfigurationEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getIdConfiguration_VerticalAlign() {
		return (EAttribute)idConfigurationEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getIdVerticalAlign() {
		return idVerticalAlignEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public IdFactory getIdFactory() {
		return (IdFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		idConfigurationEClass = createEClass(ID_CONFIGURATION);
		createEAttribute(idConfigurationEClass, ID_CONFIGURATION__PREFIX);
		createEAttribute(idConfigurationEClass, ID_CONFIGURATION__COUNT);
		createEAttribute(idConfigurationEClass, ID_CONFIGURATION__VERTICAL_ALIGN);

		// Create enums
		idVerticalAlignEEnum = createEEnum(ID_VERTICAL_ALIGN);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Obtain other dependent packages
		ConfigurationPackage theConfigurationPackage = (ConfigurationPackage)EPackage.Registry.INSTANCE.getEPackage(ConfigurationPackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		idConfigurationEClass.getESuperTypes().add(theConfigurationPackage.getProrPresentationConfiguration());

		// Initialize classes and features; add operations and parameters
		initEClass(idConfigurationEClass, IdConfiguration.class, "IdConfiguration", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIdConfiguration_Prefix(), ecorePackage.getEString(), "prefix", "REQ-", 1, 1, IdConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIdConfiguration_Count(), ecorePackage.getEInt(), "count", null, 1, 1, IdConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIdConfiguration_VerticalAlign(), this.getIdVerticalAlign(), "verticalAlign", "Top", 0, 1, IdConfiguration.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(idVerticalAlignEEnum, IdVerticalAlign.class, "IdVerticalAlign");
		addEEnumLiteral(idVerticalAlignEEnum, IdVerticalAlign.TOP);
		addEEnumLiteral(idVerticalAlignEEnum, IdVerticalAlign.CENTER);

		// Create resource
		createResource(eNS_URI);
	}

} //IdPackageImpl
