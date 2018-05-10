/*
 *
 * License Terms
 *
 * Copyright (c) 2014-2016, California Institute of Technology ("Caltech").
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
package gov.nasa.jpl.imce.profileGenerator.model.profile;

import java.lang.String;
import java.lang.Object;
import java.util.ArrayList;

/**
 * Represents an element with a name - however, unlike UML's NamedElement, this
 * type also allows for generalization and stereotyping (simplified).
 * 
 * @author Sebastian.J.Herzig@jpl.nasa.gov
 */
public abstract class NamedElement extends Element {

	/** */
	private ArrayList<Generalization> _generalization = new ArrayList<Generalization>();
	
	/** */
	private ArrayList<Stereotype> _appliedStereotypes = new ArrayList<Stereotype>();

	/** */
	private ArrayList<Constraint> _appliedConstraints = new ArrayList<Constraint>();

	/** List of attributes of this stereotype. */
	private ArrayList<Attribute> _attributes = new ArrayList<Attribute>();
	
	/** */
	private String _documentation = "";
	
	/** Name of the element. */
	private String _name = "";

	/**
	 * 
	 * @param name
	 */
	public NamedElement(String name) {
		setName(name);
	}
	
	/**
	 * @return the generalization
	 */
	public ArrayList<Generalization> getGeneralization() {
		return _generalization;
	}

	/**
	 * @param generalization the generalization to set
	 */
	public void setGeneralization(ArrayList<Generalization> generalization) {
		_generalization = generalization;
	}

	/**
	 * Return all generalizations (inherited and owned).
	 *
	 * @return
     */
	public ArrayList<Generalization> getAllGeneralizations() {
		ArrayList<Generalization> allGenerals = new ArrayList<Generalization>();

		allGenerals.addAll(this.getGeneralization());

		for (Generalization g : getGeneralization())
			allGenerals.addAll(g.getGeneral().getAllGeneralizations());

		return allGenerals;
	}

	/**
	 * Returns the name of the profile.
	 * 
	 * @return The name of the profile.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Sets the name of the profile
	 * 
	 * @param name The name to give the profile.
	 */
	public void setName(String name) {
		_name = name;
	}
	
	/**
	 * Returns the qualified name of the element.
	 * 
	 * @return
	 */
	public String getQualifiedName() {
		// Construct qualified name
		String qualifiedName = getName();
		
		// Search owner tree (and check for cycles - which should never exist)
		NamedElement n = (NamedElement) this.getOwner();
		
		while(n != null && n != this) {
			qualifiedName = n.getName() + "::" + qualifiedName;
			
			// FIXME Dangerous type casting
			n = (NamedElement) n.getOwner();
		}
		
		return qualifiedName;
	}

	/**
	 * @return the appliedStereotypes
	 */
	public ArrayList<Stereotype> getAppliedStereotypes() {
		return _appliedStereotypes;
	}

	/**
	 * @param appliedStereotypes the appliedStereotypes to set
	 */
	public void setAppliedStereotypes(ArrayList<Stereotype> appliedStereotypes) {
		this._appliedStereotypes = appliedStereotypes;
	}

	/**
	 *
	 * @return
     */
	public ArrayList<Constraint> getAppliedConstraints() {
		return _appliedConstraints;
	}

	/**
	 *
	 * @param appliedConstraints
     */
	public void setAppliedConstraints(ArrayList<Constraint> appliedConstraints) {
		this._appliedConstraints = appliedConstraints;
	}

	/**
	 * 
	 * @return
	 */
	public String getDocumentation() {
		return _documentation;
	}

	/**
	 * 
	 * @param documentation
	 */
	public void setDocumentation(String documentation) {
		this._documentation = documentation;
	}

	/**
	 * @return the attributes
	 */
	public ArrayList<Attribute> getAttributes() {
		return _attributes;
	}

	/**
	 * @param attributes the attributes to set
	 */
	public void setAttributes(ArrayList<Attribute> attributes) {
		this._attributes = attributes;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getQualifiedName() == null) ? 0 : getQualifiedName().hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NamedElement other = (NamedElement) obj;
		if (getQualifiedName() == null) {
			if (other.getQualifiedName() != null)
				return false;
		} else if (!getQualifiedName().equals(other.getQualifiedName()))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getQualifiedName();
	}
	
}