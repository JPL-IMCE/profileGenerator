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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.yaml.snakeyaml.Yaml;

import com.esotericsoftware.yamlbeans.YamlException;

import gov.nasa.jpl.imce.profileGenerator.model.bundle.*;
import gov.nasa.jpl.imce.profileGenerator.model.bundle.Class;

/**
 * @author sherzig
 *
 */
public class YAMLBundleDigestReader extends BundleDigestReader {
	
	/** */
	private Map<String,Map> _rootObject = null;

	/**
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		BundleDigestReader bundleReader = new YAMLBundleDigestReader();
		bundleReader.openBundle("test/project-bundle.yaml");
		bundleReader.readBundleModel();
	}
	
	@Override
	public void openBundle(String filename) {
		//YamlReader reader;
		Yaml yaml = new Yaml();
		
		try {
			// Clean file, just in case
			cleanBundleFile(filename);
			
			//reader = new YamlReader(new FileReader(filename));
			Object root = yaml.load(new FileReader(filename));
			_rootObject = (Map<String, Map>) root;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (YamlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public ArrayList<Class> readClasses() {
		ArrayList<Class> newClasses = new ArrayList<Class>();
		
		for (Entry<String,Map<String,String>> ci : (Collection<Entry<String,Map<String,String>>>) _rootObject.get(SECTION_CLASSES).entrySet()) {
			System.out.println("Found class: " + ci.getKey());
			
			// Create new Class object based on properties found
			Class clazz = new Class(
					ci.getKey(),
					Boolean.parseBoolean(ci.getValue().get("isAbstract")),
					Boolean.parseBoolean(ci.getValue().get("reifiedObjectProperty")),
					Boolean.parseBoolean(ci.getValue().get("reifiedStructuredDataProperty")),
					Boolean.parseBoolean(ci.getValue().get("structuredDatatype")));
			
			// Set documentation
			clazz.setDocumentation((String) _rootObject.get(SECTION_DOCUMENTATION).get(ci.getKey()));
			
			if (!_objectStore.contains(clazz))
				_objectStore.add(clazz);
			
			newClasses.add(clazz);
		}
		
		return newClasses;
	}
	
	@Override
	public ArrayList<Generalization> readClassTaxonomy() {
		return readTaxonomy(SECTION_CLASS_TAXONOMY);
	}
	
	@Override
	public ArrayList<Generalization> readObjectPropertyTaxonomy() {
		return readTaxonomy(SECTION_OBJECT_PROPERTY_TAXONOMY);
	}
	
	/**
	 * @precondition classes and object properties must have been read.
	 * @return
	 */
	protected ArrayList<Generalization> readTaxonomy(String section) {
		ArrayList<Generalization> newGeneralizations = new ArrayList<Generalization>();
		
		if (_rootObject.get(section) == null)
			return newGeneralizations;		// No entries
		
		for (Entry<String,ArrayList<String>> ci : (Collection<Entry<String,ArrayList<String>>>) _rootObject.get(section).entrySet()) {
			System.out.println("Found taxonomy: " + ci.getKey() + " " + ci.getValue());
			
			NamedElement specific = lookupElementByName(ci.getKey());
			
			// If not found, the element is probably a reference to a model library element
			if (specific == null)
				specific = new ReferencedElement(ci.getKey());
			
			// General classes are defined in a collection
			ArrayList<String> generals = ci.getValue();
			
			for (String generalName : generals) {
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
	public ArrayList<ObjectProperty> readObjectProperties() {
		ArrayList<ObjectProperty> newObjectProperties = new ArrayList<ObjectProperty>();
		
		for (Entry<String,Map<String,String>> ci : (Collection<Entry<String,Map<String,String>>>) _rootObject.get(SECTION_OBJECT_PROPERTIES).entrySet()) {
			System.out.println("Found object property: " + ci.getKey());
			
			NamedElement relType = lookupElementByName((String) ci.getValue().get("reltype"));
			
			if (relType == null)
				relType = new ReferencedElement((String) ci.getValue().get("reltype"));
			
			// Create new Class object based on properties found
			ObjectProperty op = new ObjectProperty(
					ci.getKey(),
					relType, 
					Boolean.parseBoolean(ci.getValue().get("isAbstract")),
					Boolean.parseBoolean(ci.getValue().get("isDerived")));
			
			// Set documentation
			op.setDocumentation((String) _rootObject.get(SECTION_DOCUMENTATION).get(ci.getKey()));
			
			if (!_objectStore.contains(op))
				_objectStore.add(op);
			
			newObjectProperties.add(op);
		}
		
		return newObjectProperties;
	}
	

	@Override
	public ArrayList<DataTypeProperty> readDataTypeProperties() {
		ArrayList<DataTypeProperty> newDataTypeProperties = new ArrayList<DataTypeProperty>();
		
		for (Entry<String,Map<String,String>> ci : (Collection<Entry<String,Map<String,String>>>) _rootObject.get(SECTION_SCALAR_DATA_PROPERTIES).entrySet()) {
			String name = ci.getKey();
			String domainName = ci.getValue().get("domain");
			String rangeName = ci.getValue().get("range");
			
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
		
		for (Entry<String,Map<String,ArrayList<String>>> ci : (Collection<Entry<String,Map<String,ArrayList<String>>>>) _rootObject.get(SECTION_DATATYPES).entrySet()) {
			String types = ci.getKey();
			
			// Enumerations
			if (types.equals("oneOf")) {
				for (Entry<String,ArrayList<String>> dataTypeDefinition : ci.getValue().entrySet()) {
					// Extract data type name
					String dataTypeName = dataTypeDefinition.getKey();
					
					// Now parse values
					ArrayList<PrimitiveTypeInstance> values = new ArrayList<PrimitiveTypeInstance>();
					
					for (String enumValue : dataTypeDefinition.getValue()) {
						String[] primitiveInfo = enumValue.split("\\^\\^");
						
						if (primitiveInfo.length != 2) {
							System.out.println("[ERROR] Illformed primitive type instance - unexpected format");
							
							continue;
						}
						
						// Get parts of primitive instance
						String primitiveTypeDesc = primitiveInfo[1];
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
	 * Returns the bundle's IRI. This IRI is used on the profile side
	 * to construct a package hierarchy for the respective bundle.
	 * 
	 * @return The bundle's IRI.
	 */
	@Override
	public String readBundleIRI() {
		return (String) _rootObject.get(SECTION_SUMMARY).get(ELEMENT_SUMMARY_BUNDLEIRI);
	}
	
	/**
	 * Clean a YAML bundle file by removing any erroneous input from the beginning
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
		int line = 1;
		while ((strLine = br.readLine()) != null)   {
		  strLine = strLine.trim();
		  
		  if (strLine.startsWith("---"))
			  skip = false;
		  
		  if (!skip) {
			  strLine = strLine.replace("|-", "");
			  
			  if (line == 189385
					  || line == 189384
					  || line == 189383
					  || line == 189386
					  || line == 189387)
				  System.out.println(strLine);
			  
			  writer.write(strLine + System.getProperty("line.separator"));
			  
			  line++;
		  }
		}

		// Close the input and output streams
		br.close();
		writer.close();
		
		inputFile.delete();
		outputFile.renameTo(inputFile);
	}

}
