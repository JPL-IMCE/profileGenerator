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
package gov.nasa.jpl.imce.profileGenerator.runner

/**
  * Created by sherzig on 7/18/16.
  */
import java.awt.event.ActionEvent
import java.io.{File, FileOutputStream}
import java.nio.file.{Files, Paths}

import com.nomagic.magicdraw.core.Project
import com.nomagic.magicdraw.core.Application
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes.MainToolbarMenuAction
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
import gov.nasa.jpl.imce.profileGenerator.io.{BundleDigestReader, JSONBundleDigestReader, MDUMLProfileWriter}
import gov.nasa.jpl.imce.profileGenerator.model.bundle.BundleDigest
import gov.nasa.jpl.imce.profileGenerator.model.profile.Package
import gov.nasa.jpl.imce.profileGenerator.transformation.{Bundle2ProfileMappings, Configuration}

import scala.{Option}
import scala.util.Try

object GenerateProfile {

  def generateProfile
  ( p: Project, ev: ActionEvent, script: MainToolbarMenuAction )
  : Try[Option[MagicDrawValidationDataResults]]
  = {
    // Dialog
    Files.copy(Paths.get(Configuration.template), new FileOutputStream(new File(Configuration.outputFile)))

    val bundleReader: BundleDigestReader = new JSONBundleDigestReader
    bundleReader.openBundle(Configuration.inputFile)
    //JSONBundleDigestReader bundleReader = new JSONBundleDigestReader();
    //bundleReader.openBundle("test/project-bundle.json");
    val bundle: BundleDigest = bundleReader.readBundleModel

    val mappings: Bundle2ProfileMappings = new Bundle2ProfileMappings

    val profilePackage: Package = mappings.bundleToProfile(bundle)

    val mdUMLProfileWriter: MDUMLProfileWriter = new MDUMLProfileWriter
    mdUMLProfileWriter.writeModel(profilePackage)

    null
  }

}