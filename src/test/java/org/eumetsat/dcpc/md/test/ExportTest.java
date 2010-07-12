package org.eumetsat.dcpc.md.test;

import java.io.File;

import org.eumetsat.dcpc.md.export.ApplyXslt;

import junit.framework.TestCase;

public class ExportTest extends TestCase
{
    public final static String TEST_DIR = "H:/Dev/ecli-workspace/DWD-metadata-transformers/src/test/resources"; ;

    public void testSimple()
    {
        System.out.println("Hello world");
    }

    public void testUniqueFile()
    {
        String dirPath = TEST_DIR + File.separatorChar + "uniqueXML";
        String filePath = "H:/Dev/ecli-workspace/DWD-metadata-transformers/ext/xslt/eum2isoapFull_v3.xsl";

        File dir = new File(dirPath);
        File xsltFile = new File(filePath);
        try
        {
            if (!dir.exists())
            {

                throw new Exception(String.format(
                        "Directory %s doesn't exist%n", dirPath));

            }
            else if (!dir.isDirectory())
            {
                throw new Exception(String.format(
                        "%s should be a directory %n", dirPath));
            }

            if (!xsltFile.exists())
            {
                throw new Exception(String.format(
                        "The file %s doesn't exist%n", xsltFile));
            }

            ApplyXslt transformer = new ApplyXslt(xsltFile, new File("H:/Dev/ecli-workspace/DWD-metadata-transformers/src/test/resources/uniqueXML"),"transformed_");
            transformer.processFiles(dir);
            System.out.println();
            System.out.println("Process finished.");

        }
        catch (Exception e)
        {
           e.printStackTrace();
           fail("Received Exception " + e);
        }

    }
}
