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

import java.util.Collection;
import java.util.HashSet;

import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.TypedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.ProfileApplication;

/**
 * @author sherzig
 *
 */
public class ModelGarbageCollector {

	private Model model = null;
	
	public static void main(String[] args) {
		String[] args1 = {"-verbose"};
		MDUtils.launchMagicDraw(args1);
		
		/***** WHEN USING TEAMWORK: SAVE LOCAL! BUG IN CODE: EVERYTHING MARKED NOT EDITABLE (CHECK SPEC) *****/
		
		/** REQUIRES BREAKPOINT HERE! LOAD MODEL MANUALLY AT THIS POINT **/
		ModelGarbageCollector garbageCollector = new ModelGarbageCollector(MDModelUtils.getActiveProject().getModel());
		garbageCollector.proposeFix();
	}
	
	public ModelGarbageCollector(Model model) {
		this.model = model;
	}
	
	public void proposeFix() {
		// Find elements that are not used anywhere (similar to -> Used By... option)
		Collection<Element> unusedElements = findUnusedElements(this.model);
		
		System.out.printf("Num potentially unused elements: %d\n", unusedElements.size());
		
		for (Element e : unusedElements)
			if (e instanceof NamedElement)
				System.out.println(((NamedElement) e).getQualifiedName() + " (type: " + e.getClassType().getName() + ")");
			else
				System.out.println(e);
		
		// Remove
		
		// Repeat until no more elements found
	}
	
	private Collection<Element> findUnusedElements(Element startingPoint) {
		Collection<Element> elements = new HashSet<Element>();
		
		if (!startingPoint.isEditable())
			return elements;
		
		if (startingPoint instanceof Package)
			System.out.println("Checking " + ((Package) startingPoint).getQualifiedName());
		
		// First check whether it's a relevant element
		if (!(startingPoint instanceof Package)
				&& !(startingPoint instanceof ProfileApplication)
				&& !(startingPoint instanceof InstanceSpecification)
				&& !(startingPoint instanceof Slot)) {
			// Is on a diagram?
			boolean usedOnDiagram = false;
			for (DiagramPresentationElement diagramPresentationElement : MDModelUtils.getActiveProject().getDiagrams())
				if(diagramPresentationElement.getUsedModelElements(false).contains(startingPoint))
					usedOnDiagram = true;
			
			// If not, used anywhere else?
			if (!usedOnDiagram && !isUsed(startingPoint, this.model)
					&& startingPoint instanceof Classifier)
				elements.add(startingPoint);
		}
		
		// Depth first
		// OPTIMIZATION: Do this only for packages? I.e., skip blocks / classes owned by classes?
		if (startingPoint instanceof Package) {
			for (Element e : startingPoint.getOwnedElement()) {
				// Avoid read-only elements
				elements.addAll(findUnusedElements(e));
			}
		}
		
		return elements;
	}
	
	private boolean isUsed(Element e, Element root) {
		// Check for:
		//		- Applied as a stereotype?
		//		- Used as type of some kind of TypedElement?
		//		(- If diagram: appears on diagram?)
		//		(- If not a classifier - is the owner used anywhere?)
		if (root.getAppliedStereotypeInstance() != null
				&& e instanceof NamedElement
				&& StereotypesHelper.getAppliedStereotypeByString(root, ((NamedElement) e).getName()) != null)
			return true;
		
		if (root instanceof TypedElement
				&& ((TypedElement) root).getType() != null
				&& ((TypedElement) root).getType() == e)
			return true;
			
		for (Element sub : root.getOwnedElement())
			if (isUsed(e, sub))
				return true;
		
		return false;
	}
	
}