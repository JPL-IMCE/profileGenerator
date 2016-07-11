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
package gov.nasa.jpl.imce.profileGenerator.util;

import java.io.File;

/**
 * @author sherzig
 *
 */
public class DevUtils {

	/** */
	public static String _root = "/Applications/MagicDraw/cae_md18_0_sp5_mdk-2.3.1/";

	/**
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		generateMDPOMDependency();
		generateCommandLineDependencies();
	}
	
	/**
	 * 
	 */
	public static void generateCommandLineDependencies() {
		// Generate class path
		File file;
		
		// Lib
		file = new File(_root + "/lib");
		traverse2(file);
		
		// Plugins
		//file = new File(root + "/plugins");
		//traverse2(file);
	}
	
	/**
	 * 
	 * @param f
	 */
	private static void traverse2(File f) {
		// Depth first search for jars
		if (f.listFiles() != null) {
			System.out.print("\"" + f.getAbsolutePath() + "/*\":");
			for (File sf : f.listFiles())
				traverse2(sf);
		}
	}
	
	/**
	 * 
	 */
	public static void generateMDPOMDependency() {
		// Generate class path
		File file;
		
		// Lib
		file = new File(_root + "/lib");
		traverse(file);
		
		// Plugins
		file = new File(_root + "/plugins");
		traverse(file);
	}
	
	/**
	 * 
	 * @param f
	 */
	private static void traverse(File f) {
		// Depth first search for jars
		if (!f.isDirectory() && isLibrary(f))
			System.out.println(constructPOMDependency(f.getAbsolutePath()));
		else if (f.listFiles() != null) {
			for (File sf : f.listFiles())
				traverse(sf);
		}
	}
	
	/**
	 * 
	 * @param filename
	 * @return
	 */
	private static String constructPOMDependency(String filename) {
		String artifactName =  (new File(filename)).getName();
		artifactName = artifactName.replace(".jar", "");
		
		return 	"<dependency>\n" + 
				"	<groupId>com.magicdraw.application</groupId>\n" + 
				"	<artifactId>" + artifactName.replace("_", "") + "</artifactId>\n" + 
				"	<version>18.0.1sp5</version>\n" + 
				"	<scope>system</scope>\n" + 
				"	<systemPath>" + filename.replace(_root, "${magicdraw.installdir}/") + "</systemPath>\n" + 
				"</dependency>";
	}
	
	/**
	 * 
	 * @param f
	 * @return
	 */
	private static boolean isLibrary(File f) {
		if (f.getAbsolutePath().endsWith(".jar"))
			return true;
		
		return false;
	}
	
}