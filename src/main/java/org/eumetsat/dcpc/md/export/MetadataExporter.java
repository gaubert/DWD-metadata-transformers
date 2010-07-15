package org.eumetsat.dcpc.md.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eumetsat.dcpc.commons.Checksummer;
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
        File tempDir        = FileSystem.createTempDirectory("temp-", this.m_WorkingDir);
        boolean prettyPrint = true;
        
        try
        {
            File xmlDir = new File(tempDir.getAbsolutePath() + File.separator + Release.XML_FILES);
            File MD5Dir = new File(tempDir.getAbsolutePath() + File.separator + Release.MD5_FILES);
            
            FileSystem.createDirs(xmlDir);
            FileSystem.createDirs(MD5Dir);
            
            // do the transformations
            XsltProcessor xsltTransformer = new XsltProcessor(this.m_XsltFilePath, xmlDir);
            
            // do xslt transformation
            xsltTransformer.processFiles(new File(aMetadataSourcePath), prettyPrint);
            
            // rename files
            MetadataFileRenamer rn = new MetadataFileRenamer(xmlDir);
            
            rn.processFiles();
            
            System.out.println("Calculate MD5s");
            
            // calculate MD5s
            calculateMD5s(xmlDir, MD5Dir);
            
            // calculate Delta from previous Release
            calculateDelta(xmlDir, MD5Dir);
            
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
    
    /**
     * Generate the MD5 values for each of the files
     * @param aInputDir
     * @param aOutputDir
     * @throws Exception
     */
    public void calculateMD5s(File aInputDir, File aOutputDir) throws Exception
    {
        // calculate MD5
        File[] files2Process = aInputDir.listFiles(new FilenameFilter() {
             public boolean accept(File dir, String name)
             {
                 return name.endsWith(".xml");
             }
         });
        
        String outputFilename;
        for (File file : files2Process)
        {
           
           outputFilename = aOutputDir + File.separator + FilenameUtils.getBaseName(file.getName()) + ".md5";
           
           Checksummer.doMD5Checksum(file, new File(outputFilename));
        }
    }
    
    
    public void calculateDelta(File aTempXmlDir, File aTempMD5Dir) throws Exception
    {
        // create a temp release
        File releaseTempDir   = FileSystem.createTempDirectory("release-", this.m_WorkingDir);
        
        Release tempRelease   = Release.createRelease(releaseTempDir);
        
        
        Release latestRelease = m_ReleaseDB.getLatestRelease();
        
        // No previous Release so all origin XML files go into Delta Result and
        // there are no deleted files
        if (latestRelease == null)
        {
            // Copy XML and MD5 in SRC
            tempRelease.addInSrcMD5s(aTempMD5Dir);
            tempRelease.addInSrcXmls(aTempXmlDir);
            
            // Copy XML into Delta/Result
            tempRelease.addInDeltaResult(aTempXmlDir);
        }
        else
        {
            // get list of previous MD5s and the list of current MD5s
            HashMap<String,String> prev = latestRelease.getDeltaMD5s();
            HashMap<String,String> curr = tempRelease.getDeltaMD5s();
            
            Set<String> currSet = new HashSet<String>();
            Set<String> deletedSet = new HashSet<String>();
            Set<String> newSet  = new HashSet<String>();
            
            /* calculate the delta
             * algorithm: 
             * for elemOld in old:
             *    if elemOld not in new:
             *       add in deletedSet
             *    if elemOld in new and elemOld.md5 != elemNew.md5:
             *       add in newSet
             *    
             *    remove elemOld from currSet
             *    
             * All elem left in currSet are the new ones and must be added in newSet
             *    newSet.add(currSet);
             */
              
             // copy curr keys in currSet
             currSet.addAll(curr.keySet());
             
            for (String key : prev.keySet())
            {
               if (!curr.containsKey(key))
               {
                  deletedSet.add(key);   
               }
               else if (!prev.get(key).equals(curr.get(key))) 
               {
                  // md5 different so the file has been updated
                  
                  // add in newSet and remove from currSet
                  newSet.add(key);
                  currSet.remove(key);
               }
            }
            
            // elems left in currSet are new and need to be added to newSet
            newSet.addAll(currSet);
            
            // copy newSet in Delta/Files
          
            for (String name : newSet)
            {
                tempRelease.addFileInDeltaResult(new File(aTempXmlDir + File.pathSeparator + name + ".xml"));
            }
            
            // create delete file
                   
            
        }
    }
}
