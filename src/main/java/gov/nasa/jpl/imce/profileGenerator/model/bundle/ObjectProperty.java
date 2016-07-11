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

/**
 * @author sherzig
 *
 */
public class ObjectProperty extends NamedElement {

	/** Relationships */
	private Class _relClass = null;
	
	/** */
	private ObjectProperty _hasAnalyzesSource = null;
	
	/** */
	private Class _srcType = null;
	
	/** */
	private ObjectProperty _targetProperty = null;
	
	/** */
	private Class _targetType = null;
	
	/** */
	private NamedElement _relType = null;
	
	/** Attributes */
	private boolean _isAbstract = false;
	
	/** */
	private boolean _isDerived = false;
	
	/**
	 * 
	 * @param name
	 * @param relType
	 * @param isAbstract
	 * @param isDerived
	 */
	public ObjectProperty(
			String name, 
			NamedElement relType,
			boolean isAbstract,
			boolean isDerived) {
		super(name);
		
		setRelType(relType);
		setAbstract(isAbstract);
		setDerived(isDerived);
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
	 * @return the isDerived
	 */
	public boolean isDerived() {
		return _isDerived;
	}

	/**
	 * @param isDerived the isDerived to set
	 */
	public void setDerived(boolean isDerived) {
		this._isDerived = isDerived;
	}

	/**
	 * @return the relClass
	 */
	public Class getRelClass() {
		return _relClass;
	}

	/**
	 * @param relClass the relClass to set
	 */
	public void setRelClass(Class relClass) {
		this._relClass = relClass;
	}

	/**
	 * @return the hasAnalyzesSource
	 */
	public ObjectProperty getHasAnalyzesSource() {
		return _hasAnalyzesSource;
	}

	/**
	 * @param hasAnalyzesSource the hasAnalyzesSource to set
	 */
	public void setHasAnalyzesSource(ObjectProperty hasAnalyzesSource) {
		this._hasAnalyzesSource = hasAnalyzesSource;
	}

	/**
	 * @return the srcType
	 */
	public Class getSrcType() {
		return _srcType;
	}

	/**
	 * @param srcType the srcType to set
	 */
	public void setSrcType(Class srcType) {
		this._srcType = srcType;
	}

	/**
	 * @return the targetProperty
	 */
	public ObjectProperty getTargetProperty() {
		return _targetProperty;
	}

	/**
	 * @param targetProperty the targetProperty to set
	 */
	public void setTargetProperty(ObjectProperty targetProperty) {
		this._targetProperty = targetProperty;
	}

	/**
	 * @return the targetType
	 */
	public Class getTargetType() {
		return _targetType;
	}

	/**
	 * @param targetType the targetType to set
	 */
	public void setTargetType(Class targetType) {
		this._targetType = targetType;
	}

	/**
	 * @return the relType
	 */
	public NamedElement getRelType() {
		return _relType;
	}

	/**
	 * @param relType the relType to set
	 */
	public void setRelType(NamedElement relType) {
		this._relType = relType;
	}
	
}
