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
import java.io.FilenameFilter;

import org.apache.commons.io.FileUtils;
import org.eumetsat.dcpc.commons.xml.SimpleNamespaceContext;
import org.eumetsat.dcpc.commons.xml.XPathExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//-----------------------------------------------
//Class:            RenameMetadataFiles
//-----------------------------------------------
/**
 * For every XML metadata file within the input directory:<br>
 * - It extracts the string value at the <code><fileIdentifier></code> element<br>
 * - It renames the file to the string value and <code>.xml</code> extension.
 */
public class MetadataFileRenamer
{  
    public final static Logger logger = LoggerFactory.getLogger(MetadataFileRenamer.class);
    
    private static final SimpleNamespaceContext ms_NamespaceContext = new SimpleNamespaceContext();
    
    // TODO To be put in a configuration file with the namespaces
    private static final String ms_XPathExprStr = "gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString";
    
    static
    {
        ms_NamespaceContext.addNamespace("gmd", "http://www.isotc211.org/2005/gmd");
        ms_NamespaceContext.addNamespace("gco", "http://www.isotc211.org/2005/gco");
        ms_NamespaceContext.addNamespace("gmi", "http://www.isotc211.org/2005/gmi");
        ms_NamespaceContext.addNamespace("gml", "http://www.opengis.net/gml");
        ms_NamespaceContext.addNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        
    }
    
    // list of XML metadata files within the input directory
    private File[] oListFiles      = null;
    // string path of the inputDirectory
    private String strInputDirPath = null;
    
    /**
     * Create a new object of <code>RenameMetadataFiles</code> class, and
     * extract the list of XML files at the input directory
     * 
     * @param inputDir
     *            The input directory where the metadata files are stored.
     */
    public MetadataFileRenamer(File inputDir)
    {
        this.strInputDirPath = inputDir.getPath();
        this.oListFiles = inputDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name)
            {
                return name.endsWith(".xml");
            }
        });
        logger.info("{} files to rename ", this.oListFiles.length);
    }

    /**
     * Iterates through the list of files, get name with XPath and rename
     * it as of <code><fileIdenfitifier></code> element.
     * @throws Exception 
     */
    public void processFiles() throws Exception
    {
        String fileName = null;
        File newFile = null;
        
        for (File file : this.oListFiles)
        {
            fileName = this.extractName(file);
            //System.out.println("Extracted name: " + fileName);

            if (fileName != null)
            {
                newFile = new File(this.strInputDirPath + File.separator + fileName + ".xml");
                //System.out.println("new File: " + newFile.toString());
                
                FileUtils.moveFile(file, newFile);          
            }
        }
    }
    
    public String extractName(File aFile) throws Exception
    {        
        
        // preconditions
        if (aFile == null)
            throw new Exception("Error invalid File");
        
        XPathExtractor xpathExtractor = new XPathExtractor();
        
        xpathExtractor.setXPathExpression(ms_XPathExprStr, ms_NamespaceContext);
        
        String result = xpathExtractor.evaluateAsString(aFile);
        
        return result;
    }

    /**
     * Iterates through the XML metadata files within the given input directory
     * and rename them. Use: <code>@{@class}</code> <input dir>
     * 
     * @param args
     *            - 1 argument is expected, the path to the input directory
     */
    public static void main(String[] args)
    {

        if (args == null || args.length <= 0 || args[0].equalsIgnoreCase(""))
        {
            System.out.println("Usage: " + MetadataFileRenamer.class.getName()
                    + " <input dir>");
            System.exit(1);
        }

        try
        {
            File inputDir = new File(args[0]);
            if (inputDir.isDirectory())
            {
                MetadataFileRenamer renameMD = new MetadataFileRenamer(inputDir);
                renameMD.processFiles();
            }
            else
            {
                System.out
                        .println("The argument specified is not a valid input directory: "
                                + args[0]);
            }

        }
        catch (Exception e)
        {
            System.out
                    .println("An error occurred while processing the files of directory: "
                            + args[0]);
            e.printStackTrace();
            System.exit(1);
        }
    }

}
