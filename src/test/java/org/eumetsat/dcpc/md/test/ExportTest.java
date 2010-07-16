package org.eumetsat.dcpc.md.test;
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
import org.eumetsat.dcpc.md.export.XsltProcessor;
import org.eumetsat.dcpc.md.export.MetadataExporter;
import org.eumetsat.dcpc.md.export.Release;
import org.eumetsat.dcpc.md.export.ReleaseDatabase;
import org.eumetsat.dcpc.md.export.MetadataFileRenamer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExportTest extends TestCase
{
    public final static String TEST_DIR = "H:/Dev/ecli-workspace/DWD-metadata-transformers/src/test/resources";
    public final static Logger logger = LoggerFactory.getLogger(ExportTest.class);
       
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
    
    public void testScenario1()
    {
        String releaseDBPath      = "H:/ReleasesDB";
        String workingDir         = "H:/WorkingDir";
        String xsltFile           = "H:/Dev/ecli-workspace/DWD-metadata-transformers/ext/xslt/eum2isoapFull_v3.xsl";
        String R1 = TEST_DIR + File.separatorChar + "scenario-1" + File.separatorChar + "R1";
        String R2 = TEST_DIR + File.separatorChar + "scenario-1" + File.separatorChar + "R2";
        String R3 = TEST_DIR + File.separatorChar + "scenario-1" + File.separatorChar + "R3";
        String R4 = TEST_DIR + File.separatorChar + "scenario-1" + File.separatorChar + "R4";
        String R5 = TEST_DIR + File.separatorChar + "scenario-1" + File.separatorChar + "R5";
        String R6 = TEST_DIR + File.separatorChar + "scenario-1" + File.separatorChar + "R6";
        
        try
        {         
            MetadataExporter exporter = new MetadataExporter(xsltFile, releaseDBPath, workingDir);
            
            ReleaseDatabase db = exporter.getReleaseDatabase();
            
            // clean DB at the beginning of the scenario
            //db.eraseReleaseDatabase();
            
            System.out.println("********** Create Export from R1: (add 10 files) **********");
            
            exporter.createExport(R1);  
            
            // Check that this is correct
            Release latestRelease = db.getLatestRelease();
            
            // We should have 10 files
            assertEquals("Should have 10 files in Delta. Check the ReleaseDB content that is in " + releaseDBPath, 10, latestRelease.getDeltaXmlFilenames().size());
            
            System.out.println("********** Create Export from R2: (delete 5 files) **********");
            
            // delete 5 files
            exporter.createExport(R2);  
            
            // Check that this is correct
            latestRelease = db.getLatestRelease();
            
            // check that 5 files have been deleted
            assertEquals("Should have been deleting 5 files. Check the ReleaseDB content that is in " + releaseDBPath, 5, latestRelease.getDeltaDeletedFilenames().size());
            
            System.out.println("********** Create Export from R3: (modify 1 file) **********");
            
            // modify a file
            exporter.createExport(R3);  
            
            // Check that this is correct
            latestRelease = db.getLatestRelease();
            
            // check that 1 files have been deleted
            assertEquals("Should have 1 file in the Delta. Check the ReleaseDB content that is in " + releaseDBPath, 1, latestRelease.getDeltaXmlFilenames().size());
            
            assertEquals("Bad name. Check the ReleaseDB content that is in " + releaseDBPath, "EO_EUM_DAT_MULT_MAPSSI.xml", (String) latestRelease.getDeltaXmlFilenames().get(0));
            
            System.out.println("********** Create Export from R4: (add 5 files, modify 1) **********");
            
            // put back 5 original files. Result is 5 new files and a modified one : 6 files in results and EO_EUM_DAT_MULT_MAPSSI.xml is modified again
            exporter.createExport(R4);  
            
            // Check that this is correct
            latestRelease = db.getLatestRelease();
            
            // check that 6 new files have been added
            assertEquals("Should have 6 files in the Delta. Check the ReleaseDB content that is in " + releaseDBPath, 6, latestRelease.getDeltaXmlFilenames().size());
            
            // check that "EO_EUM_DAT_MULT_MAPSSI.xml" is in the list of modified files
            assertTrue("Delta should contain EO_EUM_DAT_MULT_MAPSSI.xml as it has been modified" , latestRelease.getDeltaXmlFilenames().contains("EO_EUM_DAT_MULT_MAPSSI.xml"));
            
            System.out.println("********** Create Export from R5: (no changes) **********");
            
            // put back 5 original files. Result is 5 new files and a modified one : 6 files in results and EO_EUM_DAT_MULT_MAPSSI.xml is modified again
            exporter.createExport(R5);  
            
            // Check that this is correct
            Release previous  = latestRelease;
            Release unchanged = db.getLatestRelease();
            
            // check that 6 new files have been added
            assertEquals("Should have 0 files in the Delta. Check the ReleaseDB content that is in " + releaseDBPath, unchanged, previous);
            
            System.out.println("********** Create Export from R6: (delete 10 files to empty DB) **********");
            
            //delete 10 files
            exporter.createExport(R6);  
            
            // Check that this is correct
            latestRelease = db.getLatestRelease();
            
            // check that 10 files have been deleted
            assertEquals("Should have been deleting 10 files. Check the ReleaseDB content that is in " + releaseDBPath, 10, latestRelease.getDeltaDeletedFilenames().size());
            
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void testLargeScaleScenario()
    {
        String releaseDBPath      = "H:/ReleasesDB";
        String workingDir         = "H:/WorkingDir";
        String xsltFile           = "H:/Dev/ecli-workspace/DWD-metadata-transformers/ext/xslt/eum2isoapFull_v3.xsl";
        String source             = "H:/Dev/ecli-workspace/DWD-metadata-transformers/ext/metadata/eo-portal-metadata";
        String empty              = TEST_DIR + File.separatorChar + "scenario-2" + File.separatorChar + "empty";
        
        
        try
        {         
            MetadataExporter exporter = new MetadataExporter(xsltFile, releaseDBPath, workingDir);
            
            ReleaseDatabase db = exporter.getReleaseDatabase();
            
            // clean DB at the beginning of the scenario
            System.gc();
            System.out.println("********** ERASE ****************");
            db.eraseReleaseDatabase();
            System.gc();
            
            System.out.println("********** Create Export from eo portal source (359 files) **********");
            
            exporter.createExport(source);  
            
            // Check that this is correct
            Release latestRelease = db.getLatestRelease();
            
            // We should have 10 files
            assertEquals("Should have 359 files in Delta. Check the ReleaseDB content that is in " + releaseDBPath, 359, latestRelease.getDeltaXmlFilenames().size());
            
            System.out.println("********** Create Second Export (empty database) **********");
            
            exporter.createExport(empty);  
            
            // Check that this is correct
            latestRelease = db.getLatestRelease();
            
            // We should have 359 files deleted
            assertEquals("Should have 359 files in Delta. Check the ReleaseDB content that is in " + releaseDBPath, 359, latestRelease.getDeltaDeletedFilenames().size());
            
        }  
        catch (Exception e)
        {
            e.printStackTrace();
            fail("See Exception Stack Trace");
        }
    } 
}
