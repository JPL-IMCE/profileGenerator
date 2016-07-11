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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author sherzig
 *
 */
public class BundleDigest {

	/** Bundle IRI determines package structure in profile. */
	private String _bundleIRI = "";
	
	/** */
	private ArrayList<Class> _classes = new ArrayList<Class>();
	
	/** */
	private ArrayList<ObjectProperty> _objectProperties = new ArrayList<ObjectProperty>();
	
	/** */
	private ArrayList<Generalization> _generalizations = new ArrayList<Generalization>();
	
	/** Attributes of stereotypes. */
	private ArrayList<DataTypeProperty> _dataTypeProperties = new ArrayList<DataTypeProperty>();
	
	/** Data type definitions. */
	private ArrayList<DataType> _dataTypes = new ArrayList<DataType>();

	/**
	 * 
	 * @param bundleIRI
	 */
	public BundleDigest(String bundleIRI) {
		setBundleIRI(bundleIRI);
	}
	
	/**
	 * @return the bundleIRI
	 */
	public String getBundleIRI() {
		return _bundleIRI;
	}

	/**
	 * @param bundleIRI the bundleIRI to set
	 */
	public void setBundleIRI(String bundleIRI) {
		this._bundleIRI = bundleIRI;
	}

	/**
	 * @return the classes
	 */
	public ArrayList<Class> getClasses() {
		return _classes;
	}

	/**
	 * @param classes the classes to set
	 */
	public void setClasses(ArrayList<Class> classes) {
		this._classes = classes;
	}

	/**
	 * @return the objectProperties
	 */
	public ArrayList<ObjectProperty> getObjectProperties() {
		return _objectProperties;
	}

	/**
	 * @param objectProperties the objectProperties to set
	 */
	public void setObjectProperties(ArrayList<ObjectProperty> objectProperties) {
		this._objectProperties = objectProperties;
	}

	/**
	 * @return the generalizations
	 */
	public ArrayList<Generalization> getGeneralizations() {
		return _generalizations;
	}

	/**
	 * @param generalizations the generalizations to set
	 */
	public void setGeneralizations(ArrayList<Generalization> generalizations) {
		this._generalizations = generalizations;
	}

	/**
	 * @return the dataTypeProperties
	 */
	public ArrayList<DataTypeProperty> getDataTypeProperties() {
		return _dataTypeProperties;
	}

	/**
	 * @param dataTypeProperties the dataTypeProperties to set
	 */
	public void setDataTypeProperties(ArrayList<DataTypeProperty> dataTypeProperties) {
		this._dataTypeProperties = dataTypeProperties;
	}

	/**
	 * @return the dataTypes
	 */
	public ArrayList<DataType> getDataTypes() {
		return _dataTypes;
	}

	/**
	 * @param dataTypes the dataTypes to set
	 */
	public void setDataTypes(ArrayList<DataType> dataTypes) {
		this._dataTypes = dataTypes;
	}
	
}
