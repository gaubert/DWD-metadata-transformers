//------------------------------------------------------------------------------
//Copyright (C) 2010 by EUMETSAT 
//------------------------------------------------------------------------------
//
//Project:            DCPC role - Metadata exchange
//Component:          org.eumetsat.dcpc.md.export
//Module:             RenameMetadataFiles.java
//
/** @author           Martinez
 */
//Creation Date:     30 Apr 2010
//
//------------------------------------------------------------------------------
//Version:            1
//------------------------------------------------------------------------------
//
/** @version
 *   1  Martinez    30/04/2010  Initial version.
 */

package org.eumetsat.dcpc.md.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

//-----------------------------------------------
//Class:            RenameMetadataFiles
//-----------------------------------------------
/**
 * For every XML metadata file within the input directory:<br> 
 * - It extracts the string value at the <code><fileIdentifier></code> element<br>
 * - It renames the file to the string value and <code>.xml</code> extension.  
 */
public class RenameMetadataFiles {
    // list of XML metadata files within the input directory
    private File[] oListFiles = null;
    // string path of the inputDirectory
    private String strInputDirPath = null;
    
    /**
     * Create a new object of <code>RenameMetadataFiles</code> class, and extract
     * the list of XML files at the input directory 
     * @param inputDir The input directory where the metadata files are stored.
     */
    public RenameMetadataFiles(File inputDir) {
        this.strInputDirPath = inputDir.getPath();
        this.oListFiles = inputDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        });
        System.out.println("Found " + this.oListFiles.length + " XML files.");
    }

    /**
     * Iterates through the list of files, parses it with XMLparser and 
     * rename it as of <code><fileIdenfitifier></code> element.
     */
    private void processFiles() {
        String fileName = null;
        File newFile = null;
        XMLInputFactory xmlInFactory = XMLInputFactory.newInstance();  
        
        for (int i=0; i < this.oListFiles.length; i++) {
            fileName = extractName(this.oListFiles[i], xmlInFactory);
            //System.out.println("Extracted name: " + fileName);
            
            if (fileName != null) {
                newFile = new File(this.strInputDirPath + File.separator + fileName + ".xml");
                System.out.println("new File: " + newFile.toString());
                boolean success = this.oListFiles[i].renameTo(newFile);
                System.out.println("Renaming has successed... " + success);
            }
        }
    }
    
    /**
     * Parses the XML file for getting the value of the <code>fileIdentifier</code> 
     * element.
     * @param file XML file to parse
     * @param xmlInFactory Factory to get instances of XMLStreamReaders
     * @return The string value of the <code>fileIdentifier</code> or <code>null</code> 
     * if the value cannot be extracted 
     */
    private String extractName(File file, XMLInputFactory xmlInFactory) {
        String extractedName = null;
        //System.out.println("Extractig name from " + file.toString());
        
        try {
            // it is needed the 'inputStream' var to be able to close the file
            // afterwards, otherwise the renaming fails...
            FileInputStream inputStream = new FileInputStream(file);
            XMLStreamReader reader = xmlInFactory.createXMLStreamReader(inputStream);
            boolean found = false;
            
            while (!found && (reader.hasNext())) {
                int event = reader.next();
                if (event == XMLStreamReader.START_ELEMENT) {
                    if (reader.getLocalName().equalsIgnoreCase("fileIdentifier")) {
                        //read the next element, <gco:CharacterString>
                        reader.nextTag();
                        if (reader.getLocalName().equalsIgnoreCase("CharacterString")) {
                            extractedName = reader.getElementText();
                            found = true;
                        } else {
                            System.out.println("Wrong element read: " + extractedName);
                        }
                    }
                }
            }
            reader.close();
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            extractedName = null;
        } catch (XMLStreamException e) {
            e.printStackTrace();
            extractedName = null;
        } catch (IOException e) {
            e.printStackTrace();
            extractedName = null;
        }
        return extractedName;
    }

    /**
     * Iterates through the XML metadata files within the given input directory and
     * rename them.
     * Use: <code>@{@class}</code> <input dir>
     * @param args - 1 argument is expected, the path to the input directory
     */
    public static void main(String[] args) {
        
        if (args == null || args.length<=0 || args[0].equalsIgnoreCase("")) {
            System.out.println("Usage: " + RenameMetadataFiles.class.getName() + " <input dir>");
            System.exit(1);
        }
        
        try {
            File inputDir = new File(args[0]);
            if (inputDir.isDirectory()) {
                RenameMetadataFiles renameMD = new RenameMetadataFiles(inputDir);
                renameMD.processFiles();
            } else {
                System.out.println("The argument specified is not a valid input directory: " + args[0]);
            }
            
        }
        catch (Exception e) {
            System.out.println("An error occurred while processing the files of directory: " + args[0]);
            e.printStackTrace();
            System.exit(1);
        }
    }

}
