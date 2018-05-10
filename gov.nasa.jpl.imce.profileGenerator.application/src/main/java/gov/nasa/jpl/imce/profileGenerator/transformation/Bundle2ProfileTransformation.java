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
package gov.nasa.jpl.imce.profileGenerator.transformation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import gov.nasa.jpl.imce.profileGenerator.io.BundleDigestReader;
import gov.nasa.jpl.imce.profileGenerator.io.JSONBundleDigestReader;
import gov.nasa.jpl.imce.profileGenerator.io.MDUMLProfileWriter;
import gov.nasa.jpl.imce.profileGenerator.model.bundle.BundleDigest;
import gov.nasa.jpl.imce.profileGenerator.model.profile.Package;

/**
 * @author sherzig
 *
 */
public class Bundle2ProfileTransformation {

	// Read YAML using JYAML (wrapper around)
	
	// Do Bundle2ProfileMappings starting from root of YAML model
	
	// Write to MD SysML Profile

	public static void main(String args[]) {
	    try {
	        System.out.println("args: " + Arrays.toString(args));
	        for (int i = 0; i < args.length ; i++) {
		        String argument = args[i];
		        if (argument.equals("--input")) {
		            if (i+1 < args.length) {
		                Configuration.inputFile = args[i+1];
		                i++;
		            }
		        }
		        else if (argument.equals("--output")) {
		            if (i+1 < args.length) {
		                Configuration.outputFile = args[i+1];
		                i++;
		            }
		        }
		        else if (argument.equals("--template")) {
		            if (i+1 < args.length) {
		                Configuration.template = args[i+1];
		                i++;
		            }
		        }
		        else if (argument.equals("--silent")) {
		            Configuration.silent = true;
		        }
				else if (argument.equals("--libraryMode")) {
					Configuration.generateCompanionAspects = true;
				}
				else if (argument.equals("--generateValidationSuite")) {
					Configuration.generateValidation = true;
					Configuration.generateValidationCustomizations = true;
					Configuration.generateValidationOCLValidationSuite = true;
				}
	        }
			Files.copy(Paths.get(Configuration.template), new FileOutputStream(new File(Configuration.outputFile)));
			
			BundleDigestReader bundleReader = new JSONBundleDigestReader();
			bundleReader.openBundle(Configuration.inputFile);
			//JSONBundleDigestReader bundleReader = new JSONBundleDigestReader();
			//bundleReader.openBundle("test/project-bundle.json");
			
			BundleDigest bundle = bundleReader.readBundleModel();
			
			Bundle2ProfileMappings mappings = new Bundle2ProfileMappings();
			
			Package profilePackage = mappings.bundleToProfile(bundle);
			
			MDUMLProfileWriter mdUMLProfileWriter = new MDUMLProfileWriter();
			mdUMLProfileWriter.writeModel(profilePackage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.exit(0);
	}
	
	// I.e., concepts (& datatype properties), then relationships?
	public void mapElements() {}			// Results in lists of elements
	public void consolidate() {}			// Establish proper references? necessary?
	public void resolveProxies() {}			// Proxy concept: add some named element using which we can later establish a reference?
	
}