package org.eumetsat.dcpc.md.test;

import java.awt.print.Printable;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.eumetsat.dcpc.commons.Checksummer;
import org.eumetsat.dcpc.commons.DateFormatter;
import org.eumetsat.dcpc.commons.XmlPrettyPrinter;
import org.eumetsat.dcpc.md.export.ApplyXslt;
import org.eumetsat.dcpc.md.export.MetadataExporter;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

public class ExportTest extends TestCase
{
    public final static String TEST_DIR = "H:/Dev/ecli-workspace/DWD-metadata-transformers/src/test/resources";
    
    public void ztestUniqueFile()
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
           fail("See Exception Stack Trace");
        }
    }
    
    /* working pretty print test */
    public void ztestPrettyPrintFile()
    {
        String dirPath          = TEST_DIR + File.separatorChar + "uniqueXML";
        String filePath         = dirPath  + File.separatorChar + "10.xml";
        String expectedFilePath = TEST_DIR + File.separatorChar + "testPrettyPrint" + File.separatorChar + "expectedPrettyPrintXML.xml";
        
        try
        {
            // pretty print
            String result = XmlPrettyPrinter.prettyPrintAsString(filePath);
            
            // get the expected result
            byte[] buffer = new byte[(int) new File(expectedFilePath).length()];
            BufferedInputStream f = new BufferedInputStream(new FileInputStream(expectedFilePath));
            f.read(buffer);
            
            String expectedXML = new String(buffer,"UTF-8");
            
            //assertEquals(expectedXML, result);
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail("See Exception Stack Trace");
        }
    }
    
    /**
     * test MD5 generation
     */
    public void ztestMD5()
    {
        String toHash    = "This is the string to hash";
        String expectMD5 = "599932288f3e3a4c377cdd6b3cb68ea0";
        
        ByteArrayInputStream BIn;
        try
        {
            BIn = new ByteArrayInputStream(toHash.getBytes("UTF-8"));
            
            String checksum = Checksummer.doMD5Checksum(BIn);
            
            assertEquals("the 2 checksum are not the same", expectMD5, checksum);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail("See Exception Stack Trace");
        }
        
    }
    
    public void ztestDateFormatting()
    {
        Date date = new Date();
        System.out.println(DateFormatter.dateToString(date));
    }
    
    public void testMetadataExporter()
    {
        String releaseDBPath = "H:/ReleasesDB";
        try
        {
            MetadataExporter exporter = new MetadataExporter(releaseDBPath);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
