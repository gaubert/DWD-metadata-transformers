package org.eumetsat.dcpc.md.export;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eumetsat.dcpc.commons.DateUtil;
import org.eumetsat.dcpc.commons.FileSystem;
import org.eumetsat.dcpc.commons.Pair;
import org.eumetsat.dcpc.commons.xml.XMLInjector;

/**
 * Represent a Release
 * 
 * @author guillaume.aubert@eumetsat.int
 * 
 */
public class Release
{
    // Internal directories
    public final static String MD5_FILES      = "MD5-Files";
    public final static String XML_FILES      = "XML-Files";

    // Root dirs
    public final static String DELTA          = "DELTA";
    public final static String SOURCE         = "SOURCE";

    public final static String SRC_XMLS       = SOURCE + File.separator
                                                      + XML_FILES;
    public final static String SRC_MD5S       = SOURCE + File.separator
                                                      + MD5_FILES;

    public final static String DELETED_PREFIX = "deleted.eumetsat.";
    // DELETED Path
    public final static String DELETED        = DELTA + File.separator
                                                      + DELETED_PREFIX;
    public final static String DELETED_SUFFIX = ".txt";

    protected File             m_ReleaseTopDir;
    protected File             m_Delta;
    protected File             m_Src;
    protected File             m_SrcXmls;
    protected File             m_SrcMd5s;

    // Deleted file
    protected File             m_Deleted;
    private   String m_Name;

    /**
     * Create a Release directory and its underneath structure
     * 
     * @param aReleaseTopDir
     * @return
     * @throws Exception
     */
    public static Release createRelease(File aReleaseTopDir) throws Exception
    {
        Release release = null;

        // Create Release and the 3 dirs if necessary
        FileSystem.createDirs(aReleaseTopDir + File.separator + DELTA);
        FileSystem.createDirs(aReleaseTopDir + File.separator + SOURCE);

        FileSystem.createDirs(aReleaseTopDir + File.separator + SRC_XMLS);
        FileSystem.createDirs(aReleaseTopDir + File.separator + SRC_MD5S);

        release = new Release(aReleaseTopDir);

        return release;
    }

    public static void deleteRelease(Release aRelease) throws IOException
    {
        FileUtils.deleteDirectory(aRelease.getRootDir());
    }

    /**
     * Constructor
     * 
     * @throws Exception
     */
    public Release(File aReleaseTopDir) throws Exception
    {
        m_Name = FilenameUtils.getBaseName(aReleaseTopDir.getName());
        m_ReleaseTopDir = aReleaseTopDir;
        init();
    }

    private void init() throws Exception
    {
        String relevant_dirs[] = this.m_ReleaseTopDir
                .list(new FilenameFilter() {
                    public boolean accept(File dir, String name)
                    {
                        return (name.equals(DELTA) || name.equals(SOURCE)) ? true
                                : false;
                    }
                });
        
        if (relevant_dirs == null)
        {
            throw new Exception(m_ReleaseTopDir + " is not a directory");
        }

        if (relevant_dirs.length != 2)
        {
            throw new Exception(
                            m_ReleaseTopDir
                            + " is not a Release directory as it doesn't contain a "
                            + DELTA + " dir and a " + SOURCE + " dir.");
        }

        // preload src info
        m_Src = new File(this.m_ReleaseTopDir + File.separator + SOURCE);
        m_SrcMd5s = new File(this.m_ReleaseTopDir + File.separator + SRC_MD5S);
        m_SrcXmls = new File(this.m_ReleaseTopDir + File.separator + SRC_XMLS);

        // preload delta info
        m_Delta = new File(this.m_ReleaseTopDir + File.separator + DELTA);

        m_Deleted = getDeletedFile();

    }

    /**
     * load the deleted file if it exists or create a new filename if necessary
     * 
     * @return the deleted file
     * @throws Exception
     */
    private File getDeletedFile() throws Exception
    {
        File deleted = null;
        String relevant_files[] = this.m_Delta.list(new FilenameFilter() {
            public boolean accept(File dir, String name)
            {
                return name.startsWith(DELETED_PREFIX);
            }
        });

        if (relevant_files.length > 1)
        {
            throw new Exception("More than one deleted file in "
                    + this.m_Delta);
        }

        else if (relevant_files.length == 0)
        {
            deleted = new File(this.m_ReleaseTopDir
                    + File.separator
                    + DELETED
                    + DateUtil.dateToString(new Date(),
                            DateUtil.ms_DELETEDATEFORMAT) + DELETED_SUFFIX);
        }
        else
        {
            // there is one file
            deleted = new File(this.m_ReleaseTopDir + File.separator + DELTA
                    + File.separator + relevant_files[0]);
        }

        return deleted;
    }

    public String getName()
    {
        return this.m_ReleaseTopDir.getName();
    }

    /**
     * Add all files contained in aXmlDir into the SRC XML Release directory
     * 
     * @param aXmlDir
     * @throws IOException
     */
    public void addInSrcXmls(File aXmlDir) throws IOException
    {
        FileUtils.copyDirectory(aXmlDir, this.m_SrcXmls, false);
    }

    /**
     * Add all files contained in aMD5Dir into SRC MD5s
     * 
     * @param aMD5Dir
     * @throws IOException
     */
    public void addInSrcMD5s(File aMD5Dir) throws IOException
    {
        FileUtils.copyDirectory(aMD5Dir, this.m_SrcMd5s, false);
    }

    /**
     * Add all the XML files in the DELTA directory
     * 
     * @param aDir
     * @throws IOException
     */
    public void addInDeltaResult(File aDir) throws Exception
    {
        // get list of files in the delta dir
        File relevant_files[] = aDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name)
            {
                // xml file
                return name.endsWith(".xml");
            }
        });

        for (File file : relevant_files)
        {
            this.addFileInDeltaResult(file);
        }
    }

    /**
     * Add a unique xml file in Delta Result
     * 
     * @param aFile
     * @throws Exception
     */
    public void addFileInDeltaResult(File aFile) throws Exception
    {
        // trick to update the dateStamp in the XML file
        Date utcD = DateUtil.getUTCCurrentTime();

        // modify file n the fly (overwrite existing file)
        XMLInjector.injectStringIntoNode(aFile.getAbsolutePath(), aFile
                .getAbsolutePath(),
                "/gmd:MD_Metadata/gmd:dateStamp/gco:Date/text()", DateUtil
                        .dateToString(utcD, DateUtil.ms_ISODATEFORMAT),
                XMLInjector.NAMESPACES);

        // copy file to the Delta Directory
        FileUtils.copyFileToDirectory(aFile, this.m_Delta, true);

        // rename the file to have the date in the right format in its name
        // + DateUtil.dateToString(modDate, DateUtil.ms_DELETEDATEFORMAT)
        String filename = this.m_Delta.getAbsolutePath() + File.separator
                + FilenameUtils.getBaseName(aFile.getName());

        File oldFile = new File(filename + ".xml");
        // follow this filename pattern
        // Z_EO_EUM_DAT_MSG_HRSEVIRI_C_EUMS_20090831000000.xml
        File newFile = new File(filename + "_"
                + DateUtil.dateToString(utcD, DateUtil.ms_DELETEDATEFORMAT)
                + ".xml");

        FileUtils.moveFile(oldFile, newFile);
    }

    public void flagAsDeleted(String aFileIdentifier) throws Exception
    {
        RandomAccessFile deleted = new RandomAccessFile(m_Deleted, "rwd");
        // Seek to end of file
        deleted.seek(m_Deleted.length());

        deleted.writeBytes(aFileIdentifier + "\n");
        deleted.close();
    }

    /**
     * Return the SrcMD5s info in a workable data structure:
     * hashMap<EOPortal-fileIdentifier,Pair(MD5,MD5FileBasename) The
     * EOPortal-fileIdentifier is the original fileIdentifier. For example
     * EO_EUM_DAT_GOES_GWW MD5FileBasename is the filename
     * Z_EO_EUM_DAT_GOES_GWW_C_EUMS_20100512000000
     * 
     * @return the hashMap<EOPortal-fileIdentifier,Pair(MD5,MD5FileBasename)>
     */
    public HashMap<String, Pair<String, String>> getSrcMD5s() throws Exception
    {
        HashMap<String, Pair<String, String>> hMap = new HashMap<String, Pair<String, String>>();

        // get list of MD5 files
        String relevant_files[] = this.m_SrcMd5s.list(new FilenameFilter() {
            public boolean accept(File dir, String name)
            {
                return name.endsWith(".md5");
            }
        });

        File md5F = null;
        String key = null;

        for (String filename : relevant_files)
        {
            md5F = new File(this.m_SrcMd5s.getAbsolutePath() + File.separator
                    + filename);
            key = MetadataFileRenamer
                    .extractFileIdentifierFromWMOFilename(filename);

            hMap.put(key, new Pair<String, String>(FileUtils
                    .readFileToString(md5F), FilenameUtils
                    .getBaseName(filename)));
        }

        return hMap;
    }

    /**
     * Check if there is a Delta. If no Delta, this is an empty Release
     * 
     * @return true if there is a Delta, false otherwise
     */
    public boolean hasADelta()
    {
        // get list of files in the delta dir
        String relevant_files[] = this.m_Delta.list(new FilenameFilter() {
            public boolean accept(File dir, String name)
            {
                // xml file or deleted
                return name.endsWith(".xml") || name.startsWith(DELETED_PREFIX);
            }
        });

        return (relevant_files.length > 0) ? true : false;
    }
    
    public void exportReleaseDeltaTo(String aOutputDir) throws Exception
    {
        File outputDir = new File(aOutputDir);
        
        File finalDest = new File(outputDir + File.separator + "Release-" + this.m_Name);
        
        // try to create the Dirs
        FileSystem.createDirs(finalDest);
       
        FileUtils.copyDirectory(this.m_Delta, finalDest); 
    }

    /**
     * Return the list of xml files in the Delta
     * 
     * @return List of xml filenames
     */
    public ArrayList<String> getDeltaXmlFilenames()
    {
        // get list of MD5 files
        String relevant_files[] = this.m_Delta.list(new FilenameFilter() {
            public boolean accept(File dir, String name)
            {
                // xml file or deleted
                return name.endsWith(".xml");
            }
        });

        return new ArrayList<String>(Arrays.asList(relevant_files));
    }

    /**
     * Get the list of deleted filenames from the deleted file
     * 
     * @return
     * @throws Exception
     */
    public ArrayList<String> getDeltaDeletedFilenames() throws Exception
    {
        ArrayList<String> result = new ArrayList<String>();

        if (m_Deleted.exists())
        {
            String line = null;
            RandomAccessFile deletedF = new RandomAccessFile(m_Deleted, "r");
            try
            {
                while ((line = deletedF.readLine()) != null)
                {
                    result.add(line);
                }
            }
            finally
            {
                deletedF.close();
            }

        }

        return result;
    }

    public File getRootDir()
    {
        return this.m_ReleaseTopDir;
    }

}
