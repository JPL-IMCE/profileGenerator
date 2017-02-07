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
package gov.nasa.jpl.imce.profileGenerator.batch.tests

import gov.nasa.jpl.imce.profileGenerator.io.{JSONBundleDigestReader, MDUMLProfileWriter}
import gov.nasa.jpl.imce.profileGenerator.transformation.{Bundle2ProfileMappings, Configuration}
import gov.nasa.jpl.imce.profileGenerator.util.MDUtils
import org.scalatest.{FlatSpec, Matchers}

import java.nio.file.{Files, Paths}
import java.io.{File, FileOutputStream}

import scala.language.reflectiveCalls
import scala.Array

class RunProfileGenerator extends FlatSpec with Matchers {
  def fixture =
    new {
      // Test configuration
      Configuration.silent = java.lang.Boolean.valueOf(true)
      Configuration.template = "dynamicScripts/gov.nasa.jpl.imce.profileGenerator.application/resources/profile-template.mdzip"
      Configuration.outputDir = "../profiles/"
      Configuration.inputFile = "../../project-bundle.json"
      Configuration.outputFile = "../output.mdzip"

      // Objects
      val bundleReader = new JSONBundleDigestReader
      val mappings = new Bundle2ProfileMappings
      val mdUMLProfileWriter = new MDUMLProfileWriter
    }

  /**
   * Main test case, responsible for executing the profile generator
   */
  it should "produce profiles" in {
    val f = fixture

    Files.copy(Paths.get(Configuration.template), new FileOutputStream(new File(Configuration.outputFile)))

    val args : Array[java.lang.String] = Array()

    MDUtils.launchMagicDraw(args)

    //java.lang.Thread.sleep(15000)

    f.bundleReader.openBundle(Configuration.inputFile)

    val bundle = f.bundleReader.readBundleModel
    val profilePackage = f.mappings.bundleToProfile(bundle)

    Configuration.silent = java.lang.Boolean.valueOf(false)
    val element = f.mdUMLProfileWriter.writeModel(profilePackage)

    assert(null != element)
  }
}

//object RunProfileGenerator {
//
//  def suite
//  : Test
//  = ExecuteDynamicScriptAsMagicDrawUnitTest.makeTestSuite(
//    (p: Path, spec: MagicDrawTestSpec) => new RunProfileGenerator(p, spec)
//  )
//
//}
//
//class RunProfileGenerator
//( resultsDir: Path,
//  spec: MagicDrawTestSpec )
//extends ExecuteDynamicScriptAsMagicDrawUnitTest(resultsDir, spec)
