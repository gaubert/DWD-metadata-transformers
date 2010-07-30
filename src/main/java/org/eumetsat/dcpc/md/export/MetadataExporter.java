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
import org.eumetsat.dcpc.commons.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Metadata Exporter
 * 
 * @author guillaume.aubert@eumetsat.int
 * 
 */
public class MetadataExporter
{
    protected ReleaseDatabase      m_ReleaseDB;
    protected File                 m_WorkingDir;
    protected File                 m_XsltFilePath;
    protected boolean              m_NoXslt = true; // default to true

    public final static Logger     logger               = LoggerFactory
                                                                .getLogger(MetadataExporter.class);

    protected static final boolean TEMP_DIR_DELETION_ON = true;

    /**
     * Constructor
     * 
     * @param aReleaseDBRootDirPath
     *            The Root Path for the ReleaseDB
     * @throws Exception
     */
    public MetadataExporter(String aReleaseDBRootDirPath,
            String aWorkingDirPath) throws Exception
    {
        m_ReleaseDB = new ReleaseDatabase(aReleaseDBRootDirPath);

        FileSystem.createDirs(aWorkingDirPath);

        m_WorkingDir = new File(aWorkingDirPath);

    }
    
    public void setXsltFile(String aXsltFilePath) throws FileNotFoundException
    {
        m_XsltFilePath = new File(aXsltFilePath);

        if (!m_XsltFilePath.exists())
            throw new FileNotFoundException("Xslt File " + aXsltFilePath
                    + " doesn't exist");
        
        m_NoXslt = false;
    }

    /**
     * return the ReleaseDB
     * 
     * @return
     */
    public ReleaseDatabase getReleaseDatabase()
    {
        return this.m_ReleaseDB;
    }
    
    public void createExport(String aMetadataSourcePath, boolean aNoCheck) throws Exception
    {
        this.createExport(aMetadataSourcePath, null, aNoCheck);
    }

    /**
     * 
     * @param aMetadataSourcePath Input Dir
     * @param aOutputDir  Output Dir
     * @param aNoCheck    to checkor not the metadata
     * @throws Exception
     */
    public void createExport(String aMetadataSourcePath, String aOutputDir, boolean aNoCheck) throws Exception
    {
        File topTempDir = FileSystem.createTempDirectory("temp-",
                this.m_WorkingDir);

        boolean prettyPrint = true;

        
        File tempDir = new File(topTempDir + File.separator + "temp");
        File xmlDir = new File(tempDir.getAbsolutePath() + File.separator
                + Release.XML_FILES);
        File MD5Dir = new File(tempDir.getAbsolutePath() + File.separator
                + Release.MD5_FILES);

        FileSystem.createDirs(xmlDir);
        FileSystem.createDirs(MD5Dir);

        if (! m_NoXslt)
        {
            logger.info("------------ Do XSLT Transformations ------------");
        
            // do the transformations
            XsltProcessor xsltTransformer = new XsltProcessor(
                    this.m_XsltFilePath, xmlDir);

            // do xslt transformation
            xsltTransformer.processFiles(new File(aMetadataSourcePath),
                    prettyPrint);
        }
        else
        {
            FileUtils.copyDirectory(new File(aMetadataSourcePath), xmlDir);
        }
        
        // rename files
        MetadataFileRenamer rn = new MetadataFileRenamer(xmlDir);
        
        // add sanity check to be sure that we are using transformed files
        // check that the files are XSLT transformed files
        if (! aNoCheck)
           rn.doSanityCheck(2);

        logger.info("------------ Rename Files            ------------");
        
        rn.processFiles();

        logger.info("------------ Calculate MD5s          ------------");

        // calculate MD5s
        calculateMD5s(xmlDir, MD5Dir);

        logger.info("------------ Calculate Delta         ------------");

        // calculate Delta from previous Release
        Release newRelease = calculateDelta(topTempDir, xmlDir, MD5Dir);

        // move in ReleaseDB if non empty
        if (newRelease.hasADelta())
        {
            Release latest = this.m_ReleaseDB.add(newRelease);
            logger.info("Created release {} in ReleaseDB.", latest.getName());
            
            // expose Delta
            if (aOutputDir != null)
            {
               logger.info("Export release {} to {}.", latest.getName(), aOutputDir);
               latest.exportReleaseDeltaTo(aOutputDir);
            }
        }
        else
        {
            logger.info("No new release.");
        }
    }

    /**
     * Generate the MD5 values for each of the files
     * 
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
        
        if (files2Process == null)
            throw new Exception("Cannot calculate MD5s. InputDir " + aInputDir.getPath() + " doesn't exist");

        logger.info("Creating {} MD5 files.", files2Process.length);
        
        String outputFilename;
        for (File file : files2Process)
        {

            outputFilename = aOutputDir + File.separator
                    + FilenameUtils.getBaseName(file.getName()) + ".md5";

            Checksummer.doMD5Checksum(file, new File(outputFilename));
        }
    }

    /**
     * generate the delta dataset that will be exported to the GISC
     * 
     * @param aTempXmlDir
     * @param aTempMD5Dir
     * @throws Exception
     */
    public Release calculateDelta(File aTopTempDir, File aTempXmlDir,
            File aTempMD5Dir) throws Exception
    {
        // create a temp release
        File releaseTempDir = FileSystem.createTempDirectory("release-",
                aTopTempDir);

        Release tempRelease = Release.createRelease(releaseTempDir);

        Release latestRelease = m_ReleaseDB.getLatestRelease();

        // copy the SOURCE Part of Release
        tempRelease.addInSrcMD5s(aTempMD5Dir);
        tempRelease.addInSrcXmls(aTempXmlDir);

        // No previous Release so all origin XML files go into Delta Result and
        // there are no deleted files
        if (latestRelease == null)
        {
            // Copy XML into Delta/Result
            tempRelease.addInDeltaResult(aTempXmlDir);
        }
        else
        {
            // get list of previous MD5s and the list of current MD5s
            HashMap<String, Pair<String, String>> prev = latestRelease
                    .getSrcMD5s();
            HashMap<String, Pair<String, String>> curr = tempRelease
                    .getSrcMD5s();

            Set<String> currSet = new HashSet<String>();
            Set<String> deletedSet = new HashSet<String>();
            Set<String> newSet = new HashSet<String>();

            /*
             * calculate the delta It is important to understand that the UID is
             * the MD_Metadata.fileidentifier and not the filename. Several
             * modifications of the same metadata in the Release are not handled
             * properly here (this should not happen but). algorithm: for
             * elemOld in old: if elemOld not in new: add in deletedSet if
             * elemOld in new and elemOld.md5 != elemNew.md5: add in newSet
             * 
             * remove elemOld from currSet
             * 
             * All elem left in currSet are the new ones and must be added in
             * newSet newSet.add(currSet);
             */

            // copy curr keys in currSet
            currSet.addAll(curr.keySet());

            for (String key : prev.keySet())
            {
                if (!curr.containsKey(key))
                {
                    deletedSet.add(key);
                }
                else
                {
                    if (!prev.get(key).equals(curr.get(key)))
                    {
                        // md5 different so the file has been updated
                        logger.debug(
                                "MD5 Differents for {}. old:[{}], new:[{}]",
                                new String[] { key, prev.get(key).getKey(),
                                        curr.get(key).getKey() });
                        
                        logger.info("{} metadata has been modified", key);

                        // add in newSet
                        newSet.add(key);
                    }

                    // remove from currSet in any case
                    currSet.remove(key);
                }
            }

            // elems left in currSet are new and need to be added to newSet
            newSet.addAll(currSet);

            String basename;
            // copy newSet in Delta/Files
            for (String name : newSet)
            {
                // add files in Results and their MD5s in MD5

                // get the basename from curr HashMap
                basename = curr.get(name).getValue();

                tempRelease.addFileInDeltaResult(new File(aTempXmlDir
                        + File.separator + basename + ".xml"));
            }

            // add files in deleted
            for (String name : deletedSet)
            {
                tempRelease.flagAsDeleted(name);
            }
        }

        return tempRelease;
    }
}
