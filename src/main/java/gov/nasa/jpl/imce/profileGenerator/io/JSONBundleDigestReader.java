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
package gov.nasa.jpl.imce.profileGenerator.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import gov.nasa.jpl.imce.profileGenerator.model.bundle.Class;
import gov.nasa.jpl.imce.profileGenerator.model.bundle.DataType;
import gov.nasa.jpl.imce.profileGenerator.model.bundle.DataTypeProperty;
import gov.nasa.jpl.imce.profileGenerator.model.bundle.Generalization;
import gov.nasa.jpl.imce.profileGenerator.model.bundle.NamedElement;
import gov.nasa.jpl.imce.profileGenerator.model.bundle.ObjectProperty;
import gov.nasa.jpl.imce.profileGenerator.model.bundle.PrimitiveType;
import gov.nasa.jpl.imce.profileGenerator.model.bundle.PrimitiveTypeInstance;
import gov.nasa.jpl.imce.profileGenerator.model.bundle.ReferencedElement;
import gov.nasa.jpl.imce.profileGenerator.model.bundle.XSDPrimitiveType;

/**
 * 
 * @author Sebastian.J.Herzig@jpl.nasa.gov
 */
public class JSONBundleDigestReader extends BundleDigestReader {

	private JSONObject _rootObject = null;
	
	public static void main(String args[]) {
		BundleDigestReader r = new JSONBundleDigestReader();
		r.openBundle("test/project-bundle.json");
		r.readBundleModel();
	}
	
	@Override
	public void openBundle(String filename) {
		JSONParser parser = new JSONParser();
		
		try {
			cleanBundleFile(filename);
			
			// Root element is an object
			_rootObject = (JSONObject) parser.parse(new FileReader(filename));
			
			// Dump sections
			System.out.println("---- SECTIONS ----");
			for (String section : (Collection<String>) _rootObject.keySet())
				System.out.println(section);
			System.out.println("------------------");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public ArrayList<Class> readClasses() {
		ArrayList<Class> newClasses = new ArrayList<Class>();
		
		JSONObject classes = (JSONObject) _rootObject.get(SECTION_CLASSES);

		// Fairly safe, since output format known - each class in this section is a JSON object
		// FIXME Check for illformed input
		for (Entry<String,JSONObject> classDesc : (Collection<Entry<String,JSONObject>>) classes.entrySet()) {
			Class newClass = new Class(
					classDesc.getKey(),
					(boolean) classDesc.getValue().get("isAbstract"),
					(boolean) classDesc.getValue().get("reifiedObjectProperty"),
					(boolean) classDesc.getValue().get("reifiedStructuredDataProperty"),
					(boolean) classDesc.getValue().get("structuredDatatype"));
			
			newClasses.add(newClass);
			
			if (!_objectStore.contains(newClass))
				_objectStore.add(newClass);
		}
		
		return newClasses;
	}
	
	/**
	 * Reads a taxonomy section.
	 * <P>
	 * This function is called by both {@link #readClassTaxonomy()} and
	 * {@link #readObjectPropertyTaxonomy()} since the organization of data
	 * is highly similar.
	 * 
	 * @param section The section to read out.
	 * @return A list of Generalization objects.
	 */
	protected ArrayList<Generalization> readTaxonomy(String section) {
		ArrayList<Generalization> newGeneralizations = new ArrayList<Generalization>();
		
		JSONObject classes = (JSONObject) _rootObject.get(section);
		
		if (classes == null)
			return newGeneralizations;		// No entries

		for (Entry<String,JSONArray> genInfo : (Collection<Entry<String,JSONArray>>) classes.entrySet()) {
			System.out.println("[JSON] Found taxonomy: " + genInfo.getKey());
			
			NamedElement specific = lookupElementByName(genInfo.getKey());
			
			// If not found, the element is probably a reference to a model library element
			if (specific == null)
				specific = new ReferencedElement(genInfo.getKey());
			
			// General classes / properties are defined in a collection
			JSONArray generals = genInfo.getValue();
			
			for (Object generalNameObj : generals) {
				String generalName = (String) generalNameObj;
				NamedElement general = null;
				
				// Look up element by name
				general = lookupElementByName(generalName);
				
				// If not found, the element is probably a reference to a model library element
				if (general == null)
					general = new ReferencedElement(generalName);
				
				// Create new generalization object
				Generalization g = new Generalization(general, specific);
				
				if (!_objectStore.contains(g))
					_objectStore.add(g);
				
				newGeneralizations.add(g);
			}
		}
		
		return newGeneralizations;
	}
	
	@Override
	public ArrayList<Generalization> readClassTaxonomy() {
		return readTaxonomy(SECTION_CLASS_TAXONOMY);
	}

	@Override
	public ArrayList<Generalization> readObjectPropertyTaxonomy() {
		return readTaxonomy(SECTION_OBJECT_PROPERTY_TAXONOMY);
	}

	@Override
	public ArrayList<ObjectProperty> readObjectProperties() {
		ArrayList<ObjectProperty> newObjectProperties = new ArrayList<ObjectProperty>();
		
		JSONObject objectProperties = (JSONObject) _rootObject.get(SECTION_OBJECT_PROPERTIES);
		
		if (objectProperties == null)
			return newObjectProperties;
		
		for (Entry<String,JSONObject> ci : (Collection<Entry<String,JSONObject>>) objectProperties.entrySet()) {
			System.out.println("[JSON] Found object property: " + ci.getKey());
			
			NamedElement relType = lookupElementByName((String) ci.getValue().get("reltype"));
			
			if (relType == null)
				relType = new ReferencedElement((String) ci.getValue().get("reltype"));
			
			boolean isAbstract = (boolean) ci.getValue().get("isAbstract");
			boolean isDerived = (boolean) ci.getValue().get("isDerived");
			
			// Create new Class object based on properties found
			ObjectProperty op = new ObjectProperty(
					ci.getKey(),
					relType, 
					isAbstract,
					isDerived);
			
			// Set documentation
			op.setDocumentation((String) getDocumentationStringFor(ci.getKey()));

			// Look up source and target
			NamedElement sourceType = lookupElementByName((String) ci.getValue().get("srctype"));
			if (sourceType == null)
				sourceType = new ReferencedElement((String) ci.getValue().get("srctype"));

			NamedElement targetType = lookupElementByName((String) ci.getValue().get("trgtype"));
			if (targetType == null)
				targetType = new ReferencedElement((String) ci.getValue().get("trgtype"));

			// Set source and target
			op.setSrcType((Class) sourceType);
			op.setTargetType((Class) targetType);
			
			if (!_objectStore.contains(op))
				_objectStore.add(op);
			
			newObjectProperties.add(op);
		}
		
		return newObjectProperties;
	}

	@Override
	public String readBundleIRI() {
		return (String) ((JSONObject) _rootObject.get(SECTION_SUMMARY)).get(ELEMENT_SUMMARY_BUNDLEIRI);
	}

	@Override
	public ArrayList<Generalization> configureRelTypes(ArrayList<ObjectProperty> objectProperties) {
		ArrayList<Generalization> relTypeGeneralizations = new ArrayList<Generalization>();
		
		for (ObjectProperty o : objectProperties) {
			if (o.getRelType() != null) {
				// Try to resolve reference
				if (o.getRelType() instanceof ReferencedElement) {
					NamedElement relType = lookupElementByName(o.getRelType().getName());
					
					if (relType != null)
						o.setRelType(relType);
				}
				
				Generalization g = new Generalization(o.getRelType(), o);
				
				if (!_objectStore.contains(g))
					_objectStore.add(g);
				
				relTypeGeneralizations.add(g);
			}
		}
		
		return relTypeGeneralizations;
	}

	@Override
	public ArrayList<DataTypeProperty> readDataTypeProperties() {
		ArrayList<DataTypeProperty> newDataTypeProperties = new ArrayList<DataTypeProperty>();
		
		JSONObject dataTypeProperties = (JSONObject) _rootObject.get(SECTION_SCALAR_DATA_PROPERTIES);
		
		if (dataTypeProperties == null)
			return newDataTypeProperties;
		
		for (Entry<String,JSONObject> ci : (Collection<Entry<String,JSONObject>>) dataTypeProperties.entrySet()) {
			String name = ci.getKey();
			JSONObject domainRange = ci.getValue();
			String domainName = (String) domainRange.get("domain");
			String rangeName = (String) domainRange.get("range");
			
			System.out.println("[JSON] Found data type property: " + name + " (domain: " + domainName + ", range: " + rangeName + ")");
			
			NamedElement domain = null;
			NamedElement range = null;
			
			// Find domain element
			domain = lookupElementByName(domainName);
			
			// Find range element
			if (rangeName.startsWith("xsd:")) {
				try {
					range = XSDPrimitiveType.fromString(rangeName);
				} catch (Exception e) {
					e.printStackTrace();
					
					continue;
				}
			}
			else
				range = lookupElementByName(rangeName);
			
			if (domain != null && range != null) {
				DataTypeProperty dtp = new DataTypeProperty(name, domain, range);
				
				if (!_objectStore.contains(dtp))
					_objectStore.add(dtp);
				
				newDataTypeProperties.add(dtp);
			}
		}
		
		return newDataTypeProperties;
	}

	@Override
	public ArrayList<DataType> readDataTypes() {
		ArrayList<DataType> newDataTypes = new ArrayList<DataType>();
		
		JSONObject dataTypes = (JSONObject) _rootObject.get(SECTION_DATATYPES);
		
		if (dataTypes == null)
			return newDataTypes;
		
		for (Entry<String,JSONObject> ci : (Collection<Entry<String, JSONObject>>) dataTypes.entrySet()) {
			String types = ci.getKey();
			
			// Enumerations
			if (types.equals("oneOf")) {
				JSONObject val = ci.getValue();
				
				for (Entry<String,JSONArray> dataTypeDefinition : (Collection<Entry<String,JSONArray>>) val.entrySet()) {
					// Extract data type name
					String dataTypeName = dataTypeDefinition.getKey();
					
					System.out.println("[JSON] Found data type: " + dataTypeName);
					
					// Now parse values
					ArrayList<PrimitiveTypeInstance> values = new ArrayList<PrimitiveTypeInstance>();
					
					for (Object enumVal : dataTypeDefinition.getValue()) {
						String enumValue = (String) enumVal;
						
						String[] primitiveInfo = enumValue.split("\\^\\^");
						
						if (primitiveInfo.length != 2) {
							System.out.println("[WARN] Illformed primitive type instance - unexpected format (before split, was: " + enumValue + "); assuming string");
							
							//continue;
						}

						// Get parts of primitive instance
						String primitiveTypeDesc = "http://www.w3.org/2001/XMLSchema#string";
						if (primitiveInfo.length >= 2)
							primitiveTypeDesc = primitiveInfo[1];

						String primitiveInstanceVal = primitiveInfo[0];
						
						// Now create corresponding objects
						PrimitiveType type = null;
						
						try {
							type = XSDPrimitiveType.fromString(primitiveTypeDesc);
						} catch (Exception e) {
							// Probably not a XSD primitive type - print error message and continue
							System.out.println("[ERROR] Unsupported primitive type \"" + primitiveTypeDesc + "\"");
							
							e.printStackTrace();
							
							continue;
						}
						
						// Add the identified element to the list of primitive type instances
						values.add(new PrimitiveTypeInstance(type, primitiveInstanceVal));
					}
					
					DataType d = new DataType(dataTypeName, values);
					
					if (!_objectStore.contains(d))
						_objectStore.add(d);
					
					// Create a new data type object, and set primitive data type instances (ensure that they really are - and warn otherwise)
					newDataTypes.add(d);
				}
			}
			else {
				System.out.println("[WARN] Unsupported datatypes type: " + types);
			}
		}
		
		return newDataTypes;
	}
	
	/**
	 * Returns the documentation string for a particular object.
	 * 
	 * @param element
	 * @return
	 */
	protected String getDocumentationStringFor(String element) {
		if (_rootObject.get(SECTION_DOCUMENTATION) == null)
			return "";
		
		return (String) ((JSONObject) _rootObject.get(SECTION_DOCUMENTATION)).get(element);
	}
	
	/**
	 * Clean a JSON bundle file by removing any erroneous input from the beginning
	 * of the file.
	 * 
	 * @param filename
	 * @throws IOException 
	 */
	public void cleanBundleFile(String filename) throws IOException {
		// Open the file
		File inputFile = new File(filename);
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		
		// Also open a temporary writer for the output
		File outputFile = new File(filename + ".tmp");
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

		String strLine;

		// Skip all lines until the "---" marker signfying beginning of the YAML file
		boolean skip = true;
		boolean foundErrorneousLines = false;
		int line = 1;
		while ((strLine = br.readLine()) != null)   {
		  strLine = strLine.trim();
		  
		  if (strLine.startsWith("{"))
			  skip = false;
		  
		  if (!skip) {
			  strLine = strLine.replace("|-", "");
			  
			  if (line == 189385
					  || line == 189384
					  || line == 189383
					  || line == 189386
					  || line == 189387)
				  System.out.println(strLine);
			  
			  // Don't write if not necessary
			  if (foundErrorneousLines)
				  writer.write(strLine + System.getProperty("line.separator"));
			  
			  line++;
		  }
		  else
			  foundErrorneousLines = true;
		}

		// Close the input and output streams
		br.close();
		
		if (foundErrorneousLines) {
			writer.close();
			
			inputFile.delete();
			outputFile.renameTo(inputFile);
		}
	}

}