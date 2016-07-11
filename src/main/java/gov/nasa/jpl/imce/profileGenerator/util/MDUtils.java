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

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.core.project.ProjectDescriptorsFactory;
import com.nomagic.runtime.ApplicationExitedException;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;

import gov.nasa.jpl.imce.profileGenerator.transformation.Configuration;
import main.scala.gov.nasa.jpl.imce.profileGenerator.util.ProjectUsageIntegrityUtilities;

/**
 * @author sherzig
 *
 */
public class MDUtils {
	/**
	 *
	 * @param args
	 */
	public static void launchMagicDraw(String args[]) {
		// launch MagicDraw in batch mode
		Application magicdrawApplication = Application.getInstance();

		System.out.println("Starting MD...");
		try {
			// Invoke MagicDraw - necessary with GUI to show license server options
			// (workaround failed / needs further investigation)
			// .start(visible?, silent?, tryLoadProject?, args?, participant?)
			// Interactive (as command line option?) = visible && !silent
			// Server mode = !visible && silent
			// WILL NOT ASK FOR LICENSE SERVER IF "SILENT"!
			magicdrawApplication.start(!Configuration.silent, Configuration.silent, false, args, null);
		} catch (ApplicationExitedException e) {
			e.printStackTrace();
			System.out.println(e.toString());
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println(e.toString());
		}

		System.out.println("Started MD");
	}

	public static void shutdownMagicDraw() {
		Application magicdrawApplication = Application.getInstance();
		try {
			magicdrawApplication.shutdown();
		} catch (ApplicationExitedException e) {
			e.printStackTrace();
			System.out.println(e.toString());
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println(e.toString());
		}
	}

	/**
	 *
	 * @param filename
	 */
	public static void loadProject(String filename) {
		File sysmlfile = new File(filename);
		ProjectDescriptor projectDescriptor = ProjectDescriptorsFactory
				.createProjectDescriptor(sysmlfile.toURI());
		Application.getInstance().getProjectsManager().loadProject(projectDescriptor, true);
	}

	public static void saveProject() {
		ProjectDescriptor projectDescriptor = ProjectDescriptorsFactory
				.getDescriptorForProject(Application.getInstance().getProjectsManager().getActiveProject());
		Application.getInstance().getProjectsManager().saveProject(projectDescriptor, true);
	}

	public static void closeActiveProject() {
		Application.getInstance().getProjectsManager().closeProject();
	}

	public static Model getModelInActiveProject() {
		return Application.getInstance().getProjectsManager().getActiveProject().getModel();
	}

}