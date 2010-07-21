package org.eumetsat.dcpc.md.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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

    public final static Logger     logger               = LoggerFactory
                                                                .getLogger(ReleaseDatabase.class);

    protected static final boolean TEMP_DIR_DELETION_ON = true;

    /**
     * Constructor
     * 
     * @param aReleaseDBRootDirPath
     *            The Root Path for the ReleaseDB
     * @throws Exception
     */
    public MetadataExporter(String aXsltFilePath, String aReleaseDBRootDirPath,
            String aWorkingDirPath) throws Exception
    {

        m_XsltFilePath = new File(aXsltFilePath);

        if (!m_XsltFilePath.exists())
            throw new FileNotFoundException("Xslt File " + aXsltFilePath
                    + " doesn't exist");

        m_ReleaseDB = new ReleaseDatabase(aReleaseDBRootDirPath);

        FileSystem.createDirs(aWorkingDirPath);

        m_WorkingDir = new File(aWorkingDirPath);

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
    
    public void createExport(String aMetadataSourcePath) throws Exception
    {
        this.createExport(aMetadataSourcePath, null);
    }

    /**
     * 
     * @param aMetadataSourcePath
     * @throws Exception
     */
    public void createExport(String aMetadataSourcePath, String aOutputDir) throws Exception
    {
        File topTempDir = FileSystem.createTempDirectory("temp-",
                this.m_WorkingDir);

        boolean prettyPrint = true;

        try
        {
            File tempDir = new File(topTempDir + File.separator + "temp");
            File xmlDir = new File(tempDir.getAbsolutePath() + File.separator
                    + Release.XML_FILES);
            File MD5Dir = new File(tempDir.getAbsolutePath() + File.separator
                    + Release.MD5_FILES);

            FileSystem.createDirs(xmlDir);
            FileSystem.createDirs(MD5Dir);

            logger.info("********* Do XSLT Transformations *********");

            // do the transformations
            XsltProcessor xsltTransformer = new XsltProcessor(
                    this.m_XsltFilePath, xmlDir);

            // do xslt transformation
            xsltTransformer.processFiles(new File(aMetadataSourcePath),
                    prettyPrint);

            logger.info("********* Rename Files *********");
            // rename files
            MetadataFileRenamer rn = new MetadataFileRenamer(xmlDir);

            rn.processFiles();

            logger.info("********* Calculate MD5s *********");

            // calculate MD5s
            calculateMD5s(xmlDir, MD5Dir);

            logger.info("********* Calculate Delta *********");

            // calculate Delta from previous Release
            Release newRelease = calculateDelta(topTempDir, xmlDir, MD5Dir);

            // move in ReleaseDB if non empty
            if (newRelease.hasADelta())
            {
                logger.info("********* Create New Release *********");
                this.m_ReleaseDB.add(newRelease);
                
                // expose Delta
                if (aOutputDir != null)
                   this.m_ReleaseDB.getLatestRelease().exportReleaseDeltaTo(aOutputDir);
            }
            else
            {
                logger.info("********* No New Release *********");
            }

           
        }
        finally
        {

            // delete the temporary directory in any case
            if (TEMP_DIR_DELETION_ON)
                FileSystem.deleteDirs(topTempDir);
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
                        logger.info(
                                "MD5 Differents for {}. old:[{}], new:[{}]",
                                new String[] { key, prev.get(key).getKey(),
                                        curr.get(key).getKey() });

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
