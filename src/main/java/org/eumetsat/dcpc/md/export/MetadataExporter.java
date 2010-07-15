package org.eumetsat.dcpc.md.export;

import java.io.File;
import java.io.FileNotFoundException;

import org.eumetsat.dcpc.commons.FileSystem;


/**
 * Metadata Exporter
 * @author guillaume.aubert@eumetsat.int
 *
 */
public class MetadataExporter
{
    protected ReleaseDatabase m_ReleaseDB;
    protected File m_WorkingDir;
    protected File m_XsltFilePath;
    
    protected static final boolean TEMP_DIR_DELETION_ON = false;
    
    /**
     * Constructor
     * @param aReleaseDBRootDirPath The Root Path for the ReleaseDB
     * @throws Exception
     */
    public MetadataExporter(String aXsltFilePath, String aReleaseDBRootDirPath, String aWorkingDirPath) throws Exception
    {
       
       m_XsltFilePath = new File(aXsltFilePath);
       
       if (!m_XsltFilePath.exists())
           throw new FileNotFoundException("Xslt File " + aXsltFilePath + " doesn't exist");
        
       m_ReleaseDB = new ReleaseDatabase(aReleaseDBRootDirPath);
       
       FileSystem.createDirs(aWorkingDirPath);
       
       m_WorkingDir = new File(aWorkingDirPath);

    }
    
    /**
     * 
     * @param aMetadataSourcePath
     * @throws Exception 
     */
    public void createExport(String aMetadataSourcePath) throws Exception
    {
        File tempDir        = FileSystem.createTempDirectory(m_WorkingDir);
        boolean prettyPrint = true;
        
        try
        {
            // do the transformations
            ApplyXslt xsltTransformer = new ApplyXslt(m_XsltFilePath, tempDir);
            
            xsltTransformer.processFiles(new File(aMetadataSourcePath), prettyPrint);
            
            // rename files
            
            // calculate Delta from previous Release
            
            // move in ReleaseDB
            
            // expose Delta
        } 
        finally
        {
            // delete the temporary directory in any case 
            if (TEMP_DIR_DELETION_ON)
                FileSystem.deleteDirs(tempDir);
        }
    }
}
