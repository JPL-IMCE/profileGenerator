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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import gov.nasa.jpl.imce.profileGenerator.model.profile.Attribute;
import gov.nasa.jpl.imce.profileGenerator.model.profile.DataType;
import gov.nasa.jpl.imce.profileGenerator.model.profile.Element;
import gov.nasa.jpl.imce.profileGenerator.model.profile.Enumeration;
import gov.nasa.jpl.imce.profileGenerator.model.profile.EnumerationLiteral;
import gov.nasa.jpl.imce.profileGenerator.model.profile.Generalization;
import gov.nasa.jpl.imce.profileGenerator.model.profile.MetaClass;
import gov.nasa.jpl.imce.profileGenerator.model.profile.NamedElement;
import gov.nasa.jpl.imce.profileGenerator.model.profile.Package;
import gov.nasa.jpl.imce.profileGenerator.model.profile.Profile;
import gov.nasa.jpl.imce.profileGenerator.model.profile.ReferencedElement;
import gov.nasa.jpl.imce.profileGenerator.model.profile.Stereotype;

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
		// Initial package structure
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
		
		// Last element is actual profile package
		Profile p = new Profile(profilePackage, owner);
		owner.getOwnedPackages().add(p);
		
		_targetModelElements.add(p);
		
		// Map data types
		for (gov.nasa.jpl.imce.profileGenerator.model.bundle.DataType d : bundle.getDataTypes()) {
			dataTypeToDataType(d, p);
		}
		
		// Map classes
		for (gov.nasa.jpl.imce.profileGenerator.model.bundle.Class c : bundle.getClasses()) {
			classToStereotype(c, p);
		}
		
		// Map object properties
		for (gov.nasa.jpl.imce.profileGenerator.model.bundle.ObjectProperty o : bundle.getObjectProperties()) {
			objectPropertyToStereotype(o, p);
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
		
		return root;
	}
	
	/**
	 * 
	 * @param clazz
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
	 * 
	 * @param clazz
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
		String packageName = dataType.getName().split(":")[0];
		
		// Try to get package (note: elements are unique by qualified name here - so setting name as qualified name (since no owner))
		Package owningPackage = (Package) resolveTargetModelElementByQualifiedName(((Package) targetModelOwner).getQualifiedName() + "::" + packageName);
		
		// If it doesn't exist, create it
		if (owningPackage == null) {
			owningPackage = new Package(packageName, (Package) targetModelOwner);
			((Package) targetModelOwner).getOwnedPackages().add(owningPackage);
			
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
	 * @param range
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
	 * @param objectToResolve
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
	 * @param objectToResolve
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
	 * 
	 * @param clazz
	 * @return
	 */
	private boolean isSysMLStereotype(gov.nasa.jpl.imce.profileGenerator.model.bundle.NamedElement clazz) {
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