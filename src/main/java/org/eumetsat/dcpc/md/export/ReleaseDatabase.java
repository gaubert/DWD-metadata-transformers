package org.eumetsat.dcpc.md.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import org.eumetsat.dcpc.commons.DateFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReleaseDatabase
{
    public final static Logger logger = LoggerFactory.getLogger(ReleaseDatabase.class);
    
    
    protected String m_ReleaseDBRootDirPath;
    protected File   m_RDBRootDir;
    
    // Releases list
    protected ArrayList<Release>       m_Releases = new ArrayList<Release>();
    // Release index
    protected HashMap<String, Release> m_RIndex   = new HashMap<String, Release>();
    
    public ReleaseDatabase(String aReleaseDBRootDirPath) throws Exception
    {
       m_ReleaseDBRootDirPath = aReleaseDBRootDirPath; 
        
       _loadDB();
    }
    
    /**
     * Force a DB reload from disk
     * @throws Exception 
     */
    public void reloadDB() throws Exception
    {
        this._loadDB();
    }
    
    /**
     * check that the releaseDB Root dir exists and is the DB root dir
     * @throws FileNotFoundException 
     */
    private void _loadDB() throws Exception
    {
        m_RDBRootDir = new File(m_ReleaseDBRootDirPath);
        
        if (! m_RDBRootDir.exists())
        {
            throw new FileNotFoundException(m_ReleaseDBRootDirPath + " does not exist");
        }
        
        if (! m_RDBRootDir.isDirectory())
        {
            throw new IOException(m_ReleaseDBRootDirPath + " should be a directory");
        }
        
        // purge list of Releases and this index
        m_Releases.clear();
        m_RIndex.clear();
        
        // load Releases    
        File[] files = m_RDBRootDir.listFiles();
               
        logger.info("Loaded {} Release dirs", files.length);
        
        // sort by name ascending
        Arrays.sort(files, new Comparator<File>()
        {
            public int compare(File f1, File f2)
            {
                return f1.getName().compareTo(f2.getName());
            } 
        });
        
        Release aRelease = null;
        // Create release list and its index
        for (File file : files)
        {
            aRelease = new Release(file);
            m_Releases.add(aRelease);
            m_RIndex.put(aRelease.getName(), aRelease);
        }  
    }
    
    public int getNbOfReleases()
    {
        return this.m_Releases.size();
    }
    
    /**
     * Return the release provided the given name
     * @param aReleaseName
     * @return return the release if it exists otherwise return null
     */
    public Release getRelease(String aReleaseName)
    {
        return this.m_RIndex.get(aReleaseName);
    }
    
    /**
     * Return the latest release if there is one otherwise null
     * @return the latest release or null
     */
    public Release getLatestRelease()
    {
       return (this.m_Releases.size() > 0) ? this.m_Releases.get(this.m_Releases.size()-1) : null; 
    }
    
    /**
     * Return the previous release of the passed one.
     * @param aOfThisRelease
     * @return the previous release otherwise null if there is no previous one
     * @throws Exception if the passed Release is not in the Database
     */
    public Release getPreviousRelease(Release aOfThisRelease) throws Exception
    {
        int index = m_Releases.indexOf(aOfThisRelease);
        
        if (index == -1)
            throw new Exception("Release " + aOfThisRelease + " is not in the ReleaseDB");
        
        if (index == 0)
        {
            return null;
        }
        else
        {
            return m_Releases.get(index -1 );
        }
    }
    
    /**
     * Create a Release
     * @return a Release
     * @throws Exception Fatal Exception in case of issue
     */
    public Release createRelease() throws Exception
    {
        // get now date
        Date date = new Date();
        File theReleaseFile   = new File(this.m_ReleaseDBRootDirPath + File.separator + DateFormatter.dateToString(date));
        Release theNewRelease = Release.createRelease(theReleaseFile);
        // add the new Release in the list of R and in its associated index
        this.m_Releases.add(theNewRelease);
        this.m_RIndex.put(theNewRelease.getName(), theNewRelease);
        
        return theNewRelease;
    }
    
    /**
     * Remove the given Release from the database
     * @param aRelease
     */
    public void deleteRelease(Release aRelease)
    {
       Release.deleteRelease(aRelease); 
       // remove Release from index and list
       this.m_Releases.remove(aRelease);
       this.m_RIndex.remove(aRelease.getName());
    }
}
