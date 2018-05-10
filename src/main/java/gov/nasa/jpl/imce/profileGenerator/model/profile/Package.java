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
import java.util.ArrayList;

/**
 * Representation of a UML package.
 * 
 * @author Sebastian.J.Herzig@jpl.nasa.gov
 */
public class Package extends NamedElement {

	/** Reference to stereotypes contained in profile. */
	private ArrayList<Package> _ownedPackages = new ArrayList<Package>();
	
	/** Reference to package owning this package. */
	private Package _owningPackage = null;
	
	/** Reference to stereotypes contained in profile. */
	private ArrayList<Stereotype> _stereotypes = new ArrayList<Stereotype>();
	
	/** Reference to data types contained in package. */
	private ArrayList<DataType> _dataTypes = new ArrayList<DataType>();

	/** Contained constraint definitions. */
	private ArrayList<Constraint> _constraints = new ArrayList<Constraint>();

	/** Contained MD customizations. */
	private ArrayList<Customization> _customizations = new ArrayList<Customization>();

	/** Reference to data types contained in package. */
	private ArrayList<Classifier> _classifiers = new ArrayList<>();

	/** Whether or not to share package - this is only the case for to-be-exported packages. */
	private boolean _sharePackage = false;

	/**
	 * 
	 * @param name
	 */
	public Package(String name) {
		super(name);
	}
	
	/**
	 * 
	 * @param owner
	 */
	public Package(String name, Package owner) {
		this(name);
		
		setOwningPackage(owner);		// Also sets "owner" property
	}
	
	/**
	 * @return the ownedPackages
	 */
	public ArrayList<Package> getOwnedPackages() {
		return _ownedPackages;
	}

	/**
	 * @param ownedPackages the ownedPackages to set
	 */
	public void setOwnedPackages(ArrayList<Package> ownedPackages) {
		this._ownedPackages = ownedPackages;
	}

	/**
	 * @return the owningPackage
	 */
	public Package getOwningPackage() {
		return _owningPackage;
	}

	/**
	 * @param owningPackage the owningPackage to set
	 */
	public void setOwningPackage(Package owningPackage) {
		this._owningPackage = owningPackage;
		
		setOwner(owningPackage);
	}

	/**
	 * 
	 * @return
	 */
	public ArrayList<Stereotype> getStereotypes() {
		return _stereotypes;
	}

	/**
	 * 
	 * @param stereotypes
	 */
	public void setStereotypes(ArrayList<Stereotype> stereotypes) {
		this._stereotypes = stereotypes;
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


	/**
	 * @return the dataTypes
	 */
	public ArrayList<Classifier> getClassifiers() {
		return _classifiers;
	}

	/**
	 * @param classifiers the dataTypes to set
	 */
	public void setClassifiers(ArrayList<Classifier> classifiers) {
		this._classifiers = classifiers;
	}


	/**
	 *
	 * @return
     */
	public boolean isSharePackage() {
		return _sharePackage;
	}

	/**
	 *
	 * @param sharePackage
     */
	public void setSharePackage(boolean sharePackage) {
		this._sharePackage = sharePackage;
	}

	/**
	 *
	 * @return
     */
	public ArrayList<Constraint> getConstraints() {
		return _constraints;
	}

	/**
	 *
	 * @param constraints
     */
	public void setConstraints(ArrayList<Constraint> constraints) {
		this._constraints = constraints;
	}

	/**
	 *
	 * @return
     */
	public ArrayList<Customization> getCustomizations() {
		return _customizations;
	}

	/**
	 *
	 * @param customizations
     */
	public void setCustomizations(ArrayList<Customization> customizations) {
		this._customizations = customizations;
	}

}