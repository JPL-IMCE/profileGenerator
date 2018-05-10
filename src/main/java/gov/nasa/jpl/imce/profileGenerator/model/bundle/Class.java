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
package gov.nasa.jpl.imce.profileGenerator.model.bundle;

import java.lang.String;

/**
 * @author sherzig
 *
 */
public class Class extends NamedElement {

	/** */
	private boolean _isAbstract = false;
	
	/** */
	private boolean _reifiedObjectProperty = false;
	
	/** */
	private boolean _reifiedStructuredDataProperty = false;
	
	/** */
	private boolean _structuredDatatype = false;

	/**
	 * 
	 * @param name
	 */
	public Class(
			String name, 
			boolean isAbstract, 
			boolean isReifiedObjectProperty, 
			boolean isReifiedStructuredDataProperty,
			boolean structuredDatatype) {
		super(name);
		
		// Attributes
		setAbstract(isAbstract);
		setReifiedObjectProperty(isReifiedObjectProperty);
		setReifiedStructuredDataProperty(isReifiedStructuredDataProperty);
		setStructuredDatatype(structuredDatatype);
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
	 * @return the reifiedObjectProperty
	 */
	public boolean isReifiedObjectProperty() {
		return _reifiedObjectProperty;
	}

	/**
	 * @param reifiedObjectProperty the reifiedObjectProperty to set
	 */
	public void setReifiedObjectProperty(boolean reifiedObjectProperty) {
		this._reifiedObjectProperty = reifiedObjectProperty;
	}

	/**
	 * @return the reifiedStructuredDataProperty
	 */
	public boolean isReifiedStructuredDataProperty() {
		return _reifiedStructuredDataProperty;
	}

	/**
	 * @param reifiedStructuredDataProperty the reifiedStructuredDataProperty to set
	 */
	public void setReifiedStructuredDataProperty(boolean reifiedStructuredDataProperty) {
		this._reifiedStructuredDataProperty = reifiedStructuredDataProperty;
	}

	/**
	 * @return the structuredDatatype
	 */
	public boolean isStructuredDatatype() {
		return _structuredDatatype;
	}

	/**
	 * @param structuredDatatype the structuredDatatype to set
	 */
	public void setStructuredDatatype(boolean structuredDatatype) {
		this._structuredDatatype = structuredDatatype;
	}
	
}
