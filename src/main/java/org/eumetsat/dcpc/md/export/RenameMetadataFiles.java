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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.eumetsat.dcpc.commons.xml.SimpleNamespaceContext;
import org.eumetsat.dcpc.commons.xml.XPathExtractor;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

//-----------------------------------------------
//Class:            RenameMetadataFiles
//-----------------------------------------------
/**
 * For every XML metadata file within the input directory:<br>
 * - It extracts the string value at the <code><fileIdentifier></code> element<br>
 * - It renames the file to the string value and <code>.xml</code> extension.
 */
public class RenameMetadataFiles
{
    // list of XML metadata files within the input directory
    private File[] oListFiles      = null;
    // string path of the inputDirectory
    private String strInputDirPath = null;
    
    private static final SimpleNamespaceContext ms_NamespaceContext = new SimpleNamespaceContext();
    
    // TODO To be put in a configuration file with the namespaces
    private static final String XPathExprStr = "gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString";
    
    static
    {
        ms_NamespaceContext.addNamespace("gmd", "http://www.isotc211.org/2005/gmd");
        ms_NamespaceContext.addNamespace("gco", "http://www.isotc211.org/2005/gco");
        ms_NamespaceContext.addNamespace("gmi", "http://www.isotc211.org/2005/gmi");
        ms_NamespaceContext.addNamespace("gml", "http://www.opengis.net/gml");
        ms_NamespaceContext.addNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        
    }
    
    /**
     * Create a new object of <code>RenameMetadataFiles</code> class, and
     * extract the list of XML files at the input directory
     * 
     * @param inputDir
     *            The input directory where the metadata files are stored.
     */
    public RenameMetadataFiles(File inputDir)
    {
        this.strInputDirPath = inputDir.getPath();
        this.oListFiles = inputDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name)
            {
                return name.endsWith(".xml");
            }
        });
        System.out.println("Found " + this.oListFiles.length + " XML files.");
    }

    /**
     * Iterates through the list of files, parses it with XMLparser and rename
     * it as of <code><fileIdenfitifier></code> element.
     */
    private void processFiles()
    {
        String fileName = null;
        File newFile = null;
        XMLInputFactory xmlInFactory = XMLInputFactory.newInstance();

        for (int i = 0; i < this.oListFiles.length; i++)
        {
            fileName = extractName(this.oListFiles[i], xmlInFactory);
            // System.out.println("Extracted name: " + fileName);

            if (fileName != null)
            {
                newFile = new File(this.strInputDirPath + File.separator
                        + fileName + ".xml");
                System.out.println("new File: " + newFile.toString());
                boolean success = this.oListFiles[i].renameTo(newFile);
                System.out.println("Renaming has successed... " + success);
            }
        }
    }
    
    public static String extractName(File aFile) throws Exception
    {        
        XPathExtractor xpathExtractor = new XPathExtractor();
        
        SimpleNamespaceContext ns = xpathExtractor.getNewNamespaceContext();
        ns.addNamespace("gmd", "http://www.isotc211.org/2005/gmd");
        ns.addNamespace("gco", "http://www.isotc211.org/2005/gco");
        ns.addNamespace("gmi", "http://www.isotc211.org/2005/gmi");
        ns.addNamespace("gml", "http://www.opengis.net/gml");
        ns.addNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        
        xpathExtractor.setXPathExpression("gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString", ns);
        
        String result = xpathExtractor.evaluateAsString(new File("H:/10.xml"));
        
        System.out.println(result);
        
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
            System.out.println("Usage: " + RenameMetadataFiles.class.getName()
                    + " <input dir>");
            System.exit(1);
        }

        try
        {
            File inputDir = new File(args[0]);
            if (inputDir.isDirectory())
            {
                RenameMetadataFiles renameMD = new RenameMetadataFiles(inputDir);
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
