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
package gov.nasa.jpl.imce.profileGenerator.util;

/**
 * @author sherzig
 *
 */
public class MDElementIDGenerator {
	
	/**
	 * Construct a URI according the specification of the previous profile
	 * generator.
	 *
	 * @param element
	 * @return
	 */
	public static String constructID(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.PackageableElement element) {
		// FIXME This can't be the full spec... where does PackageableElement come from?
		// _project-bundle_project_PackageableElement-project_u003aWorkPackage_PackageableElement
		// Seems to be a URI -> go through owners until hit profile
		if (element instanceof com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile)
			return "_" + getReproducibleElementSubID(element);
		
		if (element.getOwner() != null)
			// Identical except for "-" vs "_"
			if (element.getOwner() instanceof com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile)
				return constructID((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.PackageableElement) element.getOwner()) + "_" + getReproducibleElementSubID(element);
			else
				return constructID((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.PackageableElement) element.getOwner()) + "-" + getReproducibleElementSubID(element);
				
		return "";
	}
	
	/**
	 * 
	 * @param element
	 * @return
	 */
	public static String constructID(com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile profile) {
		// FIXME Where does the "_0" come from? Representation of "null"?
		return "_" + getReproducibleElementSubID(profile) + "__0";
	}
	
	/**
	 * 
	 * @param element
	 * @return
	 */
	public static String constructID(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.PackageImport packageImport) {
		// FIXME Why is exactly just the owning package of the last element contained? (and, e.g., not "IMCE" package)
		//_project-bundle_packageImport__PackageImport._project-bundle__0._OWL2-SysML_Integration_IMCE.owl2-mof2_PackageableElement
		// <bundle-name>_packageImport__PackageImport.<importing-namespace-name>.<imported-package>
		String id = getReproducibleElementSubID(packageImport.getOwner());
		id += "_packageImport__PackageImport.";
		id += getReproducibleElementSubID(packageImport.getImportingNamespace()) + ".";
		id += ((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement) packageImport.getImportedPackage().getOwner()).getName() + "_" + getReproducibleElementSubID(packageImport.getImportedPackage());
		
		return id;
	}

	/**
	 * 
	 * @param element
	 * @return
	 */
	public static String constructID(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization generalization) {
		// Example:
		// _project-bundle_analysis_PackageableElement-analysis_u003aAnalysis_PackageableElement-generalization__Generalization._project-bundle_analysis_PackageableElement-analysis_u003aAnalysis_PackageableElement._project-bundle_analysis_PackageableElement-analysis_u003aExplanation_PackageableElement
		// <path-to-generalization>.<path-to-specific>.<path-to-general>
		String id = constructID((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.PackageableElement) generalization.getOwner()) + "-generalization__Generalization";
		id += ".";
		id += constructID((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.PackageableElement) generalization.getSpecific());
		id += ".";
		id += constructID((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.PackageableElement) generalization.getGeneral());
		
		return id;
	}

	/**
	 * 
	 * @param element
	 * @return
	 */
	public static String constructID(com.nomagic.uml2.ext.magicdraw.mdprofiles.ExtensionEnd extensionEnd) {
		//_project-bundle_base_PackageableElement-E_extension_base_u003aPackage_base_Package_PackageableElement-extension_base_u003aPackage_Property
		return constructID((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property) extensionEnd);
	}
	
	/**
	 * 
	 * @param property
	 * @return
	 */
	public static String constructID(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property property) {
		//_project-bundle_base_PackageableElement-E_extension_base_u003aPackage_base_Package_PackageableElement-extension_base_u003aPackage_Property
		return constructID((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.PackageableElement) property.getOwner()) + "-" + getReproducibleElementSubID(property);
	}
	
	/**
	 * 
	 * @param element
	 * @return
	 */
	public static String constructID(com.nomagic.uml2.ext.magicdraw.mdprofiles.Extension extension) {
		return constructID((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.PackageableElement) extension);
	}
	
	/**
	 * 
	 * @param element
	 * @return
	 */
	public static String constructID(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification valueSpec) {
		// _project-bundle_base_PackageableElement-E_extension_base_u003aPackage_base_Package_PackageableElement-extension_base_u003aPackage_Property-upperValue__ValueSpecification
		return constructID((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.PackageableElement) valueSpec.getOwner()) + "-" + getReproducibleElementSubID(valueSpec);
	}
	
	/**
	 * 
	 * @param pckg
	 * @return
	 */
	public static String getReproducibleElementSubID(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element element) {
		// Constructed slightly differently, depending on element
		// FIXME Clean up
		if (element instanceof com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property)
			return ((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement) element).getName().replace(":",  "_u003a") + "_Property";
		else if (element instanceof com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile)
			return ((com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile) element).getName().replace(":",  "_u003a");
		else if (element instanceof com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification)
			return ((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement) element).getName().replace(":",  "_u003a") + "_ValueSpecification";
		else if (element instanceof com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DataType)
			return ((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement) element).getName().replace(":",  "_u003a") + "_DataType";
		else if (element instanceof com.nomagic.uml2.ext.magicdraw.classes.mdkernel.PackageableElement)
			return ((com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement) element).getName().replace(":",  "_u003a") + "_PackageableElement";
		
		return "";
	}
	
}