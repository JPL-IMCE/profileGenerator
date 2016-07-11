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

import java.util.ArrayList;

/**
 * This class represents a simplified form of a UML stereotype.
 * <P>
 * UML stereotyping is a mechanism for extending the vocabulary / syntax of
 * UML. Instances of meta-class stereotype can be applied to model elements.
 * <P>
 * For a full description of UML stereotypes. See section 12.4.9 of the UML
 * 2.5 specification.
 * 
 * @author Sebastian.J.Herzig@jpl.nasa.gov
 */
public class Stereotype extends NamedElement {

	/** Marks a stereotype as abstract or non-abstract. */
	private boolean _isAbstract = false;
	
	/** Pointer to meta-class that this stereotype extends. */
	private MetaClass _metaclass = null;
	
	/** List of attributes of this stereotype. */
	private ArrayList<Attribute> _attributes = new ArrayList<Attribute>();
	
	/**
	 * Default constructor.
	 * <P>
	 * Stereotype can be instantiated by providing a name, a value for
	 * whether or not the stereotype is abstract, and a pointer to the
	 * owner of the stereotype (typically a {@link Package}).
	 * 
	 * @param name Name of the stereotype.
	 * @param isAbstract Whether or not the stereotype is abstract.
	 * @param owner The owner of the stereotype element.
	 */
	public Stereotype(
			String name, 
			boolean isAbstract,
			Element owner) {
		// Call constructor NamedElement(name)
		super(name);
		
		// Call setters
		setAbstract(isAbstract);
		setOwner(owner);
		
		// Extra logic for stereotype relations - automatically add this stereotype instance
		// to the owning package's list of contained stereotypes
		if (owner instanceof Package) {
			((Package) owner).getStereotypes().add(this);
		}
	}
	
	/**
	 * @return the isAbstract
	 */
	public boolean isAbstract() {
		return _isAbstract;
	}

	/**
	 * @param isAbstract the isAbstract to set
	 */
	public void setAbstract(boolean isAbstract) {
		this._isAbstract = isAbstract;
	}

	/**
	 * @return the metaclass
	 */
	public MetaClass getMetaclass() {
		return _metaclass;
	}

	/**
	 * @param metaclass the metaclass to set
	 */
	public void setMetaclass(MetaClass metaclass) {
		this._metaclass = metaclass;
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

}