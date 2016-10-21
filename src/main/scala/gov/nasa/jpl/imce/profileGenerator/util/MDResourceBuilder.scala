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

//import java.io.File

/**
  * Created by sherzig on 7/29/16.
  */
object MDResourceBuilder {
/*
  val root = "target" / "mdresource"
  val resourceManager = root / "data" / "resourcemanager"

  def buildDescriptorFile = {
    val resourceDescriptorFile = resourceManager / "MDR_IMCE_ProfilesLibraries_74997_descriptor.xml"
    val resourceDescriptorInfo =
      <resourceDescriptor critical="false" date={d}
                          description="IMCE Profiles &amp; Libraries"
                          group="IMCE Resource"
                          homePage="https://github.jpl.nasa.gov/imce/imce.qvto.profileGenerator"
                          id="74997"
                          mdVersionMax="higher"
                          mdVersionMin="18.0"
                          name="IMCEProfiles"
                          product="IMCE Profiles And Libraries"
                          restartMagicdraw="false" type="Profile">
        <version human={Versions.version} internal={Versions.version} resource={Versions.version + "0"}/>
        <provider email="sebastian.j.herzig@jpl.nasa.gov"
                  homePage="https://github.jpl.nasa.gov/imce"
                  name="IMCE"/>
        <edition>Reader</edition>
        <edition>Community</edition>
        <edition>Standard</edition>
        <edition>Professional Java</edition>
        <edition>Professional C++</edition>
        <edition>Professional C#</edition>
        <edition>Professional ArcStyler</edition>
        <edition>Professional EFFS ArcStyler</edition>
        <edition>OptimalJ</edition>
        <edition>Professional</edition>
        <edition>Architect</edition>
        <edition>Enterprise</edition>
        <installation>
          <file from="modelLibraries/IMCE/IMCE.DC.mdzip"
                to="modelLibraries/IMCE/IMCE.DC.mdzip"/>
          <file from="modelLibraries/IMCE/IMCE.DI.mdzip"
                to="modelLibraries/IMCE/IMCE.DI.mdzip"/>
          <file from="modelLibraries/IMCE/IMCE.IEC80000-13 Information Science and Technology.mdzip"
                to="modelLibraries/IMCE/IMCE.IEC80000-13 Information Science and Technology.mdzip"/>
          <file from="modelLibraries/IMCE/IMCE.IEC80000-6 Electromagnetism.mdzip"
                to="modelLibraries/IMCE/IMCE.IEC80000-6 Electromagnetism.mdzip"/>
          <file from="modelLibraries/IMCE/IMCE.ISO-80000-All.mdzip"
                to="modelLibraries/IMCE/IMCE.ISO-80000-All.mdzip"/>
          <file from="modelLibraries/IMCE/IMCE.ISO80000-1 General.mdzip"
                to="modelLibraries/IMCE/IMCE.ISO80000-1 General.mdzip"/>
          <file from="modelLibraries/IMCE/IMCE.ISO80000-10 Atomic and Nuclear Physics.mdzip"
                to="modelLibraries/IMCE/IMCE.ISO80000-10 Atomic and Nuclear Physics.mdzip"/>
          <file from="modelLibraries/IMCE/IMCE.ISO80000-3 Space and Time.mdzip"
                to="modelLibraries/IMCE/IMCE.ISO80000-3 Space and Time.mdzip"/>
          <file from="modelLibraries/IMCE/IMCE.ISO80000-4 Mechanics.mdzip"
                to="modelLibraries/IMCE/IMCE.ISO80000-4 Mechanics.mdzip"/>
          <file from="modelLibraries/IMCE/IMCE.ISO80000-5 Thermodynamics.mdzip"
                to="modelLibraries/IMCE/IMCE.ISO80000-5 Thermodynamics.mdzip"/>
          <file from="modelLibraries/IMCE/IMCE.ISO80000-7 Light.mdzip"
                to="modelLibraries/IMCE/IMCE.ISO80000-7 Light.mdzip"/>
          <file from="modelLibraries/IMCE/IMCE.ISO80000-9 Physical Chemistry and Molecular Physics.mdzip"
                to="modelLibraries/IMCE/IMCE.ISO80000-9 Physical Chemistry and Molecular Physics.mdzip"/>
          <file from="modelLibraries/IMCE/IMCE.SysMLDI.mdzip"
                to="modelLibraries/IMCE/IMCE.SysMLDI.mdzip"/>
          <file from="modelLibraries/IMCE/IMCE.UML2.5.mdzip"
                to="modelLibraries/IMCE/IMCE.UML2.5.mdzip"/>
          <file from="modelLibraries/IMCE/IMCE.UMLDI.mdzip"
                to="modelLibraries/IMCE/IMCE.UMLDI.mdzip"/>
          <file from="modelLibraries/IMM/XMLSchema Metamodel.mdzip"
                to="modelLibraries/IMM/XMLSchema Metamodel.mdzip"/>
          <file from="profiles/IMCE/IMCE.BlockSpecificTypeModelingAndAnalysis.mdzip"
                to="profiles/IMCE/IMCE.BlockSpecificTypeModelingAndAnalysis.mdzip"/>
          <file from="profiles/IMCE/IMCE.owl2-mof2.mdzip"
                to="profiles/IMCE/IMCE.owl2-mof2.mdzip"/>
          <file from="profiles/IMCE/QVTOValidation.mdzip"
                to="profiles/IMCE/QVTOValidation.mdzip"/>
          <file from="profiles/IMCEOntologyBundles/imce.jpl.nasa.gov/foundation/project/project-bundle.mdzip"
                to="profiles/IMCEOntologyBundles/imce.jpl.nasa.gov/foundation/project/project-bundle.mdzip"/>
          <file from="profiles/IMCEOntologyBundles/imce.jpl.nasa.gov/www.omg.org/spec/SysML/20140311/Metrology-bundle.mdzip"
                to="profiles/IMCEOntologyBundles/imce.jpl.nasa.gov/www.omg.org/spec/SysML/20140311/Metrology-bundle.mdzip"/>
        </installation>
      </resourceDescriptor>


    xml.XML.save(
      filename=resourceDescriptorFile.getAbsolutePath,
      node=resourceDescriptorInfo,
      enc="UTF-8")
  }*/

}