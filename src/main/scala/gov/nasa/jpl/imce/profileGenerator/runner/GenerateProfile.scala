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
import gov.nasa.jpl.dynamicScripts.DynamicScriptsTypes.MainToolbarMenuAction
import gov.nasa.jpl.dynamicScripts.magicdraw.validation.MagicDrawValidationDataResults
import gov.nasa.jpl.imce.profileGenerator.io.{BundleDigestReader, JSONBundleDigestReader, MDUMLProfileWriter}
import gov.nasa.jpl.imce.profileGenerator.model.bundle.BundleDigest
import gov.nasa.jpl.imce.profileGenerator.model.profile.Package
import gov.nasa.jpl.imce.profileGenerator.transformation.{Bundle2ProfileMappings, Configuration}

import scala.{Array,Option}
import scala.util.Try
import scala.Predef.refArrayOps

import java.lang.System

object GenerateProfile {

  /**
    * Default profile generation function - this will generate a single profile as specified
    * by the Configuration class values.
    *
    * @param p
    * @param ev
    * @param script
    * @return
    */
  def generateProfile
  ( p: Project, ev: ActionEvent, script: MainToolbarMenuAction )
  : Try[Option[MagicDrawValidationDataResults]]
  = {
    produceSingleProfile(new File(Configuration.inputFile))
  }

  /**
    * Produce all profiles for all digests found in a particular subdirectory.
    *
    * @param p
    * @param ev
    * @param script
    * @return
    */
  def generateAllProfiles
  ( p: Project, ev: ActionEvent, script: MainToolbarMenuAction )
  : Try[Option[MagicDrawValidationDataResults]]
  = {
    // Collect a list of all files in a particular subdirectory
    def collectFiles(dir : File) : Array[File] = {
      val these = dir.listFiles
      these ++ these.filter(_.isDirectory).flatMap(collectFiles)
    }

    // Filter the list of files in a subdirectory by the extension used by digests (here: json)
    val digests = collectFiles(new File("resources/digests")).filter(f => f.getAbsoluteFile.toString.endsWith(".json"))

    // Create the various profiles, and package
    digests.map(d => produceSingleProfile(d))

    null
  }

  /**
    * Produce a single profile.
    *
    * @param inputFile
    * @return
    */
  def produceSingleProfile(inputFile : File) = {
    if (!inputFile.exists()) {
      System.out.println("[ERROR] Specified input bundle digest does not exist at " + inputFile.getAbsolutePath)
    }
    else if (!(new File(Configuration.template)).exists()) {
      System.out.println("[ERROR] Specified template file does not exist at " + (new File(Configuration.template)).getAbsolutePath)
    }
    else {
      Files.copy(Paths.get(Configuration.template), new FileOutputStream(new File(Configuration.outputFile)))

      val bundleReader: BundleDigestReader = new JSONBundleDigestReader
      bundleReader.openBundle(inputFile.getAbsolutePath)

      val bundle: BundleDigest = bundleReader.readBundleModel

      val mappings: Bundle2ProfileMappings = new Bundle2ProfileMappings

      val profilePackage: Package = mappings.bundleToProfile(bundle)

      val mdUMLProfileWriter: MDUMLProfileWriter = new MDUMLProfileWriter
      mdUMLProfileWriter.writeModel(profilePackage)
    }

    null
  }

}