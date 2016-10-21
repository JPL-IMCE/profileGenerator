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
package gov.nasa.jpl.imce.profileGenerator.util;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by sherzig on 7/27/16.
 */
public class PackagingUtils {

    static final int BUFFER = 2048;

    /**
     * Creates a zip archive with all files contained in a specified directory (including the directory
     * specified itself). E.g., if the following directory structure exists:
     *
     *   target/A/B.java
     *
     * and "target" is passed, then a zip file will be created with three entries:
     *
     *   target
     *   target/A
     *   target/A/B.java
     *
     * @param directory
     * @param targetArchiveFilename
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void zip(File directory, String targetArchiveFilename)
            throws FileNotFoundException, IOException {
        BufferedInputStream origin = null;
        byte data[] = new byte[BUFFER];

        System.out.println("Creating zip archive " + targetArchiveFilename + "...");

        FileOutputStream dest = new
                FileOutputStream(targetArchiveFilename);
        ZipOutputStream out = new
                ZipOutputStream(new BufferedOutputStream(dest));

        ArrayList<File> filesToZip = new ArrayList<File>();

        // Add directory itself
        filesToZip.add(directory);

        // And all contained files
        filesToZip.addAll(collectContainedFiles(directory));

        for (File f : filesToZip) {
            System.out.println("... adding: " + f + " to zip archive");

            FileInputStream fi = new FileInputStream(f);

            origin = new BufferedInputStream(fi, BUFFER);

            // create zip entry
            ZipEntry entry = new ZipEntry(f.getAbsolutePath());

            // add entries to ZIP file
            out.putNextEntry(entry);

            // Write data to zip file
            int count;
            while ((count = origin.read(data, 0,
                    BUFFER)) != -1) {
                out.write(data, 0, count);
            }

            origin.close();
        }

        out.close();

        System.out.println("Finished writing zip archive " + targetArchiveFilename + ".");
    }

    /**
     * Perform a depth first search to collect all files contained in a directory and any subdirectories.
     *
     * @param directory
     * @return
     */
    private static ArrayList<File> collectContainedFiles(File directory) {
        ArrayList<File> files = new ArrayList<File>();

        files.addAll(Arrays.asList(directory.listFiles()));

        for (File f : files)
            if (f.isDirectory())
                files.addAll(collectContainedFiles(f));

        return files;
    }

}
