/*
 *
 * License Terms
 *
 * Copyright (c) 2016, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * *   Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * *   Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the
 *    distribution.
 *
 * *   Neither the name of Caltech nor its operating division, the Jet
 *    Propulsion Laboratory, nor the names of its contributors may be
 *    used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gov.nasa.jpl.imce.profileGenerator.io;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.io.File;
import java.io.IOException;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.MDCounter;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.imce.profileGenerator.model.profile.*;
import gov.nasa.jpl.imce.profileGenerator.model.profile.Constraint;
import gov.nasa.jpl.imce.profileGenerator.model.profile.DataType;
import gov.nasa.jpl.imce.profileGenerator.model.profile.Enumeration;
import gov.nasa.jpl.imce.profileGenerator.model.profile.EnumerationLiteral;
import gov.nasa.jpl.imce.profileGenerator.model.profile.Generalization;
import gov.nasa.jpl.imce.profileGenerator.model.profile.Package;
import gov.nasa.jpl.imce.profileGenerator.util.MDUMLModelUtils;
import gov.nasa.jpl.imce.profileGenerator.util.MDUtils;
import gov.nasa.jpl.imce.profileGenerator.util.MDElementIDGenerator;
import gov.nasa.jpl.imce.profileGenerator.transformation.Configuration;
import gov.nasa.jpl.imce.profileGenerator.util.PUICUtils;

/**
 * Note: this can be seen as a "IMCE Profile 2 MD Profile" model-to-model
 * transformation! (or even as a kind of "serialization" of the IMCE profile
 * model to MD)
 *
 * TODO Need to rewrite this to first write all packages, then data types, etc.
 *
 * @author Sebastian.J.Herzig@jpl.nasa.gov
 */
public class MDUMLProfileWriter {

	/** */
	private Element _profile = null;

	/** Generated MD customizations package (if relevant). */
	private Element _validationCustomizationsPackage = null;

	/** Generated OCL validation rules package (if relevant). */
	private Element _validationOCLPackage = null;

	/** */
	private HashMap<Object,Element> _mappings = new HashMap<Object,Element>();

	/**
	 *
	 * @param profilePackage
	 * @return
	 */
	public Element writeModel(Package profilePackage) {
		// Best order to avoid running algorithm multiple times is:
		// - OCL validation package first
		// - Then stereotypes (refer to OCL constraints)
		// - Then MD customizations (refer to stereotypes)

		// Initialize
		prepare();

		Model defaultOwner = Application.getInstance().getProjectsManager().getActiveProject().getModel();

		// Create package structure
		Element rootPackage = writePackage(profilePackage, defaultOwner);

		// Now write generalizations
		Set<Object> keys = new HashSet<Object>();
		keys.addAll(_mappings.keySet());
		for (Object mappedObject : keys)
			if (mappedObject instanceof Stereotype)
				for (Generalization g : ((Stereotype) mappedObject).getGeneralization())
					writeGeneralization(g);

		// Lastly, write redefinitions of metaclass extensions
		for (Element mappedElement : _mappings.values())
			if (mappedElement instanceof com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype)
				writeMetaClassExtensionRedefinitions(
						(com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype) mappedElement);

		cleanup();

		return rootPackage;
	}

	/**
	 *
	 * @param pkg
	 * @param owner
	 * @return
	 */
	private Element writePackage(Package pkg, Element owner) {
		Element mdPackage = resolveElementInTargetModel(pkg, com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package.class);

		if (mdPackage == null) {
			mdPackage = MDUMLModelUtils.createPackage(pkg.getName(), owner);
			
			// Set element ID
			String id = MDElementIDGenerator.constructID((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.PackageableElement) mdPackage);
			mdPackage.setID(id);

			// Apply any stereotypes that may be defined
			for (Stereotype s : pkg.getAppliedStereotypes())
				MDUMLModelUtils.applyStereotypeByName(mdPackage, s.getName());

			// Share, if specified
			if (pkg.isSharePackage())
				MDUMLModelUtils.sharePackage((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package) mdPackage);

			// Check whether this is the top-level validation or customization package
			if (pkg instanceof ValidationPackage)
				this._validationOCLPackage = mdPackage;
			else if (pkg instanceof CustomizationPackage)
				this._validationCustomizationsPackage = mdPackage;

			_mappings.put(pkg, mdPackage);
		}

        for (Package subPkg : pkg.getOwnedPackages())
            if (subPkg instanceof Profile)
                writeProfile((Profile) subPkg, mdPackage);
            else
                writePackage(subPkg, mdPackage);

		// Contained data type definitions
		for (DataType d : pkg.getDataTypes())
			writeDataType(d, mdPackage);

		// Contained stereotypes
		for (Stereotype s : pkg.getStereotypes())
			writeStereotype(s, mdPackage);

		// Components
		for (Classifier c : pkg.getClassifiers())
			if (c instanceof Component)
				writeComponent((Component) c, mdPackage);
			else if (c instanceof Block)
				writeBlock((Block) c, mdPackage);

		// Contained MD customizations
		for (Customization c : pkg.getCustomizations()) {
			writeMDCustomizationClass(c, mdPackage);
		}

		// Contained constraint definitions
		for (Constraint c : pkg.getConstraints()) {
            writeValidationOCLConstraint(c, mdPackage);
        }

		return mdPackage;
	}

	/**
	 *
	 * @param stereotype
	 * @param owner
	 * @return
	 */
	private Element writeStereotype(Stereotype stereotype, Element owner) {
		Element mdStereotype = resolveElementInTargetModel(stereotype, com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype.class);

		if (mdStereotype == null) {
			mdStereotype = MDUMLModelUtils.createStereotype(stereotype.getName(), stereotype.isAbstract(), owner);

			// Set element ID
			String id = MDElementIDGenerator.constructID((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.PackageableElement) mdStereotype);
			mdStereotype.setID(id);
			
			// Set metaclass extension
			if (stereotype.getMetaclass() != null)
				writeMetaClassExtension(
						(com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype) mdStereotype,
						stereotype.getMetaclass().getName());
			
			// Properties
			for(Attribute p : stereotype.getAttributes())
				writeProperty(p, mdStereotype);

			// Create documentation
			MDUMLModelUtils.createDocumentationAnnotation(mdStereotype, stereotype.getDocumentation());

			// Add any applied constraints
			for (Constraint c : stereotype.getAppliedConstraints()) {
				// Attempt to resolve the constraint
				Element mdConstraint = resolveElementInTargetModel(c, com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint.class);

				// Apply the constraint to the model
				MDUMLModelUtils.applyConstraint(
						mdStereotype,
						(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint) mdConstraint);
			}

			_mappings.put(stereotype, mdStereotype);
		}

		return mdStereotype;
	}
	
	/**
	 *
	 * @param owner
	 * @return
	 */
	private Element writeDataType(DataType dataType, Element owner) {
		Element mdDataType = resolveElementInTargetModel(dataType, com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DataType.class);

		if (mdDataType == null) {
			if (dataType instanceof Enumeration) {
				mdDataType = MDUMLModelUtils.createEnumeration(dataType.getName(), owner);
	
				// Set element ID
				String id = MDElementIDGenerator.constructID((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DataType) mdDataType);
				mdDataType.setID(id);
				
				// Literals
				for(EnumerationLiteral l : ((Enumeration) dataType).getLiterals())
					writeEnumerationLiteral(l, (com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Enumeration) mdDataType);
	
				_mappings.put(dataType, mdDataType);
			}
		}

		return mdDataType;
	}

	/**
	 *
	 * @param owner
	 * @return
	 */
	private Element writeComponent(Component component, Element owner) {
		Element mdComponent = resolveElementInTargetModel(component, com.nomagic.uml2.ext.magicdraw.components.mdbasiccomponents.Component.class);

		if (mdComponent == null) {
			mdComponent = MDUMLModelUtils.createComponent(component.getName(), owner);

			// Set element ID
			String id = MDElementIDGenerator.constructID((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.PackageableElement) mdComponent);
			mdComponent.setID(id);

			// Properties
			for(Attribute p : component.getAttributes())
				writeProperty(p, mdComponent);

			// Apply any stereotypes
			for (Stereotype s : component.getAppliedStereotypes())
				MDUMLModelUtils.applyStereotypeByName(mdComponent, s.getName());

			_mappings.put(component, mdComponent);
		}

		return mdComponent;
	}

	/**
	 * FIXME Really is a class...
	 * @param owner
	 * @return
	 */
	private Element writeBlock(Block block, Element owner) {
		Element mdBlock = resolveElementInTargetModel(block, com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class);

		if (mdBlock == null) {
			mdBlock = MDUMLModelUtils.createClass(block.getName(), owner);

			// Set element ID
			String id = MDElementIDGenerator.constructID((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.PackageableElement) mdBlock);
			mdBlock.setID(id);

			// Properties
			for(Attribute p : block.getAttributes())
				writeProperty(p, mdBlock);

			// Apply any stereotypes
			for (Stereotype s : block.getAppliedStereotypes())
				MDUMLModelUtils.applyStereotypeByName(mdBlock, s.getName());

			_mappings.put(block, mdBlock);
		}

		return mdBlock;
	}
	
	/**
	 *
	 * @param owner
	 * @return
	 */
	private Element writeEnumerationLiteral(EnumerationLiteral enumLiteral, com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Enumeration owner) {
		Element mdEnumerationLiteral = MDUMLModelUtils.createEnumerationLiteral(enumLiteral.getName(), (com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Enumeration) owner);
		
		// Set element ID
		String id = MDElementIDGenerator.constructID((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.PackageableElement) mdEnumerationLiteral);
		mdEnumerationLiteral.setID(id);
		
		return mdEnumerationLiteral;
	}
	
	/**
	 *
	 * @param owner
	 * @return
	 */
	private Element writeProperty(Attribute property, Element owner) {
		Element type = null;
				
		// Set metaclass extension
		if (property.getType() != null) {
			type = _mappings.get(property.getType());
			
			if (type == null)
				type = resolveElementInTargetModel(property.getType(), com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier.class);
			
			if (type == null)
				System.out.println("[WARN] Could not find type for " + property.getName());
		}
		
		Element mdProperty = MDUMLModelUtils.createProperty(property.getName(), (com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type) type, owner);
		
		// Set element ID
		String id = MDElementIDGenerator.constructID((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property) mdProperty);
		mdProperty.setID(id);

		// Apply any stereotypes
		for (Stereotype s : property.getAppliedStereotypes())
			MDUMLModelUtils.applyStereotypeByName(mdProperty, s.getName());

		_mappings.put(property, mdProperty);

		return mdProperty;
	}

	/**
	 *
	 * @param generalization
	 * @return
	 */
	private Element writeGeneralization(Generalization generalization) {
		// FIXME Dangerous assumption: all generalizations are between stereotypes
		// (OK - but not very generic (i.e., not critical))
		com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype general = (com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype) resolveElementInTargetModel(generalization.getGeneral(), com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype.class);
		com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype specific = (com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype) resolveElementInTargetModel(generalization.getSpecific(), com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype.class);

		if (general != null && specific != null) {
			Element mdGeneralization = MDUMLModelUtils.createGeneralization(general, specific, specific);

			// Set element ID
			String id = MDElementIDGenerator.constructID((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization) mdGeneralization);
			mdGeneralization.setID(id);
			
			_mappings.put(generalization, mdGeneralization);

			return mdGeneralization;
		}

		return null;
	}

	/**
	 *
	 * @param profile
	 * @param owner
	 * @return
	 */
	private Element writeProfile(Profile profile, Element owner) {
		Element mdProfile = resolveElementInTargetModel(profile, com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile.class);

		if (mdProfile == null) {
			mdProfile = MDUMLModelUtils.createProfile(profile.getName(), owner);
			
			// Set element ID
			String id = MDElementIDGenerator.constructID((com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile) mdProfile);
			mdProfile.setID(id);

			// Apply any stereotypes that may be defined
			// IMCE profile bundles have a special stereotype applied
			for (Stereotype s : profile.getAppliedStereotypes())
				MDUMLModelUtils.applyStereotypeByName(mdProfile, s.getName());

			MDUMLModelUtils.sharePackage((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package) mdProfile);

			_mappings.put(profile, mdProfile);
		}

        for (Package subPkg : profile.getOwnedPackages())
            if (subPkg instanceof Profile)
                writeProfile((Profile) subPkg, mdProfile);
            else
                writePackage(subPkg, mdProfile);

		for (DataType d : profile.getDataTypes())
			writeDataType(d, mdProfile);
		
		for (Stereotype s : profile.getStereotypes())
			writeStereotype(s, mdProfile);

		// Components
		for (Classifier c : profile.getClassifiers())
			if (c instanceof Component)
				writeComponent((Component) c, mdProfile);
			else if (c instanceof Block)
				writeBlock((Block) c, mdProfile);

		// Contained MD customizations (should never happen)
		for (Customization c : profile.getCustomizations())
			writeMDCustomizationClass(c, mdProfile);

		// Contained constraint definitions (should never happen)
		for (Constraint c : profile.getConstraints())
			writeValidationOCLConstraint(c, mdProfile);

		this._profile = mdProfile;

		return mdProfile;
	}

	/**
	 *
	 * @param customization
	 * @param owner
     * @return
     */
	private Element writeMDCustomizationClass(Customization customization, Element owner) {
		Element mdCustomization = resolveElementInTargetModel(customization, com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class);

		if (mdCustomization == null) {
			mdCustomization = MDUMLModelUtils.createClass(customization.getName(), owner);

			// Set element ID
			String id = MDElementIDGenerator.constructID((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class) mdCustomization);
			mdCustomization.setID(id);

			// Customization stereotype
			com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype stereotype = MDUMLModelUtils.applyStereotypeByName(mdCustomization, "Customization");

			// Get customization target that was previously transformed
			com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype customizationTarget
					= (com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype) resolveElementInTargetModel(
					customization.getCustomizationTarget(),
					com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype.class);

			// Get source type (here: a stereotype previously transformed)
			com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype typesForSource
					= (com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype) resolveElementInTargetModel(
					customization.getAllowableSourceType(),
					com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype.class);

			// Get target type (here: a stereotype previously transformed)
			com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype typesForTarget
					= (com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype) resolveElementInTargetModel(
					customization.getAllowableTargetType(),
					com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype.class);

			// Get supertype
			com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier superTypes
					= (com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier) resolveElementInTargetModel(
					customization.getSuperType(),
					com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier.class);

			// Only create if successfully resolved all elements
			if (customizationTarget != null
					&& typesForSource != null
					&& typesForTarget != null) {
				MDUMLModelUtils.setStereotypePropertyValue(mdCustomization, stereotype, "customizationTarget", customizationTarget);
				MDUMLModelUtils.setStereotypePropertyValue(mdCustomization, stereotype, "typesForSource", typesForSource);
				MDUMLModelUtils.setStereotypePropertyValue(mdCustomization, stereotype, "typesForTarget", typesForTarget);
			}
			else if (customizationTarget != null
					&& superTypes != null) {
				MDUMLModelUtils.setStereotypePropertyValue(mdCustomization, stereotype, "customizationTarget", customizationTarget);
				MDUMLModelUtils.setStereotypePropertyValue(mdCustomization, stereotype, "superTypes", superTypes);
			}
			else {
				System.out.println("[PANIC] Ran into a resolution issue - need to implement this");
			}

			_mappings.put(customization, mdCustomization);
		}

		return mdCustomization;
	}

	/**
	 *
	 * @param constraint
	 * @param owner
     * @return
     */
	private Element writeValidationOCLConstraint(Constraint constraint, Element owner) {
		Element mdConstraint =
				resolveElementInTargetModel(constraint, com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint.class);

		if (mdConstraint == null) {
			mdConstraint = MDUMLModelUtils.createConstraint(constraint.getName(), owner);

			// Set element ID
			String id =
					MDElementIDGenerator.constructID((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint) mdConstraint);
			mdConstraint.setID(id);

			// Tag constraint as validation rule
			com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype stereotype =
					MDUMLModelUtils.applyStereotypeByName(mdConstraint, "validationRule");

			// Set error message
			if (constraint instanceof ValidationRule)
				MDUMLModelUtils.setStereotypePropertyValue(
						mdConstraint,
						stereotype,
						"errorMessage",
						((ValidationRule) constraint).getMessage());

			// Create an opaque expression for the OCL constraint
			com.nomagic.uml2.ext.magicdraw.classes.mdkernel.OpaqueExpression expression =
					MDUMLModelUtils.createOpaqueExpression(
						constraint.getName() + "Expression",
						constraint.getLanguage(),
						constraint.getBody(),
						mdConstraint);

			((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint) mdConstraint).setSpecification(expression);

			_mappings.put(constraint, mdConstraint);
		}

		return mdConstraint;
	}

	/**
	 *
	 * @param mdStereotype
	 * @param metaclassName
	 */
	private void writeMetaClassExtension(
			com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype mdStereotype,
			String metaclassName) {
		Element mdExtension = MDUMLModelUtils.createMetaClassExtension(mdStereotype,
				MDUMLModelUtils.getMetaClassByName(
						metaclassName));
		
		// Set element ID
		String id = MDElementIDGenerator.constructID((com.nomagic.uml2.ext.magicdraw.mdprofiles.Extension) mdExtension);
		mdExtension.setID(id);
		
		// Get extension ends and set element IDs
		com.nomagic.uml2.ext.magicdraw.mdprofiles.ExtensionEnd mdOwnedExtensionEnd = StereotypesHelper.getExtensionEnd((com.nomagic.uml2.ext.magicdraw.mdprofiles.Extension) mdExtension);
		String extEndID = MDElementIDGenerator.constructID((com.nomagic.uml2.ext.magicdraw.mdprofiles.ExtensionEnd) mdOwnedExtensionEnd);
		mdOwnedExtensionEnd.setID(extEndID);
	}

	/**
	 *
	 * @param mdStereotype
	 */
	private void writeMetaClassExtensionRedefinitions(
			com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype mdStereotype) {
		for (com.nomagic.uml2.ext.magicdraw.mdprofiles.Extension extension : mdStereotype.getExtension()) {
			// Stereotype extension end
			com.nomagic.uml2.ext.magicdraw.mdprofiles.ExtensionEnd stereotypeEnd = StereotypesHelper.getExtensionEnd(extension);

			// Metaclass end
			com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property metaclassEnd = StereotypesHelper.getExtensionMetaProperty(mdStereotype).iterator().next();

			// Retrieve all base classifiers
			Collection<com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier> baseClassifiers = MDUMLModelUtils.getAllGenerals(mdStereotype);

			for (com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier c : baseClassifiers) {
				// Check whether this classifier has a metaclass extension
				if (c instanceof com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype
						&& ((com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype) c).getExtension() != null
						&& ((com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype) c).getExtension().size() > 0) {
					for (com.nomagic.uml2.ext.magicdraw.mdprofiles.Extension e : ((com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype) c).getExtension()) {
						// Add a generalization from the newly created extension to the "inherited"
						Element mdGeneralization = MDUMLModelUtils.createGeneralization(e, extension, extension);

						// Set element ID
						String id = MDElementIDGenerator.constructID((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization) mdGeneralization);
						mdGeneralization.setID(id);
						
						// Redefine extension ends
						if (e.getMemberEnd().size() != 2) {
							System.out.println("[PANIC] something unexpected happened when trying to get extension ends for " + e.getName());

							continue;
						}

						com.nomagic.uml2.ext.magicdraw.mdprofiles.ExtensionEnd sEnd = StereotypesHelper.getExtensionEnd(e);

						com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property mEnd = StereotypesHelper.getExtensionMetaProperty(
								(com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype) c).iterator().next();

						// Add to list of redefined elements
						stereotypeEnd.getRedefinedProperty().add(sEnd);
						metaclassEnd.getRedefinedProperty().add(mEnd);
					}
				}
			}
		}
	}

	/**
	 *
	 * @param pkg
	 * @return
	 */
	/*private Profile findProfile(Package pkg) {
		if (pkg instanceof Profile)
			return (Profile) pkg;

		for (Package p : pkg.getOwnedPackages())
			if (p instanceof Profile)
				return (Profile) p;
			else
				return findProfile(p);

		// No profile found
		return null;
	}*/

	/**
	 *
	 * @param pkg
	 * @param owner
	 * @return
	 */
	// FIXME Dangerous - if more than one package per hierarchy level, this will not return the right package
	/*private Element createPackageStructure(Package pkg, Element owner) {
		Element newOwner = MDUMLModelUtils.findElementByName(pkg.getName());

		if (newOwner == null)
			newOwner = MDUMLModelUtils.createPackage(pkg.getName(), owner);

		if (pkg.getOwnedPackages().size() > 0)
			for (Package ownedPackage : pkg.getOwnedPackages())
				if (!(ownedPackage instanceof Profile))
					return createPackageStructure(ownedPackage, newOwner);

		return newOwner;
	}*/

	/**
	 *
	 */
	private void prepare() {
		// Probably out of class
		if (Application.getInstance() == null) {
			if (Configuration.silent) {
				String args[] = {};
				MDUtils.launchMagicDraw(args);
			} else {
				String args[] = {"-verbose", "DEVELOPER"};
				MDUtils.launchMagicDraw(args);
			}
		}

		MDUtils.loadProject(Configuration.outputFile);

		SessionManager.getInstance().createSession("Profile creation");
		
		// The following will allow the immutable element IDs to be modified
		// FIXME This method is deprecated, and removed in 18.2 - find another way
		MDCounter mdCounter = Application.getInstance().getProjectsManager().getActiveProject().getCounter();
		mdCounter.setCanResetIDForObject(true);
	}

	/**
	 *
	 */
	private void cleanup() {
		SessionManager.getInstance().closeSession();

		PUICUtils mainProjectPUICUtils = new PUICUtils();

		// Mount PUIC profile
		mainProjectPUICUtils.mountPUICProfile();

		// Invoke PUIC to repair project
		mainProjectPUICUtils.repairProject();

		// Unmount PUIC profile
		mainProjectPUICUtils.unmountPUICProfile();

		// Export module
		String exportedModuleFilename =
                Configuration.outputDir +
						((com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile) _profile).getName() + ".mdzip";
		exportAsModuleAndRepair(_profile, exportedModuleFilename);

		System.out.println("Exported main profile module...");

		// Export validation modules as well
		if (Configuration.generateValidation) {
			// MD customizations that prevent illegal relationships in the first place
			if (Configuration.generateValidationCustomizations
					&& _validationCustomizationsPackage != null) {
				String exportedMDCustomizationsModuleFilename =
						Configuration.outputDir +
								((com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile) _profile).getName() + "-mdcustomizations.mdzip";
				exportAsModuleAndRepair(_validationCustomizationsPackage, exportedMDCustomizationsModuleFilename);

				System.out.println("Exported MD customizations for validation module...");
			}

			// OCL validation suite
			if (Configuration.generateValidationOCLValidationSuite
					&& _validationOCLPackage != null) {
				String exportedOCLValidationModuleFilename =
						Configuration.outputDir +
								((com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile) _profile).getName() + "-oclvalidation.mdzip";
				exportAsModuleAndRepair(_validationOCLPackage, exportedOCLValidationModuleFilename);

				System.out.println("Exported OCL validation rules module...");
			}
		}

		// Close output template project
		MDUtils.closeActiveProject();

		System.out.println("!!!! DONE !!!!");

		if (Configuration.silent) {
			MDUtils.shutdownMagicDraw();
		}
	}

	/**
	 *
	 * @param pckg
	 * @param filename
     */
	private void exportAsModuleAndRepair(Element pckg, String filename) {
		// MD fails to write module if the file does not yet exist. This is alleviated by simulating a "touch" command
		// using the standard Java IO API. Note that any missing directories are created first.
		try {
			File f = new File(filename);
			f.getParentFile().mkdirs();
			f.createNewFile();
		}
		catch (IOException e) {
			System.out.println("[ERROR] Failed to create output file for module!");
			System.out.println(e.getMessage());
			return;
		}

		PUICUtils validationModulePUICUtils = new PUICUtils();

		// Export module
		MDUMLModelUtils.exportModule((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package) pckg,
				filename);

		// Load project
		MDUtils.loadProject(filename);

		// Mount PUIC profile
		validationModulePUICUtils.mountPUICProfile();

		// Invoke PUIC to repair project
		validationModulePUICUtils.repairProject();

		// Probably out of class
		MDUtils.saveProject();

		// Unmount profile
		validationModulePUICUtils.unmountPUICProfile();

		// Save project
		MDUtils.saveProject();

		// Close project
		MDUtils.closeActiveProject();
	}

	/**
	 *
	 * @param element
	 * @param type
	 * @return
	 */
	private Element resolveElementInTargetModel(Object element, java.lang.Class type) {
		if (element == null)
			return null;

		// FIXME Dangerous type casting
		Element resolvedElement = (Element) _mappings.get(element);

		if (resolvedElement == null) {
			// Search in model if not part of mappings
			// FIXME Dangerous type assumptions & casting
			resolvedElement = MDUMLModelUtils.findElementByQualifiedNameAndType(
					((gov.nasa.jpl.imce.profileGenerator.model.profile.NamedElement) element).getQualifiedName(),
					type);
		}

		// If all fails, try to map the element
		//if (resolvedElement == null) {
		//	if (element instanceof Stereotype)
		//		return writeStereotype((Stereotype) element, resolveElementInTargetModel(((Stereotype) element).getOwner(), com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype.class));
		//}

		return resolvedElement;
	}

}