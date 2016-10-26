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
package gov.nasa.jpl.imce.profileGenerator.util

import gov.nasa.jpl.magicdraw.projectUsageIntegrity.ProjectUsageIntegrityPlugin
import gov.nasa.jpl.magicdraw.projectUsageIntegrity.commands.ComputeProjectUsageGraphCommand
import java.util.List
import java.util.Set

import scala.Unit
import scala.Boolean
import scala.Predef.String
import scala.collection.JavaConversions._
import com.nomagic.actions.NMAction
import com.nomagic.magicdraw.annotation.Annotation
import com.nomagic.magicdraw.core.Application
import com.nomagic.magicdraw.core.GUILog
import com.nomagic.magicdraw.core.Project
import com.nomagic.magicdraw.core.project.ProjectsManager
import com.nomagic.magicdraw.openapi.uml.SessionManager

/**
 * @author nicolas.f.rouquette@jpl.nasa.gov
 * @author sebastian.j.herzig@jpl.nasa.gov
 */
class ProjectUsageIntegrityUtilities(project: Project) {

	val projectUsageIntegrityPlugin: ProjectUsageIntegrityPlugin = ProjectUsageIntegrityPlugin.getInstance()
	val projectsManager: ProjectsManager = Application.getInstance().getProjectsManager()
	val a: Application = Application.getInstance()
	val log: GUILog = a.getGUILog()
	var iterations = 0
	var repairs = 0

	val iProject = project.getPrimaryProject()
	val integrityHelper = projectUsageIntegrityPlugin.getSSCAEProjectUsageIntegrityProfileForProject(project)

	if (null == integrityHelper)
			throw new java.lang.IllegalArgumentException("The SSCAE ProjectUsage Integrity Checker plugin must be enabled!")

	val sessionManager: SessionManager = SessionManager.getInstance()

	/**
		* Mount PUIC profile
		*/
	/*def mountPUICProfile(): Unit = {
		MDUMLModelUtils.mountProfile("target/md.package/profiles/SSCAEProjectUsageIntegrityProfile.mdzip")
	}*/

	/**
		* Run repairs
		*/
	def runSSCAEValidationAndRepairs(): Unit = {
		if (sessionManager.isSessionCreated())
			sessionManager.closeSession()

		val showProjectUsageDiagnosticModalDialog = false
		val c: ComputeProjectUsageGraphCommand = new ComputeProjectUsageGraphCommand(project, showProjectUsageDiagnosticModalDialog)
		c.run()

		var iterations = 0

		var checkForRepairs = true

		while (checkForRepairs) {
			checkForRepairs = false
			
			iterations += 1
			//log.log(String.format("(iteration=%d)", iterations));

			var annotations: Set[Annotation] = integrityHelper.runSSCAEProjectStereotypeValidationRule()
			checkForRepairs = process(checkForRepairs, annotations, "runSSCAEProjectStereotypeValidationRule")
			c.run()

			annotations = integrityHelper.runSSCAEValidProjectUsageGraphRule()
			checkForRepairs = process(checkForRepairs, annotations, "runSSCAEValidProjectUsageGraphRule")
			c.run()

			annotations = integrityHelper.runSSCAEProjectUsageRelationshipRule()
			checkForRepairs = process(checkForRepairs, annotations, "runSSCAEProjectUsageRelationshipRule")
			c.run()

			//log.log(String.format("==> %d repairs in %d iterations", repairs, iterations));
		}
	}

	/**
		* Process a set of errors marked in the model by the PUIC (stored as annotations).
		*
		* @param checkForRepairs
		* @param annotations
		* @param description
    * @return
    */
	def process(checkForRepairs: Boolean, annotations: Set[Annotation], description: String): Boolean = {
		var chkForRepairs = checkForRepairs

		//log.log(String.format("=> %d annotations from %s", annotations.size(), description));
		for ((annotation: Annotation) <- annotations.toList) {
			val actions: List[_ <: NMAction] = annotation.getActions()

			//log.log(String.format("==> %s", describe(a)));
			for ((action: NMAction) <- actions.toList) {
				repairs += 1
				//val message : String = String.format("(repair=%d, iteration=%d) %s: %s", repairs, iterations, description, annotation.getText())
				//qvtLog.log(message);
				//log.log(message);

				action.actionPerformed(null)

				/*if (!*/integrityHelper.checkIProjectResources(iProject)//)

					//log.log(message + "\n *** Some project resources have lost their project relationship after sharing the package!");
					//AbstractMDHelper._reportProblem_(context, message + "\n *** Some project resources have lost their project relationship after sharing the package!");

				chkForRepairs = true
			}
		}

		chkForRepairs
	}

}