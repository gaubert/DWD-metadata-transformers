package org.eumetsat.dcpc.commons;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author guillaume.aubert@eumetsat.int
 * 
 */
public class FileSystem
{

    protected final static Logger logger = LoggerFactory
                                                 .getLogger(FileSystem.class);

    /**
     * Delete Root Directory and all its sibblings
     * 
     * @param path
     *            Root Dir from where to delete
     */
    public static void deleteDirs(File path)
    {
        try
        {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; ++i)
            {
                if (files[i].isDirectory())
                {
                    deleteDirs(files[i]);
                }
                files[i].delete();
            }
            path.delete();
        }
        catch (Exception ignored)
        {
            logger.error("Cannot deleteDirs", ignored);
        }
    }

    /**
     * Delete Root Directory and all its sibblings
     * 
     * @param path
     *            Root Dir name from where to delete
     */
    public static void deleteDirs(String aPathName)
    {
        try
        {
            File path = new File(aPathName);

            File[] files = path.listFiles();

            for (int i = 0; i < files.length; ++i)
            {
                if (files[i].isDirectory())
                {
                    deleteDirs(files[i]);
                }
                files[i].delete();
            }
            path.delete();
        }
        catch (Exception ignored)
        {
            logger.error("Cannot deleteDirs ", ignored);
        }
    }

    /**
     * Create TempDirectory in the given root dir
     * 
     * @param aRootDir
     *            if null then use the default temp dir
     * @return the created temp dir
     * @throws IOException
     */
    public static File createTempDirectory(String aPrefix, File aRootDir)
            throws IOException
    {
        final File temp;

        temp = File.createTempFile(aPrefix, Long.toString(System.nanoTime()),
                aRootDir);

        if (!(temp.delete()))
        {
            throw new IOException("Could not delete temp file: "
                    + temp.getAbsolutePath());
        }

        if (!(temp.mkdir()))
        {
            throw new IOException("Could not create temp directory: "
                    + temp.getAbsolutePath());
        }

        return (temp);
    }

    /**
     * CreateDir if necessary (error if dir cannot be created)
     * 
     * @param aFile
     * @throws IOException
     */
    public static void createDirs(File aFile) throws IOException
    {
        if (aFile == null)
            throw new IllegalArgumentException("aFile null");

        // if does not exist create them
        if (!aFile.exists())
        {
            aFile.mkdirs();
        }
        else
        {
            // if not a directory then error
            if (!aFile.isDirectory())
                throw new IOException(aFile.getCanonicalPath()
                        + " isn't a directory");
        }
    }

    /**
     * CreateDir if necessary (error if dir cannot be created)
     * 
     * @param aPathName
     * @throws IOException
     */
    public static void createDirs(String aPathName) throws IOException
    {
        File file = new File(aPathName);

        // if does not exist create them
        if (!file.exists())
        {
            file.mkdirs();
        }
        else
        {
            // if not a directory then error
            if (!file.isDirectory())
                throw new IOException(aPathName + " isn't a directory");
        }
    }

    /**
     * Return the files names (file or directory names) contained by the
     * Directory
     * 
     * @param _path
     * @return Array of Files. null IOError or aPath isn't a directory. empty
     *         array if no files
     */
    public static String[] listDirectoryAsString(String aPath)
    {
        if (aPath == null)
            return null;

        File dir = new File(aPath);

        return dir.list();
    }

    public static File[] listDirectory(String aPath, FileFilter aFileFilter)
    {
        if (aPath == null)
            return null;

        File dir = new File(aPath);

        if (dir == null)
            return null;

        return ((aFileFilter == null) ? dir.listFiles() : dir
                .listFiles(aFileFilter));
    }

    /**
     * 
     * @param _path
     * @return Array of Files. null IOError or aPath isn't a directory. empty
     *         array if no files
     */
    public static File[] listDirectory(String aPath)
    {
        return listDirectory(aPath, null);
    }

    public static File[] listDirectory(File aDir, FileFilter aFileFilter)
    {
        if ((aDir == null))
            return null;

        return ((aFileFilter == null) ? aDir.listFiles() : aDir
                .listFiles(aFileFilter));
    }

    /**
     * return the Directory files
     * 
     * @param aDir
     *            Directory to list
     * @return Array of Files. null IOError or aPath isn't a directory. empty
     *         array if no files
     */
    public static File[] listDirectory(File aDir)
    {
        return listDirectory(aDir, null);
    }

}
