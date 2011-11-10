package org.eumetsat.eoportal.dcpc.md.test;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eumetsat.eoportal.dcpc.commons.FileSystem;
import org.eumetsat.eoportal.dcpc.md.export.CMDRunner;
import org.eumetsat.eoportal.dcpc.md.export.MetadataExporter;
import org.eumetsat.eoportal.dcpc.md.export.Release;
import org.eumetsat.eoportal.dcpc.md.export.ReleaseDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadataExporterValidationSuite extends TestCase
{
    
    public final static Logger logger = LoggerFactory.getLogger(MetadataExporterValidationSuite.class);
    public static String PROJ_DIR = null;        
    static
    {
        PROJ_DIR = System.getProperty("project.dir",".");
        System.out.println("Project dir = " + new File(PROJ_DIR).getAbsolutePath());
    }
    
    public final static String TEST_DIR = PROJ_DIR + "/src/test/resources";
    
    /**
     * Test all different cases of updates and deletion
     */
    public void testScenario1()
    {
        
        
        String releaseDBPath      = "/tmp/ReleasesDB";
        String workingDir         = "/tmp/WorkingDir";
        String outputDir          = "/tmp/OutputDir";
        String xsltFile           = PROJ_DIR + "/etc/xslt/eum2iso_v4.1.xsl";
        String R1 = TEST_DIR + File.separatorChar + "scenario-1" + File.separatorChar + "R1";
        String R2 = TEST_DIR + File.separatorChar + "scenario-1" + File.separatorChar + "R2";
        String R3 = TEST_DIR + File.separatorChar + "scenario-1" + File.separatorChar + "R3";
        String R4 = TEST_DIR + File.separatorChar + "scenario-1" + File.separatorChar + "R4";
        String R5 = TEST_DIR + File.separatorChar + "scenario-1" + File.separatorChar + "R5";
        String R6 = TEST_DIR + File.separatorChar + "scenario-1" + File.separatorChar + "R6";
        String R7 = TEST_DIR + File.separatorChar + "scenario-1" + File.separatorChar + "R7";
        
        System.out.println("*****************************************");
        System.out.println("********** VALIDATION SCENARIO **********");
        System.out.println("*****************************************");
        
        try
        {     
            FileSystem.createDirs(releaseDBPath);
            FileSystem.createDirs(workingDir);
            FileSystem.createDirs(outputDir);
            
            // create R7 if it isn't already there as ant will not copy and empty dir
            FileSystem.createDirs(R7);
            
            MetadataExporter exporter = new MetadataExporter(releaseDBPath, workingDir);
            
            exporter.setXsltFile(xsltFile);
            
            ReleaseDatabase db = exporter.getReleaseDatabase();
            
            // clean DB at the beginning of the scenario
            db.eraseReleaseDatabase();
            
            System.out.println("********** Create Export from R1: (add 10 files) **********");
            
            exporter.createExport(R1, outputDir, false);  
            
            // Check that this is correct
            Release latestRelease = db.getLatestRelease();
            
            // We should have 10 files
            assertEquals("Should have 10 files in Delta. Check the ReleaseDB content that is in " + releaseDBPath, 10, latestRelease.getDeltaXmlFilenames().size());
            
            System.out.println("********** Create Export from R2: (delete 5 files) **********");
            
            // delete 5 files
            exporter.createExport(R2, true);  
            
            // Check that this is correct
            latestRelease = db.getLatestRelease();
            
            // check that 5 files have been deleted
            assertEquals("Should have been deleting 5 files. Check the ReleaseDB content that is in " + releaseDBPath, 5, latestRelease.getDeltaDeletedFilenames().size());
            
            System.out.println("********** Create Export from R3: (modify 1 file) **********");
            
            // modify a file
            exporter.createExport(R3, true);  
            
            // Check that this is correct
            latestRelease = db.getLatestRelease();
            
            // check that 1 files have been deleted
            assertEquals("Should have 1 file in the Delta. Check the ReleaseDB content that is in " + releaseDBPath, 1, latestRelease.getDeltaXmlFilenames().size());
            
            assertTrue("Bad name (expect something like Z_EO_EUM_DAT_MULT_MAPSSI_C_EUMS_CurrentDate.xml). Check the ReleaseDB content that is in " + releaseDBPath, ((String) latestRelease.getDeltaXmlFilenames().get(0)).startsWith("Z_EO_EUM_DAT_MULT_MAPSSI_C_EUMS_"));
            
            System.out.println("********** Create Export from R4: (add 5 files, modify 1) **********");
            
            // put back 5 original files. Result is 5 new files and a modified one : 6 files in results and EO_EUM_DAT_MULT_MAPSSI.xml is modified again
            exporter.createExport(R4, true);  
            
            // Check that this is correct
            latestRelease = db.getLatestRelease();
            
            // check that 6 new files have been added
            assertEquals("Should have 6 files in the Delta. Check the ReleaseDB content that is in " + releaseDBPath, 6, latestRelease.getDeltaXmlFilenames().size());
            
            //check that Z_EO_EUM_DAT_GOES_SAE_C_EUMS is amongst the metadata elements
            
            boolean ok = false;
            for (String fnames : latestRelease.getDeltaXmlFilenames())
            {
                if (fnames.startsWith("Z_EO_EUM_DAT_GOES_SAE_C_EUMS_"))
                {
                    ok = true;
                }
            }
            
            assertTrue("Bad name (expect something like Z_EO_EUM_DAT_GOES_SAE_C_EUMS_CurrentDate.xml). Check the ReleaseDB content that is in " + releaseDBPath, ok);
            
            // nothing in deleted just modifications
            assertEquals("Should have nothing in delete dir. Check the ReleaseDB content that is in " + releaseDBPath, 0, latestRelease.getDeltaDeletedFilenames().size());
            
            System.out.println("********** Create Export from R5: (back to previous version) **********");
            
            // put back 5 original files. Result is 5 new files and a modified one : 6 files in results and EO_EUM_DAT_MULT_MAPSSI.xml is modified again
            exporter.createExport(R5, true); 
            
            // Check that this is correct
            latestRelease = db.getLatestRelease();
            
            // check that 1 new file has been added
            assertEquals("Should have 1 files in the Delta. Check the ReleaseDB content that is in " + releaseDBPath, 1, latestRelease.getDeltaXmlFilenames().size());
            
            System.out.println("********** Create Export from R6: (no changes) **********");
            
            // put back 5 original files. Result is 5 new files and a modified one : 6 files in results and EO_EUM_DAT_MULT_MAPSSI.xml is modified again
            exporter.createExport(R6, true);  
            
            // Check that this is correct
            Release previous  = latestRelease;
            Release unchanged = db.getLatestRelease();
            
            // check that 6 new files have been added
            assertEquals("Should have 0 files in the Delta. Check the ReleaseDB content that is in " + releaseDBPath, unchanged, previous);
            
            System.out.println("********** Create Export from R7: (delete 10 files to empty DB) **********");
            
            //delete 10 files
            exporter.createExport(R7, true);  
            
            // Check that this is correct
            latestRelease = db.getLatestRelease();
            
            // check that 10 files have been deleted
            assertEquals("Should have been deleting 10 files. Check the ReleaseDB content that is in " + releaseDBPath, 10, latestRelease.getDeltaDeletedFilenames().size());
            
            //db.eraseReleaseDatabase();
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            FileSystem.deleteDirs(releaseDBPath);
            FileSystem.deleteDirs(workingDir);
            FileSystem.deleteDirs(outputDir);
        }
    }
    
    /**
     * Largescale scenario with download from the portal
     */
    public void testFullTestWithDownload()
    {
        String xsltFile           = PROJ_DIR + "/etc/xslt/eum2iso_v4.1.xsl";
        String [] args = new String[] { "-out", "/tmp/md-exporter-download-sc/out", "-rdb" , "/tmp/md-exporter-download-sc/RDB" , "-xslt", xsltFile};
        
        
        System.out.println("*****************************************");
        System.out.println("*********** DOWNLOAD SCENARIO ***********");
        System.out.println("*****************************************");
        
        FileSystem.deleteDirs("/tmp/md-exporter-download-sc/out");
        
        System.out.println("Please Clean the working Directory /tmp/md-exporter-download-sc");
        CMDRunner.main(args, false);  
        
    }
    
    /**
     * Failing sanity check
     */
    public void testScenario3()
    {
        String releaseDBPath      = "/tmp/ReleasesDB";
        String workingDir         = "/tmp/WorkingDir";
        String outputDir          = "/tmp/OutputDir";
        String R1 = TEST_DIR + File.separatorChar + "scenario-1" + File.separatorChar + "R1";
        
        System.out.println("*****************************************");
        System.out.println("********** SANITYCHECK SCENARIO **********");
        System.out.println("*****************************************");
        
        try
        {         
            FileSystem.createDirs(releaseDBPath);
            FileSystem.createDirs(workingDir);
            FileSystem.createDirs(outputDir);
            
            MetadataExporter exporter = new MetadataExporter(releaseDBPath, workingDir);
            
            exporter.getReleaseDatabase();
            
            exporter.createExport(R1, outputDir, false);  
            fail("The sanity check should fail");
        }
        catch(Exception e)
        {
            assertTrue("The sanity check should fail but another error occured.", e.getMessage().contains(" doesn't seem to be a transformed file. Please check"));
        }
        finally
        {
            // delete ReleaseDB
            FileSystem.deleteDirs(releaseDBPath);
            FileSystem.deleteDirs(workingDir);
            FileSystem.deleteDirs(outputDir);
        }
        
        
    }
    
    public void testLargeScaleScenario()
    {
        String releaseDBPath      = "/tmp/ReleasesDB";
        String workingDir         = "/tmp/WorkingDir";
        String xsltFile           = PROJ_DIR + "/etc/xslt/eum2iso_v4.1.xsl";
        String source             = PROJ_DIR + "/etc/metadata/eo-portal-metadata";
        String empty              = TEST_DIR + File.separatorChar + "scenario-2" + File.separatorChar + "empty";
        
        System.out.println("*****************************************");
        System.out.println("********** LARGESCALE SCENARIO **********");
        System.out.println("*****************************************");
        
        try
        {         
            FileSystem.createDirs(releaseDBPath);
            FileSystem.createDirs(workingDir);
            FileSystem.createDirs(empty);
            
            MetadataExporter exporter = new MetadataExporter(releaseDBPath, workingDir);
            
            exporter.setXsltFile(xsltFile);
            
            ReleaseDatabase db = exporter.getReleaseDatabase();
            
            // clean DB at the beginning of the scenario
            System.out.println("********** ERASE ****************");
            db.eraseReleaseDatabase();
            
            System.out.println("********** Create Export from eo portal source (> 350 files) **********");
            
            exporter.createExport(source, true);  
            
            // Check that this is correct
            Release latestRelease = db.getLatestRelease();
            
            // We should have 350 files
            assertTrue("Should have more than 350 files in Delta. Check the ReleaseDB content that is in " + releaseDBPath, latestRelease.getDeltaXmlFilenames().size() > 350);
            
            System.out.println("********** Create Second Export (empty database) **********");
            
            exporter.createExport(empty, true);  
            
            // Check that this is correct
            latestRelease = db.getLatestRelease();
            
            // We should have 359 files deleted
            assertTrue("Should have more than 350 files in Delta. Check the ReleaseDB content that is in " + releaseDBPath, latestRelease.getDeltaDeletedFilenames().size() > 350);
            
            db.eraseReleaseDatabase();
        }  
        catch (Exception e)
        {
            e.printStackTrace();
            fail("See Exception Stack Trace");
        }
        finally
        {
            // delete ReleaseDB
            FileSystem.deleteDirs(releaseDBPath);
            FileSystem.deleteDirs(workingDir);
        }
    }
    
    public static Test suite() 
    {
       return new TestSuite(MetadataExporterValidationSuite.class);
    }
          
    
    public static void main(String[] args) throws Exception 
    {
          junit.textui.TestRunner.run(suite());
    }
          
}
