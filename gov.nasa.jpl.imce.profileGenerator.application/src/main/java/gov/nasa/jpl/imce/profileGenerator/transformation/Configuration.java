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

import java.lang.Boolean;
import java.lang.String;

public class Configuration {

	/** Default input file. */
	public static String inputFile =
			"dynamicScripts/gov.nasa.jpl.imce.profileGenerator.application/project-bundle.json";

	/** Default output file. */
	public static String outputFile =
			"dynamicScripts/gov.nasa.jpl.imce.profileGenerator.application/output.mdzip";

	/** Directory to place generated profiles / files into (will be created if non-existent). */
	public static String outputDir =
			"dynamicScripts/gov.nasa.jpl.imce.profileGenerator.application/target/md.plugin/profiles/";

	/** Default template to be used for profiles. */
	public static String template =
			"dynamicScripts/gov.nasa.jpl.imce.profileGenerator.application/resources/profile-template.mdzip";

	/** Whether to load MagicDraw's GUI or not if executed in standalone mode. */
	public static Boolean silent = false;

	/** Whether to generate validation procedures (generally). */
	public static Boolean generateValidation = true;

	/** Whether to generate MD customizations for validation. */
	public static Boolean generateValidationCustomizations = true;

	/** Whether to generate OCL validation rules. */
	public static Boolean generateValidationOCLValidationSuite = true;

	/** Setting this to true will cause the profile generator to create companion aspects. */
	public static Boolean generateCompanionAspects = false;

}