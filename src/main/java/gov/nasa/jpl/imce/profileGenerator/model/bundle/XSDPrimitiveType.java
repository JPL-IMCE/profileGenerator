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
public class XSDPrimitiveType extends PrimitiveType {

	// Static instances
	public static final XSDPrimitiveType XSD_STRING = new XSDPrimitiveType("string");				// xsd:string : Any string
	public static final XSDPrimitiveType XSD_INT = new XSDPrimitiveType("int");						// xsd:int : 32-bit signed integers 
	public static final XSDPrimitiveType XSD_INTEGER = new XSDPrimitiveType("integer");				// xsd:integer : Signed integers of arbitrary length 
	public static final XSDPrimitiveType XSD_BOOLEAN = new XSDPrimitiveType("boolean");				// xsd:boolean : Boolean (true or false)
	public static final XSDPrimitiveType XSD_DATE = new XSDPrimitiveType("date");
	
	/**
	 * Retrieve XSD data type from string
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public static XSDPrimitiveType fromString(String name) throws Exception {
		// FIXME Really should use URI mapping
		if (name.startsWith("xsd:"))
			name = name.replace("xsd:", "");
		else if(name.startsWith("http://www.w3.org/2001/XMLSchema#"))
			name = name.replace("http://www.w3.org/2001/XMLSchema#", "");
		
		switch (name) {
		case "int":
			return XSD_INT;
		case "integer":
			return XSD_INTEGER;
		case "boolean":
			return XSD_BOOLEAN;
		case "date":
			return XSD_DATE;
		default:
		//case "string":
			return XSD_STRING;
		}
		
		//throw new Exception("Unknown or unsupported XSD data type \"xsd:" + name + "\" requested");
	}
	
	/**
	 * Private constructor used for creating primitive types internally.
	 * 
	 * @param name
	 */
	private XSDPrimitiveType(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

}
