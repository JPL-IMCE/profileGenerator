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
package gov.nasa.jpl.imce.profileGenerator.tests

import java.nio.file.Path

import junit.framework.Test
import gov.nasa.jpl.imce.magicdraw.dynamicscripts.batch.ExecuteDynamicScriptAsMagicDrawUnitTest
import gov.nasa.jpl.imce.magicdraw.dynamicscripts.batch.json.MagicDrawTestSpec

object ExecuteUnitTests {

  /**
    * Create the test suite for this project's unit tests.
    *
    * Note that the magicdraw unit tests created must be
    * based on unit test classes defined in this project.
    *
    * @return Unit test suite.
    */
  def suite
  : Test
  = ExecuteDynamicScriptAsMagicDrawUnitTest.makeTestSuite(
    (p: Path, spec: MagicDrawTestSpec) => new ExecuteUnitTests(p, spec)
  )

}

/**
  * Project-specific MagicDraw Unit Test.
  *
  * In SBT, the compile & test libraries produce different artifacts (*.jar, *-tests.jar)
  * Compile libraries are public.
  * Test libraries are private.
  *
  * The difference means that SBT does not scan dependencies on other test libraries for unit tests.
  * SBT only scans the project's test library for unit tests.
  *
  * This means that to run SBT unit tests in a given project, it is necessary to define a unit test class.
  *
  * This is the idiom to do it in a simple way.
  *
  * @param resultsDir test result directory
  * @param spec MagicDraw unit test specification read from a MagicDrawTestSpec Json file in `resources/CITests`.
  *             (see `specsRoot` variable in `build.sbt`)
  */
class ExecuteUnitTests
( resultsDir: Path,
  spec: MagicDrawTestSpec )
  extends ExecuteDynamicScriptAsMagicDrawUnitTest(resultsDir, spec)