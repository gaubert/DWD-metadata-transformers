//------------------------------------------------------------------------------

//Copyright (C) 2010 by EUMETSAT 
//------------------------------------------------------------------------------
//
//Project:            DCPC role - Metadata exchange
//Component:          org.eumetsat.dcpc.md.export
//Module:             ApplyXslt.java
//
/** @author           Martinez
 * 
 */
//Creation Date:     10 May 2010
//
//------------------------------------------------------------------------------
//Version:            1
//------------------------------------------------------------------------------
//
/** @version
 *   1  Martinez	10/05/2010  Initial version.
 */

package org.eumetsat.dcpc.md.export;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import org.eumetsat.dcpc.commons.XmlPrettyPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//-----------------------------------------------
//Class:            ApplyXslt
//-----------------------------------------------
/**
 * Apply the XSLT stylesheet given as command line argument to all the XML files
 * stored at the input directory given as command line argument.
 */
public class XsltProcessor
{
    protected final static Logger logger          = LoggerFactory
                                                          .getLogger(XsltProcessor.class);

    // List of XML metadata files within the input directory
    private File[]                oListFiles      = null;

    // String path of the inputDirectory
    private String                strInputDirPath = null;

    // Saxon Processor
    private Processor             oSaxonProcessor = null;

    // The XSLT File
    private File                  m_XsltFile      = null;

    // The directory where results will be generated
    private File                  m_OutputDir     = null;

    // The prefix that will be applied to the transformed filename (default: no
    // prefix)
    private String                m_Prefix        = null;

    /**
     * Create a new object of <code>ApplyXslt</code> class, and extract the list
     * of XML files at the input directory
     * 
     * @param a_XsltFile
     *            The XSLT file used for the transformation
     * @param a_OutputDir
     *            The output directory where the results files are written
     */
    public XsltProcessor(File a_XsltFile, File a_OutputDir) throws Exception
    {
        this(a_XsltFile, a_OutputDir, "");
    }

    /**
     * Create a new object of <code>ApplyXslt</code> class, and extract the list
     * of XML files at the input directory
     * 
     * @param a_XsltFile
     *            The XSLT file used for the transformation
     * @param a_OutputDir
     *            The output directory where the results files are written
     * @param a_OutputFilePrefix
     *            Prefix that will be applied to all output files
     */
    public XsltProcessor(File a_XsltFile, File a_OutputDir,
            String a_OutputFilePrefix) throws Exception
    {
        this.m_Prefix = a_OutputFilePrefix;

        this.m_XsltFile = a_XsltFile;

        if (!this.m_XsltFile.exists())
        {
            throw new Exception("Error: " + this.m_XsltFile.getPath()
                    + " XSLT file doesn't exist");
        }

        this.m_OutputDir = a_OutputDir;

        // Create if it doesn't exist
        if (!this.m_OutputDir.exists())
        {
            if (!this.m_OutputDir.mkdir())
            {
                throw new Exception("Error: Cannot create " + this.m_OutputDir);
            }
        }
    }

    /**
     * Iterates through the list of files, transforms them with the given XSLT
     * 1.0 and creates the output dir, where the results of the transformation
     * are stored.
     * 
     * @param aDirToProcess
     *            the directory to process
     * @param aPrettyPrint
     *            prettyPrint the XML
     * @throws Exception
     */
    public void processFile(File aFile2Process, boolean aPrettyPrint)
            throws Exception
    {
        // Create if it doesn't exist
        if (!aFile2Process.exists())
        {
            throw new Exception("Error: " + aFile2Process + " doesn't exist");
        }

        logger.debug("Transform files using XSLT 1.0.");

        Source xsltSource = new StreamSource(this.m_XsltFile);

        // create an instance of TransformerFactory
        TransformerFactory transFact = TransformerFactory.newInstance();
        Transformer trans;

        try
        {
            trans = transFact.newTransformer(xsltSource);

            if (aPrettyPrint)
            {
                transformAndPrettyPrintFile(aFile2Process, trans,
                        this.m_OutputDir);
            }
            else
            {
                transformFile(aFile2Process, trans, this.m_OutputDir);
            }

        }
        catch (TransformerConfigurationException e)
        {
            System.out.println("Error, see exception");
            e.printStackTrace();
        }
        catch (TransformerException e)
        {
            e.printStackTrace();
        }
        finally
        {
            System.out.println();
        }
    }

    /**
     * Iterates through the list of files, transforms them with the given XSLT
     * 1.0 and creates the output dir, where the results of the transformation
     * are stored.
     * 
     * @param aDirToProcess
     *            the directory to process
     * @param aPrettyPrint
     *            prettyPrint the XML
     * @throws Exception
     */
    public void processFiles(File aDirToProcess, boolean aPrettyPrint)
            throws Exception
    {
        // Create if it doesn't exist
        if (!aDirToProcess.exists())
        {
            throw new Exception("Error: " + aDirToProcess + " doesn't exist");
        }

        File[] files2Process = aDirToProcess.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name)
            {
                return name.endsWith(".xml");
            }
        });

        logger.debug("Found {} XML files to process.", files2Process.length);

        logger.debug("Transform files using XSLT 1.0.");

        Source xsltSource = new StreamSource(this.m_XsltFile);

        // create an instance of TransformerFactory
        TransformerFactory transFact = TransformerFactory.newInstance();
        Transformer trans;

        try
        {
            trans = transFact.newTransformer(xsltSource);
            int cpt = 0;
            for (File file : files2Process)
            {
                if (aPrettyPrint)
                {
                    transformAndPrettyPrintFile(file, trans, this.m_OutputDir);
                }
                else
                {
                    transformFile(file, trans, this.m_OutputDir);
                }
                cpt++;
                System.out.print(".");
                if ((cpt % 80) == 0)
                {
                    System.out.println();
                }
            }
        }
        catch (TransformerConfigurationException e)
        {
            System.out.println("Error, see exception");
            e.printStackTrace();
        }
        catch (TransformerException e)
        {
            e.printStackTrace();
        }
        finally
        {
            System.out.println();
        }
    }

    /**
     * Iterates through the list of files, transforms them with the given XSLT
     * 2.0 and creates the output dir, where the results of the transformation
     * are stored.
     * 
     * @param xsltFile
     *            XSLT 2.0 used for the transformation
     */
    @SuppressWarnings("unused")
    private void processFilesXSLT2(File xsltFile)
    {
        File outputDir = new File(this.strInputDirPath + File.separator
                + "output");
        if (!outputDir.mkdir())
        {
            System.out.println("Output dir could not be created.");
        }
        else
        {
            System.out.println("Transform files using XSLT 2.0.");
            Source xsltSource = new StreamSource(xsltFile);
            // creates a Saxon Processor of the Home edition
            oSaxonProcessor = new Processor(false);
            // creates an XSLT compiler
            XsltCompiler saxonCompiler = oSaxonProcessor.newXsltCompiler();
            // compile the xslt 2.0, load it and use the transformer
            XsltExecutable saxonExecutable;
            try
            {
                saxonExecutable = saxonCompiler.compile(xsltSource);
                XsltTransformer saxonTransformer = saxonExecutable.load();
                for (int i = 0; i < this.oListFiles.length; i++)
                {
                    transformFileXSLT2(this.oListFiles[i], saxonTransformer,
                            outputDir);
                }
            }
            catch (SaxonApiException e)
            {
                System.out.println("Error, see exception");
                e.printStackTrace();
            }
        }
    }

    /**
     * Takes the input file, transforms it applying the XSLT 1.0 and stores the
     * result in the output dir
     * 
     * @param inputFile
     *            input file
     * @param xsltTransformer
     *            the transformer object that performs the transformation
     * @param outputDir
     *            directory for the transformed XML files
     * @throws SaxonApiException
     *             when there is an exception
     */
    private void transformFile(File inputFile, Transformer xsltTransformer,
            File outputDir) throws TransformerException
    {
        Source xmlSource = new StreamSource(inputFile);
        String outputFileName = outputDir.getPath() + File.separator
                + this.m_Prefix + inputFile.getName();

        Result result = new StreamResult(new File(outputFileName));

        try
        {
            xsltTransformer.transform(xmlSource, result);
        }
        catch (TransformerException e)
        {
            System.out.println("Failed transforming " + inputFile.getName());
            throw e;
        }

    }

    // ByteArrayOutputStream stream = new ByteArrayOutputStream();
    // stream.toString("UTF-8");

    private void transformAndPrettyPrintFile(File inputFile,
            Transformer xsltTransformer, File outputDir) throws Exception
    {
        Source xmlSource = new StreamSource(inputFile);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String outputFileName = outputDir.getPath() + File.separator
                + this.m_Prefix + inputFile.getName();

        Result result = new StreamResult(out);

        try
        {
            xsltTransformer.transform(xmlSource, result);

            FileOutputStream oFile = new FileOutputStream(outputFileName);

            XmlPrettyPrinter.prettyPrint(new ByteArrayInputStream(out
                    .toByteArray()), oFile);

            oFile.close();
        }
        catch (Exception e)
        {
            logger.error("Failed transforming " + inputFile.getName());
            throw e;
        }
        finally
        {
            out.close();
        }

    }

    /**
     * Takes the input file, transforms it applying the XSLT 2.0 and stores the
     * result in the output dir
     * 
     * @param inputFile
     *            input file
     * @param xsltTransformer
     *            the transformer object that performs the transformation
     * @param outputDir
     *            directory for the transformed XML files
     * @throws SaxonApiException
     *             when there is an exception
     */
    private void transformFileXSLT2(File inputFile,
            XsltTransformer saxonTransformer, File outputDir)
            throws SaxonApiException
    {
        XdmNode xmlSource = oSaxonProcessor.newDocumentBuilder().build(
                new StreamSource(inputFile));
        String outputFileName = outputDir.getPath() + File.separator
                + inputFile.getName();

        Serializer outResult = new Serializer();
        outResult.setOutputProperty(Serializer.Property.METHOD, "xml");
        outResult.setOutputProperty(Serializer.Property.INDENT, "no");
        outResult.setOutputFile(new File(outputFileName));

        saxonTransformer.setInitialContextNode(xmlSource);
        saxonTransformer.setDestination(outResult);
        saxonTransformer.transform();
    }

    /**
     * Iterates through the XML metadata files within the given input directory
     * and applies the given XSLT to them. Use: <code>@{@class}</code> <input dir> <XSLT file>
     * 
     * @param args
     *            - 2 arguments are expected, the path to the input directory
     *            and the path to the XSLT file
     */
    public static void main(String[] args)
    {
        if (args == null || args.length != 2 || (args[0].equalsIgnoreCase(""))
                && (args[1].equalsIgnoreCase("")))
        {
            System.out.println("Usage: " + XsltProcessor.class.getName()
                    + " <input dir> <XSLT file>");
            System.exit(1);
        }

        /*
         * try { File inputDir = new File(args[0]); if (!inputDir.isDirectory())
         * { System.out
         * .println("The argument specified is not a valid input directory: " +
         * args[0]); } else { File xsltFile = new File(args[1]); if
         * (!xsltFile.exists()) { System.out
         * .println("The argument specified is not a valid file: " + args[1]); }
         * else { ApplyXslt applyXSLT = new ApplyXslt(inputDir);
         * applyXSLT.processFiles(xsltFile); System.out.println();
         * System.out.println("Process finished."); } }
         * 
         * } catch (Exception e) { System.out
         * .println("An error occurred while processing the files of directory: "
         * + args[0]); e.printStackTrace(); System.exit(1); }
         */
    }
}
