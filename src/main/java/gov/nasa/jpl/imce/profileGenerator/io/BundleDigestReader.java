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
package gov.nasa.jpl.imce.profileGenerator.io;

import java.util.ArrayList;
import java.util.HashSet;

import gov.nasa.jpl.imce.profileGenerator.model.bundle.BundleDigest;
import gov.nasa.jpl.imce.profileGenerator.model.bundle.Class;
import gov.nasa.jpl.imce.profileGenerator.model.bundle.DataType;
import gov.nasa.jpl.imce.profileGenerator.model.bundle.DataTypeProperty;
import gov.nasa.jpl.imce.profileGenerator.model.bundle.Generalization;
import gov.nasa.jpl.imce.profileGenerator.model.bundle.NamedElement;
import gov.nasa.jpl.imce.profileGenerator.model.bundle.ObjectProperty;

/**
 * @author sherzig
 *
 */
public abstract class BundleDigestReader {
	
	/** */
	protected static final String SECTION_CLASSES = "classes";
	protected static final String SECTION_CLASS_TAXONOMY = "class taxonomy";
	protected static final String SECTION_OBJECT_PROPERTIES = "object property reification";
	protected static final String SECTION_OBJECT_PROPERTY_TAXONOMY = "reified object property taxonomy";
	protected static final String SECTION_SCALAR_DATA_PROPERTIES = "scalar data properties";
	protected static final String SECTION_DATATYPES = "datatypes";
	protected static final String SECTION_DOCUMENTATION = "entity description";
	protected static final String SECTION_SUMMARY = "summary";
	
	/** */
	protected static final String ELEMENT_SUMMARY_BUNDLEIRI = "bundle iri";
	
	/** Used for resolving objects when adding relationships to Bundle model. */
	protected HashSet<Object> _objectStore = new HashSet<Object>();
	
	/**
	 * 
	 * @param filename
	 */
	public abstract void openBundle(String filename);
	
	/**
	 * 
	 * @return
	 */
	public abstract ArrayList<Class> readClasses();
	
	/**
	 * 
	 * @return
	 */
	public abstract ArrayList<Generalization> readClassTaxonomy();
	
	/**
	 * 
	 * @return
	 */
	public abstract ArrayList<Generalization> readObjectPropertyTaxonomy();
	
	/**
	 * 
	 * @return
	 */
	public abstract ArrayList<ObjectProperty> readObjectProperties();
	
	/**
	 * 
	 * @return
	 */
	public abstract ArrayList<DataTypeProperty> readDataTypeProperties();
	
	/**
	 * 
	 * @return
	 */
	public abstract ArrayList<DataType> readDataTypes();
	
	/**
	 * 
	 * @param objectProperties
	 * @return
	 */
	public abstract ArrayList<Generalization> configureRelTypes(ArrayList<ObjectProperty> objectProperties);
	
	/**
	 * 
	 * @return
	 */
	public abstract String readBundleIRI();
	
	/**
	 * 
	 * @return
	 */
	public BundleDigest readBundleModel() {
		// !! ORDER MATTERS !!
		// Pre-compute concepts & reified object properties
		ArrayList<DataType> dataTypes = readDataTypes();
		ArrayList<Class> classes = readClasses();
		ArrayList<ObjectProperty> objProps = readObjectProperties();
		ArrayList<DataTypeProperty> dataTypeProperties = readDataTypeProperties();
		
		// Pre-compute relationships
		ArrayList<Generalization> classGeneralizations = readClassTaxonomy();
		ArrayList<Generalization> objPropGeneralizations = readObjectPropertyTaxonomy();
		ArrayList<Generalization> objPropRelTypeSpecifications = configureRelTypes(objProps);
		
		// Package model
		// TODO Summary information as info for load prod bundle?
		BundleDigest loadProdBundle = new BundleDigest(readBundleIRI());
		loadProdBundle.setClasses(classes);
		loadProdBundle.setObjectProperties(objProps);
		loadProdBundle.setDataTypeProperties(dataTypeProperties);
		loadProdBundle.setDataTypes(dataTypes);
		loadProdBundle.getGeneralizations().addAll(classGeneralizations);
		loadProdBundle.getGeneralizations().addAll(objPropGeneralizations);
		loadProdBundle.getGeneralizations().addAll(objPropRelTypeSpecifications);
		
		return loadProdBundle;
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	protected NamedElement lookupElementByName(String name) {
		for (Object o : _objectStore)
			if (o instanceof NamedElement
					&& ((NamedElement) o).getName().equals(name))
				return ((NamedElement) o);
		
		return null;
	}
	
}
