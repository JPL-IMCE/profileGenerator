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

import java.io.File;
import java.util.*;

import com.nomagic.ci.persistence.IAttachedProject;
import com.nomagic.ci.persistence.decomposition.ProjectAttachmentConfiguration;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.core.modules.ModulesService;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.core.project.ProjectDescriptorsFactory;
import com.nomagic.magicdraw.uml.ClassTypes;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Enumeration;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.components.mdbasiccomponents.Component;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Extension;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.ExtensionEnd;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.eclipse.emf.common.util.URI;

/**
 * @author sherzig
 *
 */
public class MDUMLModelUtils extends MDModelUtils {

	/**
	 *
	 * @param name
	 * @param owner
     * @return
     */
	public static Class createClass(String name, Element owner) {
		System.out.println("[CREATE::Class] " + name + " (owner: " + owner.toString() + ")");

		Class c = getElementsFactory().createClassInstance();
		c.setName(name);

		addToModel(c, owner);

		return c;
	}

	/**
	 *
	 * @param name
	 * @param owner
     * @return
     */
	public static Constraint createConstraint(String name, Element owner) {
		System.out.println("[CREATE::Constraint] " + name + " (owner: " + owner.toString() + ")");

		Constraint c = getElementsFactory().createConstraintInstance();
		c.setName(name);

		addToModel(c, owner);

		return c;
	}

	/**
	 *
	 * @param general
	 * @param specific
	 * @param owner
	 * @return
	 */
	public static Generalization createGeneralization(Classifier general, Classifier specific, Element owner) {
		System.out.println("[CREATE::Generalization] " + general.getName() + " - " + specific.getName() + " (owner: " + owner.toString() + ")");

		Generalization g = getElementsFactory().createGeneralizationInstance();
		g.setGeneral(general);
		g.setSpecific(specific);

		addToModel(g, owner);

		return g;
	}

	/**
	 *
	 * @param name
	 * @param owner
	 * @return
	 */
	public static Package createPackage(String name, Element owner) {
		System.out.println("[CREATE::Package] " + name + " (owner: " + owner.toString() + ")");

		Package p = getElementsFactory().createPackageInstance();
		p.setName(name);

		addToModel(p, owner);

		return p;
	}

	/**
	 *
	 * @param name
	 * @param owner
	 * @return
	 */
	public static Profile createProfile(String name, Element owner) {
		System.out.println("[CREATE::Profile] " + name + " (owner: " + owner.toString() + ")");

		Profile p = getElementsFactory().createProfileInstance();
		p.setName(name);

		addToModel(p, owner);

		return p;
	}

	/**
	 *
	 * @param name
	 * @param isAbstract
	 * @param owner
	 * @return
	 */
	public static Stereotype createStereotype(String name, boolean isAbstract, Element owner) {
		System.out.println("[CREATE::Stereotype] " + name + " (owner: " + owner.toString() + ")");

		Stereotype s = getElementsFactory().createStereotypeInstance();
		s.setName(name);
		s.setAbstract(isAbstract);

		addToModel(s, owner);

		return s;
	}

	/**
	 *
	 * @param name
	 * @param owner
     * @return
     */
	public static Enumeration createEnumeration(String name, Element owner) {
		System.out.println("[CREATE::Enumeration] " + name + " (owner: " + owner.toString() + ")");

		Enumeration e = getElementsFactory().createEnumerationInstance();
		e.setName(name);

		addToModel(e, owner);

		return e;
	}

	/**
	 *
	 * @param name
	 * @param owner
     * @return
     */
	public static EnumerationLiteral createEnumerationLiteral(String name, Enumeration owner) {
		System.out.println("[CREATE::EnumerationLiteral] " + name + " (owner: " + owner.toString() + ")");

		EnumerationLiteral l = getElementsFactory().createEnumerationLiteralInstance();
		l.setName(name);

		addToModel(l, owner);

		return l;
	}

	/**
	 *
	 * @param name
	 * @param owner
	 * @return
	 */
	public static Component createComponent(String name, Element owner) {
		System.out.println("[CREATE::Component] " + name + " (owner: " + owner.toString() + ")");

		Component c = getElementsFactory().createComponentInstance();
		c.setName(name);

		addToModel(c, owner);

		return c;
	}

	/**
	 *
	 * @param name
	 * @param type
	 * @param owner
     * @return
     */
	public static Property createProperty(String name, Type type, Element owner) {
		if (type != null)
			System.out.println("[CREATE::Property] " + name + " (type: " + type.toString() + "; owner: " + owner.toString() + ")");
		else
			System.out.println("[CREATE::Property] " + name + " (type: null; owner: " + owner.toString() + ")");

		Property p = getElementsFactory().createPropertyInstance();
		p.setName(name);
		p.setVisibility(VisibilityKindEnum.PUBLIC);
		
		if (type != null)
			p.setType(type);

		addToModel(p, owner);

		return p;
	}

	/**
	 *
	 * @param element
	 * @param documentation
	 * @return
	 */
	public static Comment createDocumentationAnnotation(Element element, String documentation) {
		if (documentation == null || documentation.equals(""))
			return null;

		System.out.println("[CREATE::DocumentationAnnotation] " + element.toString());

		Comment c = getElementsFactory().createCommentInstance();
		c.setBody("<html>" + documentation + "</html>");
		c.setOwner(element);
		c.getAnnotatedElement().add(element);

		return c;
	}

	/**
	 * Creates a meta-class extension for a specific stereotype.
	 * <P>
	 * Internally, this function will set the base class of the stereotype, and will
	 * rely on MagicDraw's OpenAPI to create the appropriate properties and extension
	 * relationship. Returned will be the extension associated with the particular
	 * meta-class that was specified.
	 *
	 * @param stereotype The stereotype to set the meta-class extension for.
	 * @param metaClass The meta-class to extend.
	 * @return The newly created extension.
	 */
	public static Extension createMetaClassExtension(Stereotype stereotype, Class metaClass) {
		System.out.println("[CREATE::MetaClassExtension] " + stereotype.getName() + " (metaClass: " + metaClass.getName() + ")");

		Collection<Class> metaClasses = new HashSet<Class>();
		metaClasses.add(metaClass);

		StereotypesHelper.setBaseClasses(stereotype, metaClasses);

		// Set extension name, since empty by default and required to be set by exporter
		StereotypesHelper.getExtension(stereotype, metaClass).setName("E_base_" + metaClass.getName() + "_extension_" + stereotype.getName());

		return StereotypesHelper.getExtension(stereotype, metaClass);
	}

	/**
	 * Creates an opaque expression element.
	 *
	 * @param name
	 * @param owner
     * @return
     */
	public static OpaqueExpression createOpaqueExpression(String name, String language, String body, Element owner) {
		System.out.println("[CREATE::OpaqueExpression] " + name + " (owner: " + owner.toString() + ")");

		OpaqueExpression o = getElementsFactory().createOpaqueExpressionInstance();
		o.setName(name);
		o.getBody().add(body);
		o.getLanguage().add(language);

		return o;
	}

	/**
	 * Performs a BFS to find all inherited elements (all generals).
	 * <P>
	 * Note that a similar function used to be in the MagicDraw 18.0 API. However,
	 * this function is deprecated and no longer available from 18.2.
	 *
	 * @param element
	 * @return
	 */
	public static Collection<Classifier> getAllGenerals(Classifier element) {
		Collection<Classifier> generals = new HashSet<Classifier>();
		Queue<Classifier> queue = new LinkedList<Classifier>();

		if (element.getGeneral() != null) {
			queue.addAll(element.getGeneral());
			generals.addAll(element.getGeneral());

			while(!queue.isEmpty()) {
				Classifier queueElement = queue.remove();

				for (Classifier g : queueElement.getGeneral()) {
					if (g.getGeneral() != null) {
						queue.addAll(g.getGeneral());
						generals.addAll(g.getGeneral());
					}
				}
			}
		}

		return generals;
	}

	/**
	 * Create a map of element name to applied stereotype for each owned element in the model
	 * @param elements
	 * @return
	 **/
	public static Map<String, String> getElementStereotypeMapping(List<Element> elements) {
		Map<String, String> elementStereotypeMap = new HashMap<String, String>();
		try {
			for (Element element : elements) {
				if (element instanceof NamedElement) {
					String elementName = ((NamedElement) element).getName();
					if (element instanceof Package || element instanceof Component) {
						InstanceSpecification instanceSpecification = element.getAppliedStereotypeInstance();
						if (instanceSpecification != null) {
							List<Classifier> classifiers = instanceSpecification.getClassifier();
							for (Classifier classifier : classifiers) {
								String classifierName = classifier.getName();
								elementStereotypeMap.put(elementName, classifierName);
							}
						}
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println(e.toString());
		}
		return elementStereotypeMap;
	}

  /**
	 * Get all owned elements of the provided parent element
	 * @param parent
	 * @return
	 **/
	public static List<Element> getAllOwnedElements(Element parent) {
		List<Element> result = new ArrayList<Element>();
		getAllOwnedElementsInner(parent, result);
		return result;
	}

	/**
	 * Recursive helper function for getAllOwnedElements(...)
	 * @param parent
	 * @param allElements
	 * @return
	 **/
	public static void getAllOwnedElementsInner(Element parent, List<Element> allElements) {
		for (Element child: parent.getOwnedElement()) {
			allElements.add(child);
			getAllOwnedElementsInner(child, allElements);
		}
	}
	
	
	/**
	 * Returns the owned extension end for a particular UML Extension.
	 *  
	 * @param extension The extension to retrieve the extension end for
	 * @return The extension end
	 */
	public static ExtensionEnd getOwnedExtensionEnd(Extension extension) {
		return StereotypesHelper.getExtensionEnd(extension);
	}

	/**
	 *
	 * @return
	 */
	public static Class getMetaElementClass() {
		return getMetaClassByName("Class");
	}

	/**
	 *
	 * @return
	 */
	public static Class getMetaElementComponent() {
		return getMetaClassByName("Component");
	}

	/**
	 *
	 * @return
	 */
	public static Class getMetaElementDependency() {
		return getMetaClassByName("Dependency");
	}

	/**
	 *
	 * @return
	 */
	public static Class getMetaElementPort() {
		return getMetaClassByName("Port");
	}

	/**
	 *
	 * @return
	 */
	public static Class getMetaElementProperty() {
		return getMetaClassByName("Property");
	}

	/**
	 *
	 * @return
	 */
	public static Class getMetaElementAssociation() {
		return getMetaClassByName("Association");
	}

	/**
	 *
	 * @return
	 */
	public static Class getMetaElementNamedElement() {
		return getMetaClassByName("NamedElement");
	}

	/**
	 *
	 * @return
	 */
	public static Class getMetaElementClassifier() {
		return getMetaClassByName("Classifier");
	}

	/**
	 *
	 * @param name
	 * @return
	 */
	public static Class getMetaClassByName(String name) {
		return StereotypesHelper.getMetaClassByName(getActiveProject(), name);
	}

	/**
	 *
	 * @param element
	 * @param stereotypeName
	 */
	public static Stereotype applyStereotypeByName(Element element, String stereotypeName) {
		// FIXME Method is deprecated - should use method with profile
		Stereotype stereotype = StereotypesHelper.getStereotype(getActiveProject(), stereotypeName);

		StereotypesHelper.addStereotype(element, stereotype);

		return stereotype;
	}

	/**
	 * Set the constraint to apply to a particular element.
	 *
	 * @param element
	 * @param c
     * @return
     */
	public static void applyConstraint(Element element, Constraint c) {
		c.getConstrainedElement().add(element);
	}

	/**
	 *
	 * @param element
	 * @param stereotype
	 * @param propertyName
	 * @param propertyValue
     */
	public static void setStereotypePropertyValue(Element element, Stereotype stereotype, String propertyName, Object propertyValue) {
		StereotypesHelper.setStereotypePropertyValue(element, stereotype,
				propertyName, propertyValue);
	}

	/**
	 *
	 * @param pkg
	 */
	public static void sharePackage(Package pkg) {
		List<Package> sharedPackages = new ArrayList<Package>();

		// Get all currently shared packages
		sharedPackages.addAll(ProjectUtilities.getSharedPackages(
				getActiveProject().getPrimaryProject()));

		// Add newly to be shared project
		sharedPackages.add(pkg);

		// Set list of shared projects
		Application.getInstance().getProjectsManager().sharePackage(
				getActiveProject(), sharedPackages, "");
	}

	/**
	 *
	 * @param expPackage
	 * @param filename
	 */
	public static void exportModule(Package expPackage, String filename) {
		File file = new File(filename);
		ProjectDescriptor projectDescriptor =
				ProjectDescriptorsFactory.createProjectDescriptor(file.toURI());

		// Export collection of packages as module
		try {
			// Also mark project usage relationship as shared (otherwise PUIC will complain)
			Collection<IAttachedProject> attachedProjects = ProjectUtilities.getAllAttachedProjects(Application.getInstance().getProjectsManager().getActiveProject());

			for (IAttachedProject attachedProject : attachedProjects)
				ModulesService.setReSharedOnTask(Application.getInstance().getProjectsManager().getActiveProject().getPrimaryProject(), attachedProject, true);

			Application.getInstance().getProjectsManager().exportModule(
					getActiveProject(),
					Arrays.asList(expPackage),
					expPackage.getName(),
					projectDescriptor);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param filename
     */
	public static IAttachedProject mountProfile(String filename) {
		File file = new File(filename);

		// TODO Nicer error handling...
		if (!file.exists()) {
			System.out.println("[CRITICAL] Could not mount profile; tried loading " + filename);
			return null;
		}

		ProjectDescriptor projectDescriptor =
				ProjectDescriptorsFactory.createProjectDescriptor(file.toURI());
		URI uri = ProjectUtilities.getEMFURI(projectDescriptor.getURI());
		ProjectAttachmentConfiguration cfg = ProjectUtilities.createDefaultProjectAttachmentConfiguration(uri);

		IAttachedProject module = null;

		// Mount profile
		try {
			// Returns a boolean for success
			// Look at exporter: batchExportOTIDocumentSets2OMFONtologies
			//Application.getInstance().getProjectsManager().useModule(
			//		getActiveProject(),
			//		projectDescriptor);

			module = ModulesService.attachModuleOnTask(MDUtils.getActiveProject().getPrimaryProject(), cfg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return module;
	}

	/**
	 *
	 */
	public static void unmountProfile(IAttachedProject module) {
		// Mount profile
		try {
			// removeModule -> ModulesService (magicdraw.core.modules)
			//Application.getInstance().getProjectsManager().unloadModule(
			//		getActiveProject(),
			//		projectDescriptor);
			Set<IAttachedProject> modules = new HashSet<IAttachedProject>();
			modules.add(module);

			ModulesService.removeModulesOnTask(modules, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param name
	 * @return
	 */
	public static Element findElementByName(String name) {
		return findElementByNameAndType(name, NamedElement.class);
	}

	/**
	 *
	 * @param name
	 * @param searchRoot
	 * @return
	 */
	public static Element findElementByName(String name, Element searchRoot) {
		return findElementByNameAndType(name, NamedElement.class, searchRoot);
	}

	/**
	 *
	 * @param name
	 * @param type
	 * @return
	 */
	public static Element findElementByNameAndType(String name, java.lang.Class type) {
		return findElementByNameAndType(name, type, getActiveProject().getModel());
	}

	/**
	 *
	 * @param name
	 * @param type
	 * @param searchRoot
	 * @return
	 */
	public static Element findElementByNameAndType(String name, java.lang.Class type, Element searchRoot) {
		java.lang.Class<?>[] types = ClassTypes.getSubtypesArray(type);

		for (Element e : ModelHelper.getElementsOfType(searchRoot, types, true, true)) {
			// "Guaranteed" to be NamedElements
			NamedElement n = (NamedElement) e;

			if (n.getName().equals(name))
				return e;
		}

		System.out.println("ERROR Could not find " + name + " of type " + type.getCanonicalName());

		return null;
	}

	/**
	 *
	 * @param qualifiedName
	 * @return
	 */
	public static Element findElementByQualifiedName(String qualifiedName) {
		return findElementByQualifiedNameAndType(qualifiedName, NamedElement.class);
	}

	/**
	 *
	 * @param qualifiedName
	 * @param searchRoot
	 * @return
	 */
	public static Element findElementByQualifiedName(String qualifiedName, Element searchRoot) {
		return findElementByQualifiedNameAndType(qualifiedName, NamedElement.class, searchRoot);
	}

	/**
	 *
	 * @param qualifiedName
	 * @param type
	 * @return
	 */
	public static Element findElementByQualifiedNameAndType(String qualifiedName, java.lang.Class type) {
		return findElementByQualifiedNameAndType(qualifiedName, type, getActiveProject().getModel());
	}

	/**
	 *
	 * @param qualifiedName
	 * @param type
	 * @param searchRoot
	 * @return
	 */
	public static Element findElementByQualifiedNameAndType(String qualifiedName, java.lang.Class type, Element searchRoot) {
		java.lang.Class<?>[] types = ClassTypes.getSubtypesArray(type);

		for (Element e : ModelHelper.getElementsOfType(searchRoot, types, true, true)) {
			// "Guaranteed" to be NamedElements
			NamedElement n = (NamedElement) e;

			if (n.getQualifiedName().equals(qualifiedName))
				return e;
		}

		// If nothing found, repeat with just trying to find endsWith
		for (Element e : ModelHelper.getElementsOfType(searchRoot, types, true, true)) {
			// "Guaranteed" to be NamedElements
			NamedElement n = (NamedElement) e;

			if (n.getQualifiedName().endsWith("::" + qualifiedName))
				return e;
		}

		// If STILL nothing found, try without the "::" (could be a root element?)
		for (Element e : ModelHelper.getElementsOfType(searchRoot, types, true, true)) {
			// "Guaranteed" to be NamedElements
			NamedElement n = (NamedElement) e;

			if (n.getQualifiedName().endsWith(qualifiedName))
				return e;
		}

		System.out.println("Element " + qualifiedName + " of type " + type.getCanonicalName() + " does not yet exist in model");

		return null;
	}

}