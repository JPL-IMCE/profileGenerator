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
package gov.nasa.jpl.imce.profileGenerator.transformation;

import java.util.*;
import java.util.concurrent.SynchronousQueue;

import gov.nasa.jpl.imce.profileGenerator.model.bundle.*;
import gov.nasa.jpl.imce.profileGenerator.model.profile.*;
import gov.nasa.jpl.imce.profileGenerator.model.profile.DataType;
import gov.nasa.jpl.imce.profileGenerator.model.profile.Enumeration;
import gov.nasa.jpl.imce.profileGenerator.model.profile.Generalization;
import gov.nasa.jpl.imce.profileGenerator.model.profile.NamedElement;
import gov.nasa.jpl.imce.profileGenerator.model.profile.Package;
import gov.nasa.jpl.imce.profileGenerator.model.profile.ReferencedElement;
import gov.nasa.jpl.imce.profileGenerator.util.MDUMLModelUtils;

/**
 * Mappings.
 * 
 * @author sherzig
 */
public class Bundle2ProfileMappings {

	/** */
	private HashMap<Object,Object> _transformedObjects = new HashMap<Object,Object>();
	
	/** */
	private HashSet<Object> _targetModelElements = new HashSet<Object>();
	
	/**
	 * 
	 * @param bundle
	 * @return
	 */
	public Package bundleToProfile(
			gov.nasa.jpl.imce.profileGenerator.model.bundle.BundleDigest bundle) {
		// Initial package structure: parsed from bundle IRI
		String bundleIRI = bundle.getBundleIRI();
		//   Remove protocol
		bundleIRI = bundleIRI.substring(bundleIRI.indexOf("://") + 3);
		//   Extract domain & split paths
		String[] iriElements = bundleIRI.split("/");
		
		// Check for empty elements
		ArrayList<String> pkgStructure = new ArrayList<String>();
		for (String e : iriElements)
			if (!e.equals(""))
				pkgStructure.add(e);
		
		String profilePackage = pkgStructure.get(pkgStructure.size() - 1);
		List<String> packageStructure = pkgStructure.subList(0, pkgStructure.size() - 1);
		
		// Root package for bundles in profile
		// -> As command line argument?? With alias??
		Package root = new Package("IMCE Ontology Bundles");
		Package owner = root;
		
		_targetModelElements.add(root);
		
		// Create package structure
		for (String p : packageStructure) {
			Package previousOwner = owner;
			owner = new Package(p, owner);
			previousOwner.getOwnedPackages().add(owner);
			
			_targetModelElements.add(owner);
		}

		// MD customizations & OCL validation suite
		Package mdCustomizationPackage = null;
		Package oclValidationSuite = null;

		// To avoid having to implement resolution tricks, the following order is imposed:
		// - First transform the OCL validation suite (just constraint definitions)
		// - Then the profile (apply constraints)
		// - Finally, the MD customizations (refer to stereotypes from the profile)
		if (Configuration.generateValidation) {
			// OCL Validation queries
			if (Configuration.generateValidationOCLValidationSuite) {
				oclValidationSuite = new ValidationPackage(profilePackage + "-validation", owner);
				oclValidationSuite.setSharePackage(true);		// Share package, since exported as module
				oclValidationSuite.getAppliedStereotypes().add(new Stereotype("validationSuite", false, null));
				owner.getOwnedPackages().add(oclValidationSuite);

				_targetModelElements.add(oclValidationSuite);
			}
		}

		// Now the actual profile package
		Profile p = new Profile(profilePackage, owner);
		owner.getOwnedPackages().add(p);

		// Add IMCE stereotypes (create dummy stereotype for existing one)
		p.getAppliedStereotypes().add(new Stereotype("owl2-mof2:BundledOntologyProfile", false, null));
		p.getAppliedStereotypes().add(new Stereotype("auxiliaryResource", false, null));
		
		_targetModelElements.add(p);

		// Finally, the MD customizations, if configured to do so
		if (Configuration.generateValidation) {
			if (Configuration.generateValidationCustomizations) {
				// MD Customizations (to disallow some relationships to be modeled)
				if (Configuration.generateValidationCustomizations) {
					mdCustomizationPackage = new CustomizationPackage(profilePackage + "-customizations", owner);
					mdCustomizationPackage.setSharePackage(true);
					owner.getOwnedPackages().add(mdCustomizationPackage);

					_targetModelElements.add(mdCustomizationPackage);
				}
			}
		}
		
		// Map data types
		for (gov.nasa.jpl.imce.profileGenerator.model.bundle.DataType d : bundle.getDataTypes()) {
			dataTypeToDataType(d, p);
		}

		// Store a list of the created stereotypes for later processing
		ArrayList<Stereotype> createdStereotypes = new ArrayList<>();

		// Map classes
		for (gov.nasa.jpl.imce.profileGenerator.model.bundle.Class c : bundle.getClasses()) {
			createdStereotypes.add(classToStereotype(c, p));
		}
		
		// Map object properties
		for (gov.nasa.jpl.imce.profileGenerator.model.bundle.ObjectProperty o : bundle.getObjectProperties()) {
			Stereotype stereotype = objectPropertyToStereotype(o, p);
		}
		
		// Map data type properties
		for (gov.nasa.jpl.imce.profileGenerator.model.bundle.DataTypeProperty d : bundle.getDataTypeProperties()) {
			dataTypePropertyToAttribute(d, p);
		}
		
		// Map class taxonomy
		for (gov.nasa.jpl.imce.profileGenerator.model.bundle.Generalization g : bundle.getGeneralizations()) {
			NamedElement specific = ((NamedElement) resolveTargetModelElement(g.getSpecific()));
			
			// Extension
			if (g.getGeneral() instanceof gov.nasa.jpl.imce.profileGenerator.model.bundle.ReferencedElement
					&& isMetaclass((gov.nasa.jpl.imce.profileGenerator.model.bundle.ReferencedElement) g.getGeneral())
					&& specific instanceof Stereotype) {
				((Stereotype) specific).setMetaclass(
						new MetaClass(
								getMetaclassName(g.getGeneral())));
			}
			// Generalization
			else {
				if (specific != null)
					specific.getGeneralization().add(generalizationToGeneralization(g));
			}
		}

		// Validation rules require the taxonomy to exist - especially the metaclasses, such that the correct
		// validation rules can be created --> create this at the very end
		// Map object properties
		for (gov.nasa.jpl.imce.profileGenerator.model.bundle.ObjectProperty o : bundle.getObjectProperties()) {
			NamedElement resolvedObjProperty = ((NamedElement) resolveTargetModelElement(o));

			if (resolvedObjProperty instanceof Stereotype) {
				Stereotype stereotype = (Stereotype) resolvedObjProperty;
				// Domain & range validation rules & MD customization
				if (stereotype != null
						&& Configuration.generateValidation) {
					if (Configuration.generateValidationCustomizations) {
						objectPropertyToValidationCustomization(o, stereotype, mdCustomizationPackage);
					}

					if (Configuration.generateValidationOCLValidationSuite) {
						Constraint rule = objectPropertyToValidationRule(o, stereotype, oclValidationSuite);

						// Apply validation rule / constraint to stereotype
						if (rule != null)
							stereotype.getAppliedConstraints().add(rule);
					}
				}
			}
		}

		// Similarly, any "library" blocks require information about the meta-classes, which is derived from
		// the class taxonomy
		// Note: this is currently experimental
		if (Configuration.generateCompanionAspects) {
			for (Stereotype s : createdStereotypes) {
				String metaClassName = determineMetaclassName(s);

				// TODO This is a terrible way of checking whether something is a classifier - should be changed some time
				// in the future, or externalized.
				if (metaClassName != null
						&& (metaClassName.equals("Classifier")
						|| metaClassName.equals("Class")
						|| metaClassName.equals("Component")
						|| metaClassName.equals("AssociationClass")))
					createCompanionAspect(s, p);
			}
		}
		
		return root;
	}
	
	/**
	 * Maps a class to a stereotype.
	 *
	 * @param clazz
	 * @param targetModelOwner
	 * @return
	 */
	public Stereotype classToStereotype(
			gov.nasa.jpl.imce.profileGenerator.model.bundle.Class clazz, Element targetModelOwner) {
		// Check whether the class should be mapped in the first place
		if (isMetaclass(clazz)								// Don't map stereotypes identifying as meta-classes
				|| clazz.isReifiedObjectProperty())			// Don't map reified object properties
			return null;
		
		// Determine owning package name (e.g., for "mission:Mission" this becomes "mission")
		String packageName = clazz.getName().split(":")[0];
		
		// Try to get package (note: elements are unique by qualified name here - so setting name as qualified name (since no owner))
		Package owningPackage = (Package) resolveTargetModelElementByQualifiedName(((Package) targetModelOwner).getQualifiedName() + "::" + packageName);
		
		if (owningPackage == null) {
			owningPackage = new Package(packageName, (Package) targetModelOwner);
			((Package) targetModelOwner).getOwnedPackages().add(owningPackage);
			
			_targetModelElements.add(owningPackage);
		}
		
		// Now add the stereotype with the appropriate owner in the target model
		Stereotype s = new Stereotype(clazz.getName(), clazz.isAbstract(), owningPackage);
		s.setDocumentation(clazz.getDocumentation());
		
		_transformedObjects.put(clazz, s);
		_targetModelElements.add(s);
		
		return s;
	}

	/**
	 * Create a companion aspect for a stereotype.
	 *
	 * Note that for all cases where the meta-class is a kind of Classifier,
	 * a companion stereotype inheriting from base:IdentifiedElement, and a
	 * Component stereotyped with this new stereotype will be created. This
	 * component then owns all properties associated with the class. To
	 * simplify modeling, an additional customization is created, which
	 * automatically adds a specialization relationship between any application
	 * of the resulting stereotype and the newly created Component.
	 *
	 * For instance, for vandv:VerificationItem the following is created:
	 * - Stereotype "vandv:VerificationItem"
	 * - Stereotype "vandv:VerificationItemAspect" as specialization of
	 * 		base:IdentifiedElement
	 * - Component "vandv:VerificationItemComponent" stereotyped with
	 * 		vandv:VerificationItemAspect
	 * - Customization "vandv:VerificationItemCustomization" which automatically
	 * 		creates a specialization between any UML element that has stereotype
	 * 		vandv:VerificationItem applied and vandv:VerificationItemComponent
	 *
	 * @param stereotype
	 * @param targetModelOwner
	 * @return
	 */
	private void createCompanionAspect(
			Stereotype stereotype, Element targetModelOwner) {
		// Determine owning package name (e.g., for "mission:Mission" this becomes "mission")
		String aspectsPackageName = stereotype.getName().split(":")[0] + "::aspects";

		// Try to get package (note: elements are unique by qualified name here - so setting name as qualified name (since no owner))
		Package aspectsPackage = (Package) resolveTargetModelElementByQualifiedName(((Package) targetModelOwner).getQualifiedName() + "::" + aspectsPackageName);

		if (aspectsPackage == null) {
			// Determine owning package name (e.g., for "mission:Mission" this becomes "mission")
			String packageName = stereotype.getName().split(":")[0];

			// Try to get package (note: elements are unique by qualified name here - so setting name as qualified name (since no owner))
			Package owningPackage = (Package) resolveTargetModelElementByQualifiedName(((Package) targetModelOwner).getQualifiedName() + "::" + packageName);

			if (owningPackage == null) {
				owningPackage = new Package(packageName, (Package) targetModelOwner);
				((Package) targetModelOwner).getOwnedPackages().add(owningPackage);

				_targetModelElements.add(owningPackage);
			}

			aspectsPackage = new Package("aspects", owningPackage);
			owningPackage.getOwnedPackages().add(aspectsPackage);

			_targetModelElements.add(aspectsPackage);
		}

		// We need to create:
		//	- A stereotype that inherits from base:IdentifiedElement, with meta-class component
		//	- A component that has this stereotype applied
		//	- A MD customization that automatically creates a generalization
		Stereotype companionAspectStereotype = createCompanionAspectStereotype(stereotype, aspectsPackage);

		String metaClassName = determineMetaclassName(stereotype);

		Classifier aspect;

		if (metaClassName.equals("Class"))
			aspect = createCompanionAspectBlock(stereotype, companionAspectStereotype, aspectsPackage);
		else
			aspect = createCompanionAspectComponent(stereotype, companionAspectStereotype, aspectsPackage);

		createCompanionAspectCustomization(stereotype, aspect, aspectsPackage);
	}

	/**
	 *
	 * @param stereotype
	 * @param targetModelOwner
	 * @return
	 */
	private Stereotype createCompanionAspectStereotype(
			Stereotype stereotype, Element targetModelOwner) {
		// Now add the stereotype with the appropriate owner in the target model
		Stereotype s = new Stereotype(stereotype.getName() + "Aspect", false, targetModelOwner);
		Generalization g = new Generalization(new ReferencedElement("base:IdentifiedElement"), s);
		s.getGeneralization().add(g);

		_targetModelElements.add(s);
		_targetModelElements.add(g);

		return s;
	}

	/**
	 *
	 * @param stereotype
	 * @param aspectStereotype
	 * @param targetModelOwner
	 * @return
	 */
	private Component createCompanionAspectComponent(
			Stereotype stereotype, Stereotype aspectStereotype, Element targetModelOwner) {
		// Now add the stereotype with the appropriate owner in the target model
		Component c = new Component(stereotype.getName() + "Template", true, targetModelOwner);
		c.getAppliedStereotypes().add(aspectStereotype);

		// Remove all of the stereotype properties and add the properties as value properties to the new component instead
		c.getAttributes().addAll(stereotype.getAttributes());
		stereotype.getAttributes().clear();

		_targetModelElements.add(c);

		return c;
	}

	/**
	 *
	 * @param stereotype
	 * @param aspectStereotype
	 * @param targetModelOwner
	 * @return
	 */
	private Block createCompanionAspectBlock(
			Stereotype stereotype, Stereotype aspectStereotype, Element targetModelOwner) {
		// Now add the stereotype with the appropriate owner in the target model
		Block b = new Block(stereotype.getName() + "Template", true, targetModelOwner);
		b.getAppliedStereotypes().add(aspectStereotype);

		// Remove all of the stereotype properties and add the properties as value properties to the new component instead
		b.getAttributes().addAll(stereotype.getAttributes());
		stereotype.getAttributes().clear();

		_targetModelElements.add(b);

		return b;
	}

	/**
	 *
	 * @param stereotype
	 * @param aspect
	 * @param targetModelOwner
	 * @return
	 */
	private Customization createCompanionAspectCustomization(
			Stereotype stereotype, Classifier aspect, Element targetModelOwner) {
		// Now add the stereotype with the appropriate owner in the target model
		Customization c = new Customization(stereotype.getName() + "AspectCustomization");
		c.setCustomizationTarget(stereotype);
		c.setSuperType(aspect);

		if (targetModelOwner instanceof Package)
			((Package) targetModelOwner).getCustomizations().add(c);
		else
			System.out.println("[PANIC] Unexpected: owner of a customization is not a Package");

		_targetModelElements.add(c);

		return c;
	}
	
	/**
	 *
	 * @return
	 */
	public Generalization generalizationToGeneralization(
			gov.nasa.jpl.imce.profileGenerator.model.bundle.Generalization generalization) {
		Generalization g = new Generalization(
				(NamedElement) resolveTargetModelElement(generalization.getGeneral()),
				(NamedElement) resolveTargetModelElement(generalization.getSpecific()));
		
		_transformedObjects.put(generalization, g);
		_targetModelElements.add(g);
		
		// Possibly add an extension, depending on the semantics of what is being inherited from (e.g.,
		// SysML-metamodel:Class.Block)
		if (symbolizesRequiredExtension(generalization.getGeneral())
				&& g.getSpecific() instanceof Stereotype
				&& ((Stereotype) g.getSpecific()).getMetaclass() == null) {
			((Stereotype) g.getSpecific()).setMetaclass(
					new MetaClass(
							getSymbolizedRequiredExtension(generalization.getGeneral())));
		}
		
		return g;
	}

	/**
	 *
	 * @param objectProperty
	 * @param targetModelOwner
     * @return
     */
	public Stereotype objectPropertyToStereotype(
			gov.nasa.jpl.imce.profileGenerator.model.bundle.ObjectProperty objectProperty, 
			Element targetModelOwner) {
		// Check whether the object property should be mapped in the first place
		if (objectProperty.isDerived())				// Don't map derived object properties
			return null;
		
		// Determine owning package name (e.g., for "mission:Mission" this becomes "mission")
		String packageName = objectProperty.getName().split(":")[0];
		
		// Try to get package (note: elements are unique by qualified name here - so setting name as qualified name (since no owner))
		Package owningPackage = (Package) resolveTargetModelElementByQualifiedName(((Package) targetModelOwner).getQualifiedName() + "::" + packageName);
		
		if (owningPackage == null) {
			owningPackage = new Package(packageName, (Package) targetModelOwner);
			((Package) targetModelOwner).getOwnedPackages().add(owningPackage);
			
			_targetModelElements.add(owningPackage);
		}
		
		Stereotype s = new Stereotype(objectProperty.getName(), objectProperty.isAbstract(), owningPackage);
		s.setDocumentation(objectProperty.getDocumentation());
		
		_transformedObjects.put(objectProperty, s);
		_targetModelElements.add(s);
		
		return s;
	}

	/**
	 * Adds a customization element to the target profile that specifies the allowable
	 * source and target types for the elements that can be connected using this particular
	 * object property.
	 *
	 * @param objectProperty
	 * @param targetModelElement
	 * @param targetModelOwner
     * @return
     */
	public Customization objectPropertyToValidationCustomization(
			gov.nasa.jpl.imce.profileGenerator.model.bundle.ObjectProperty objectProperty,
			Stereotype targetModelElement,
			Element targetModelOwner) {
		if (objectProperty == null || targetModelElement == null || targetModelOwner == null)
			return null;

		// Determine owning package name (e.g., for "mission:Mission" this becomes "mission")
		String packageName = objectProperty.getName().split(":")[0];

		// Try to get package (note: elements are unique by qualified name here - so setting name as qualified name (since no owner))
		Package owningPackage = (Package) resolveTargetModelElementByQualifiedName(((Package) targetModelOwner).getQualifiedName() + "::" + packageName);

		if (owningPackage == null) {
			owningPackage = new Package(packageName, (Package) targetModelOwner);
			((Package) targetModelOwner).getOwnedPackages().add(owningPackage);

			_targetModelElements.add(owningPackage);
		}

		// Resolve source and target stereotypes / elements
		NamedElement sourceType = (NamedElement) resolveTargetModelElement(objectProperty.getSrcType());
		NamedElement targetType = (NamedElement) resolveTargetModelElement(objectProperty.getTargetType());

		Customization c = new Customization(objectProperty.getName() + " validation customization");
		c.setAllowableSourceType(sourceType);
		c.setAllowableTargetType(targetType);
		c.setCustomizationTarget(targetModelElement);

		owningPackage.getCustomizations().add(c);

		_targetModelElements.add(c);

		return c;
	}

	/**
	 * Adds a customization element to the target profile that specifies the allowable
	 * source and target types for the elements that can be connected using this particular
	 * object property.
	 *
	 * @param objectProperty
	 * @param targetModelElement
	 * @param targetModelOwner
	 * @return
	 */
	public Constraint objectPropertyToValidationRule(
			gov.nasa.jpl.imce.profileGenerator.model.bundle.ObjectProperty objectProperty,
			Stereotype targetModelElement,
			Element targetModelOwner) {
		if (objectProperty == null || targetModelElement == null || targetModelOwner == null)
			return null;

		// Determine owning package name (e.g., for "mission:Mission" this becomes "mission")
		String packageName = objectProperty.getName().split(":")[0];

		// Try to get package (note: elements are unique by qualified name here - so setting name as qualified name (since no owner))
		Package owningPackage = (Package) resolveTargetModelElementByQualifiedName(((Package) targetModelOwner).getQualifiedName() + "::" + packageName);

		if (owningPackage == null) {
			owningPackage = new Package(packageName, (Package) targetModelOwner);
			((Package) targetModelOwner).getOwnedPackages().add(owningPackage);

			_targetModelElements.add(owningPackage);
		}

		// Resolve source and target stereotypes / elements
		NamedElement sourceType = (NamedElement) resolveTargetModelElement(objectProperty.getSrcType());
		NamedElement targetType = (NamedElement) resolveTargetModelElement(objectProperty.getTargetType());

		ValidationRule c = new ValidationRule(objectProperty.getName() + " domain and range validation");

		// Body expression depends on meta-class used - a bit of a hack, but necessary since OLC expression depends on
		// type of meta-class
		// FIXME for some reason, I can't use the fully qualified name - MD gives up after some packages down. Using just the name for now
		// TODO If you change over to qualified names, make sure to escape all special characters with encoded<hex code> (e.g., "-" and ".")
		// TODO For spaces, "_" is fine -> or "encoded20"
		if (targetModelElement.getMetaclass() != null && targetModelElement.getMetaclass().getName().contains("Dependency"))		// Seems to be the only directed relationship meta-class used
			c.setBody("self.source->any(true).oclIsKindOf(" + sourceType.getName().replace(":", "encoded3A").replace(" ", "_").replace("-", "encoded2D").replace(".", "encoded2E") + ") and self.target->any(true).oclIsKindOf(" + targetType.getName().replace(":", "encoded3A").replace(" ", "_").replace("-", "encoded2D").replace(".", "encoded2E") + ")");
		else if (targetModelElement.getMetaclass() != null && targetModelElement.getMetaclass().getName().contains("Association"))
			c.setBody("");			// TODO Not yet supported
		else {
			// If not so obvious, do some deeper checking...
			for (Generalization g : targetModelElement.getAllGeneralizations()) {
				if (g.getGeneral().getName().toLowerCase().contains("dependency"))
					c.setBody("self.source->any(true).oclIsKindOf(" + sourceType.getName().replace(":", "encoded3A").replace(" ", "_").replace("-", "encoded2D").replace(".", "encoded2E") + ") and self.target->any(true).oclIsKindOf(" + targetType.getName().replace(":", "encoded3A").replace(" ", "_").replace("-", "encoded2D").replace(".", "encoded2E") + ")");
			}
		}

		c.setMessage("Wrong source and / or target type. Must be a relationship from '" + sourceType.getName() + "' to '" + targetType.getName() + "'.");

		if (!c.getBody().equals("")) {
			// Add constraint to set of contained constraints
			owningPackage.getConstraints().add(c);

			_targetModelElements.add(c);
		}
		else
			c = null;

		return c;
	}
	
	/**
	 * Maps a scalar data type property to an attribute that is owned by a
	 * stereotype.
	 * 
	 * @param dataTypeProperty
	 * @param targetModelOwner
	 * @return
	 */
	public Attribute dataTypePropertyToAttribute(
			gov.nasa.jpl.imce.profileGenerator.model.bundle.DataTypeProperty dataTypeProperty, 
			Element targetModelOwner) {
		String parsedName = dataTypeProperty.getName();
		
		if (parsedName.contains(":"))
			parsedName = parsedName.substring(parsedName.lastIndexOf(":") + 1);
		
		// Try to get domain (owner)
		Stereotype owningStereotype = (Stereotype) resolveTargetModelElement(dataTypeProperty.getDomain());
		
		Attribute a = new Attribute(parsedName, owningStereotype);
		
		owningStereotype.getAttributes().add(a);
		
		// Now attempt to find range (i.e., set type)
		if (dataTypeProperty.getRange() instanceof gov.nasa.jpl.imce.profileGenerator.model.bundle.XSDPrimitiveType)
			a.setType(
					xsdPrimitiveTypeToUMLPrimitiveType(
							(gov.nasa.jpl.imce.profileGenerator.model.bundle.XSDPrimitiveType) dataTypeProperty.getRange()));
		else
			a.setType((NamedElement) resolveTargetModelElement(dataTypeProperty.getRange()));
		
		_transformedObjects.put(dataTypeProperty, a);
		_targetModelElements.add(a);
		
		return a;
	}
	
	/**
	 * Map a data type property.
	 * <P>
	 * Note that currently only enumerations are supported by the profile generator.
	 * 
	 * @param dataType
	 * @param targetModelOwner
	 * @return
	 */
	public DataType dataTypeToDataType(
			gov.nasa.jpl.imce.profileGenerator.model.bundle.DataType dataType,
			Element targetModelOwner) {
		// TODO This is duplicated code (from stereotype mapping...) - externalize
		// Determine owning package name (e.g., for "mission:Mission" this becomes "mission")
		String packageName = dataType.getName().split(":")[0] + "::datatypes";
		
		// Try to get package (note: elements are unique by qualified name here - so setting name as qualified name (since no owner))
		Package owningPackage = (Package) resolveTargetModelElementByQualifiedName(((Package) targetModelOwner).getQualifiedName() + "::" + packageName);
		
		// If it doesn't exist, create it
		if (owningPackage == null) {
			// FIXME We currently assume that the containing package is already created
			String profilePckgName = dataType.getName().split(":")[0];

			// Try to get package (note: elements are unique by qualified name here - so setting name as qualified name (since no owner))
			Package ontoPackage = (Package) resolveTargetModelElementByQualifiedName(((Package) targetModelOwner).getQualifiedName() + "::" + profilePckgName);

			if (ontoPackage == null) {
				ontoPackage = new Package(profilePckgName, (Package) targetModelOwner);
				((Package) targetModelOwner).getOwnedPackages().add(ontoPackage);

				_targetModelElements.add(ontoPackage);
			}

			owningPackage = new Package("datatypes", ontoPackage);
			ontoPackage.getOwnedPackages().add(owningPackage);
			
			_targetModelElements.add(owningPackage);
		}
		
		// FIXME Assumes enumerations only for now
		Enumeration d = new Enumeration(dataType.getName(), owningPackage);
		
		// For each enumeration literal, create a corresponding owned element
		for (gov.nasa.jpl.imce.profileGenerator.model.bundle.PrimitiveTypeInstance primInst : dataType.getValues())
			// TODO Is type really not necessary?
			d.getLiterals().add(new EnumerationLiteral(primInst.getValue(), d));
		
		_transformedObjects.put(dataType, d);
		_targetModelElements.add(d);
		
		return d;
	}
	
	/**
	 *
	 * @return
	 */
	public NamedElement xsdPrimitiveTypeToUMLPrimitiveType(
			gov.nasa.jpl.imce.profileGenerator.model.bundle.XSDPrimitiveType xsdType) {
		switch(xsdType.getName()) {
		case "int":
		case "integer":
			return new ReferencedElement("PrimitiveTypes::Integer");
			
		case "boolean":
			return new ReferencedElement("PrimitiveTypes::Boolean");
			
		case "string":
			return new ReferencedElement("PrimitiveTypes::String");			// Partial qualified name
																		// FIXME This should be derivable from the URI
		}
		
		System.out.println("[WARN] Unsupported primitive type \"" + xsdType.getName() + "\" found, defaulting to PrimitiveTypes::String.");
		
		// Could be "default:" case in switch, but rather return here so user is informed of unsupported XSD
		return new ReferencedElement("PrimitiveTypes::String");
	}

	/**
	 *
	 * @return
	 */
	private Object resolveTargetModelElementByName(String name) {
		for (Object o : _targetModelElements)
			if (o instanceof NamedElement
					&& ((NamedElement) o).getName().equals(name))
				return o;
		
		// First, check whether this is a previously transformed element
		for (Object resolvedElement : _transformedObjects.values())
			if (resolvedElement instanceof NamedElement
					&& ((NamedElement) resolvedElement).getName().equals(name))
				return resolvedElement;
		
		return null;
	}
	
	/**
	 *
	 * @return
	 */
	private Object resolveTargetModelElementByQualifiedName(String qualifiedName) {
		for (Object o : _targetModelElements)
			if (o instanceof NamedElement
					&& ((NamedElement) o).getQualifiedName().equals(qualifiedName))
				return o;
		
		// First, check whether this is a previously transformed element
		for (Object resolvedElement : _transformedObjects.values())
			if (resolvedElement instanceof NamedElement
					&& ((NamedElement) resolvedElement).getQualifiedName().equals(qualifiedName))
				return resolvedElement;
		
		return null;
	}
	
	/**
	 * 
	 * @param objectToResolve
	 * @return
	 */
	private Object resolveTargetModelElement(Object objectToResolve) {
		// First, check whether this is a previously transformed element
		Object resolvedElement = _transformedObjects.get(objectToResolve);
		
		if (resolvedElement == null)
			if (_targetModelElements.contains(objectToResolve))
				for (Object o : _targetModelElements)
					if (o.equals(objectToResolve))
						return o;
		
		// If the target element is an element of the SysML meta-model, translate this
		// NOTE This is a result of a change in the LoadProd input - SysML-metamodel:Component.Block etc are no longer stereotypes
		if (isSysMLStereotype((gov.nasa.jpl.imce.profileGenerator.model.bundle.NamedElement) objectToResolve))
			resolvedElement = new ReferencedElement(
					getSysMLElementName((gov.nasa.jpl.imce.profileGenerator.model.bundle.NamedElement) objectToResolve));
		
		if (resolvedElement == null)
			resolvedElement = new ReferencedElement(
					((gov.nasa.jpl.imce.profileGenerator.model.bundle.NamedElement) objectToResolve).getName());
		
		return resolvedElement;
	}
	
	/**
	 * 
	 * @param clazz
	 * @return
	 */
	private boolean isMetaclass(gov.nasa.jpl.imce.profileGenerator.model.bundle.NamedElement clazz) {
		if (clazz.getName().startsWith("UML-metamodel:"))
			return true;
		
		return false;
	}
	
	/**
	 * 
	 * @param clazz
	 * @return
	 */
	private String getMetaclassName(gov.nasa.jpl.imce.profileGenerator.model.bundle.NamedElement clazz) {
		if (isMetaclass(clazz))
			return clazz.getName().substring(clazz.getName().indexOf(":") + 1);
		
		return null;
	}

	/**
	 * Perform a breadth-first search to identify the most immediate meta-class from the specified element.
	 *
	 * @param clazz
	 * @return
	 */
	private String determineMetaclassName(gov.nasa.jpl.imce.profileGenerator.model.bundle.NamedElement clazz) {
		if (isMetaclass(clazz))
			return getMetaclassName(clazz);

		// Need to do a breadth-first search to find "lowest" meta-class in tree
		Queue<gov.nasa.jpl.imce.profileGenerator.model.bundle.Generalization> generalizations = new SynchronousQueue<>();
		generalizations.addAll(clazz.getGeneralization());

		for (gov.nasa.jpl.imce.profileGenerator.model.bundle.Generalization g : generalizations) {
			generalizations.addAll(g.getGeneral().getGeneralization());

			String mcName = determineMetaclassName(g.getGeneral());

			if (mcName != null)
				return mcName;
		}

		return null;
	}

	/**
	 * Perform a breadth-first search to identify the most immediate meta-class from the specified element.
	 *
	 * @param stereotype
	 * @return
	 */
	private String determineMetaclassName(Stereotype stereotype) {
		if (stereotype == null)
			return null;

		if (stereotype.getMetaclass() != null)
			return stereotype.getMetaclass().getName();

		// Need to do a breadth-first search to find "lowest" meta-class in tree
		Queue<Generalization> generalizations = new LinkedList<>();
		generalizations.addAll(stereotype.getGeneralization());

		while (!generalizations.isEmpty()) {
			Generalization g = generalizations.remove();

			generalizations.addAll(g.getGeneral().getGeneralization());

			if (g.getGeneral() instanceof Stereotype) {
				String mcName = determineMetaclassName((Stereotype) g.getGeneral());

				if (mcName != null)
					return mcName;
			}
		}

		return null;
	}
	
	/**
	 * 
	 * @param clazz
	 * @return
	 */
	private boolean isSysMLStereotype(gov.nasa.jpl.imce.profileGenerator.model.bundle.NamedElement clazz) {
		if (clazz == null)
			return false;

		if (clazz.getName() == null)
			return false;

		if (clazz.getName().startsWith("SysML-metamodel:"))
			return true;
		
		return false;
	}
	
	/**
	 * 
	 * @param clazz
	 * @return
	 */
	private String getSysMLElementName(gov.nasa.jpl.imce.profileGenerator.model.bundle.NamedElement clazz) {
		if (isSysMLStereotype(clazz)) {
			String sysmlStereotype = clazz.getName().substring(clazz.getName().indexOf(":") + 1);
			
			if (sysmlStereotype.contains("."))
				sysmlStereotype = sysmlStereotype.substring(sysmlStereotype.lastIndexOf(".") + 1);
			
			return sysmlStereotype;
		}
		
		return null;
	}
	
	/**
	 * Determines whether or not a particular element symbolizes the need for an
	 * extension to be added.
	 * <P>
	 * For instance, inheriting from SysML-metamodel:Class.Block means that the 
	 * element should extend UML-metamodel:Class, and specialize 
	 * SysML-metamodel:Block.
	 * 
	 * @param clazz
	 * @return
	 */
	private boolean symbolizesRequiredExtension(gov.nasa.jpl.imce.profileGenerator.model.bundle.NamedElement clazz) {
		if (clazz.getName().startsWith("SysML-metamodel:")
				&& clazz.getName().contains("."))
			return true;
		
		return false;
	}
	
	/**
	 * 
	 * @param clazz
	 * @return
	 */
	private String getSymbolizedRequiredExtension(gov.nasa.jpl.imce.profileGenerator.model.bundle.NamedElement clazz) {
		if (!symbolizesRequiredExtension(clazz))
			return null;
		
		String[] el = clazz.getName().split(":");
		
		// Ensure that well-formed
		if (el.length != 2)
			return null;
		
		String extendFrom = el[1].split("\\.")[0];		// split takes regex as argument - escape "dot" character since symbol for any character
		
		return extendFrom;
	}
	
}