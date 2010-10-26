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

package org.eumetsat.eoportal.dcpc.md.export;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.eumetsat.eoportal.dcpc.commons.DateUtil;
import org.eumetsat.eoportal.dcpc.commons.xml.SimpleNamespaceContext;
import org.eumetsat.eoportal.dcpc.commons.xml.XPathExtractor;
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
    public final static Logger                  logger              = LoggerFactory
                                                                            .getLogger(MetadataFileRenamer.class);

    private static final SimpleNamespaceContext ms_NamespaceContext = new SimpleNamespaceContext();

    // TODO To be put in a configuration file with the namespaces
    private static final String                 ms_XPathGetName     = "gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString";

    private static final String                 ms_XPathGetDate     = "gmd:MD_Metadata/gmd:dateStamp/gco:Date";

    static
    {
        ms_NamespaceContext.addNamespace("gmd",
                "http://www.isotc211.org/2005/gmd");
        ms_NamespaceContext.addNamespace("gco",
                "http://www.isotc211.org/2005/gco");
        ms_NamespaceContext.addNamespace("gmi",
                "http://www.isotc211.org/2005/gmi");
        ms_NamespaceContext.addNamespace("gml", "http://www.opengis.net/gml");
        ms_NamespaceContext.addNamespace("xsi",
                "http://www.w3.org/2001/XMLSchema-instance");

    }

    // list of XML metadata files within the input directory
    private File[]                              oListFiles          = null;
    // string path of the inputDirectory
    private String                              strInputDirPath     = null;

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

    }

    /**
     * checkt that the files are XSLT transformed files
     * 
     * @param aNbOfFilesToCheck
     * @throws Exception
     */
    public void doSanityCheck(int aNbOfFilesToCheck) throws Exception
    {
        XPathExtractor xpathExtractor = new XPathExtractor();

        xpathExtractor.setXPathExpression(ms_XPathGetName, ms_NamespaceContext);

        logger.debug("Perform Sanity Check");

        int cpt = 0;
        for (File file : oListFiles)
        {
            if (xpathExtractor.evaluateAsString(file) == null)
            {
                throw new Exception(
                        "The file "
                                + file.getAbsolutePath()
                                + " doesn't seem to be a transformed file. Please check.");
            }

            cpt++;

            if (cpt >= aNbOfFilesToCheck)
                return;
        }
    }

    /**
     * Iterates through the list of files, get name with XPath and rename it as
     * of <code><fileIdenfitifier></code> element.
     * 
     * @throws Exception
     */
    public void processFiles() throws Exception
    {
        String mdName = null;
        File newFile = null;

        logger.info("Renaming {} files.", this.oListFiles.length);

        for (File file : this.oListFiles)
        {
            mdName = this.extractNameFromXML(file);

            // System.out.println("Extracted name: " + fileName);

            if (mdName != null)
            {
                newFile = new File(this.strInputDirPath + File.separator + "Z_"
                        + mdName + "_C_EUMS" + ".xml");
                // System.out.println("new File: " + newFile.toString());

                FileUtils.moveFile(file, newFile);
            }
        }
    }

    /**
     * Get the metadata name without the wmo URI stuff
     * 
     * @param aFile
     * @return the string
     * @throws Exception
     */
    public String extractNameFromXML(File aFile) throws Exception
    {

        // preconditions
        if (aFile == null)
            throw new Exception("Error invalid File");

        XPathExtractor xpathExtractor = new XPathExtractor();

        xpathExtractor.setXPathExpression(ms_XPathGetName, ms_NamespaceContext);

        String result = xpathExtractor.evaluateAsString(aFile);

        if (result == null)
            throw new Exception(
                    "cannot extract the metadata fileIdentifier from "
                            + aFile.getAbsolutePath()
                            + " with the following XPath expression ["
                            + ms_XPathGetName + "] ");

        String[] strs = result.split("::");

        // should have 2 elements in the list otherwise error;

        if (strs.length != 2)
        {
            throw new Exception(
                    "Error. Cannot properly extract the EUMETSAT identifier from "
                            + result);
        }

        return strs[1];
    }

    /**
     * Return the EOPortalFileIdentifier from a WMOFilename
     * 
     * @param aWMOFilename
     * @return
     * @throws Exception
     */
    public static String extractFileIdentifierFromWMOFilename(
            String aWMOFilename) throws Exception
    {
        String[] dummys = aWMOFilename.split("_C_EUMS");
        if (dummys.length != 2)
        {
            throw new Exception(
                    "Error cannot extract the EOPortal fileIdentifier from "
                            + aWMOFilename
                            + ". aWMOFilename.split(\"C_EUMS\") = "
                            + ((dummys.length != 0) ? dummys : "null"));
        }

        return dummys[0].substring(2);
    }

    /**
     * Get the Metadata Date
     * 
     * @param aFile
     * @return
     * @throws Exception
     */
    public Date extractDate(File aFile) throws Exception
    {
        // preconditions
        if (aFile == null)
            throw new Exception("Invalid File");

        XPathExtractor xpathExtractor = new XPathExtractor();

        xpathExtractor.setXPathExpression(ms_XPathGetDate, ms_NamespaceContext);

        String result = xpathExtractor.evaluateAsString(aFile);

        if (result == null)
            throw new Exception("cannot extract the metadata dateStamp from "
                    + aFile.getAbsolutePath()
                    + " with the following XPath expression ["
                    + ms_XPathGetDate + "] ");

        return DateUtil.createDate(result, DateUtil.ms_MDDATEFORMAT);
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
