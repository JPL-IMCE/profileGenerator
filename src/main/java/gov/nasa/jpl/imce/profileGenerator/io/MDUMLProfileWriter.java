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

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.MDCounter;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import gov.nasa.jpl.imce.profileGenerator.model.profile.Profile;
import gov.nasa.jpl.imce.profileGenerator.model.profile.Package;
import gov.nasa.jpl.imce.profileGenerator.model.profile.Attribute;
import gov.nasa.jpl.imce.profileGenerator.model.profile.DataType;
import gov.nasa.jpl.imce.profileGenerator.model.profile.Enumeration;
import gov.nasa.jpl.imce.profileGenerator.model.profile.EnumerationLiteral;
import gov.nasa.jpl.imce.profileGenerator.model.profile.Generalization;
import gov.nasa.jpl.imce.profileGenerator.model.profile.Stereotype;
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
 * @author Sebastian.J.Herzig@jpl.nasa.gov
 */
public class MDUMLProfileWriter {

	/** */
	private Element _profile = null;

	/** */
	private HashMap<Object,Element> _mappings = new HashMap<Object,Element>();

	/**
	 *
	 * @param profilePackage
	 * @return
	 */
	public Element writeModel(Package profilePackage) {
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

			_mappings.put(pkg, mdPackage);
		}

		for (DataType d : pkg.getDataTypes())
			writeDataType(d, mdPackage);
		
		for (Stereotype s : pkg.getStereotypes())
			writeStereotype(s, mdPackage);

		for (Package subPkg : pkg.getOwnedPackages())
			if (subPkg instanceof Profile)
				writeProfile((Profile) subPkg, mdPackage);
			else
				writePackage(subPkg, mdPackage);

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

			// IMCE profile bundles have a special stereotype applied
			// FIXME Should become part of profile model
			MDUMLModelUtils.applyStereotypeByName(mdProfile, "owl2-mof2:BundledOntologyProfile");
			MDUMLModelUtils.applyStereotypeByName(mdProfile, "auxiliaryResource");
			// --> Change to apply stereotype with a referenced element!!

			MDUMLModelUtils.sharePackage((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package) mdProfile);

			_mappings.put(profile, mdProfile);
		}

		for (DataType d : profile.getDataTypes())
			writeDataType(d, mdProfile);
		
		for (Stereotype s : profile.getStereotypes())
			writeStereotype(s, mdProfile);

		for (Package subPkg : profile.getOwnedPackages())
			if (subPkg instanceof Profile)
				writeProfile((Profile) subPkg, mdProfile);
			else
				writePackage(subPkg, mdProfile);

		this._profile = mdProfile;

		return mdProfile;
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
	private Profile findProfile(Package pkg) {
		if (pkg instanceof Profile)
			return (Profile) pkg;

		for (Package p : pkg.getOwnedPackages())
			if (p instanceof Profile)
				return (Profile) p;
			else
				return findProfile(p);

		// No profile found
		return null;
	}

	/**
	 *
	 * @param pkg
	 * @param owner
	 * @return
	 */
	// FIXME Dangerous - if more than one package per hierarchy level, this will not return the right package
	private Element createPackageStructure(Package pkg, Element owner) {
		Element newOwner = MDUMLModelUtils.findElementByName(pkg.getName());

		if (newOwner == null)
			newOwner = MDUMLModelUtils.createPackage(pkg.getName(), owner);

		if (pkg.getOwnedPackages().size() > 0)
			for (Package ownedPackage : pkg.getOwnedPackages())
				if (!(ownedPackage instanceof Profile))
					return createPackageStructure(ownedPackage, newOwner);

		return newOwner;
	}

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

		String exportedModuleFilename =
				"dynamicScripts/gov.nasa.jpl.imce.profileGenerator/" +
				((com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile) _profile).getName() + ".mdzip";

		// Mount PUIC profile
		PUICUtils.mountPUICProfile();

		// Invoke PUIC to repair project
		PUICUtils.repairProject();

		MDUMLModelUtils.exportModule((com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile) _profile,
				exportedModuleFilename);

		// Probably out of class
		//MDUtils.saveProject();

		// Load project
		MDUtils.loadProject(exportedModuleFilename);

		// Mount PUIC profile
		PUICUtils.mountPUICProfile();

		// Invoke PUIC to repair project
		PUICUtils.repairProject();

		// Probably out of class
		MDUtils.saveProject();

		// Unmount profile
		PUICUtils.unmountPUICProfile();

		// Save project
		MDUtils.saveProject();

		System.out.println("!!!! DONE !!!!");

		if (Configuration.silent) {
			MDUtils.shutdownMagicDraw();
		}
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