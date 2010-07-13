package org.eumetsat.dcpc.md.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class ReleaseDatabase
{
    protected String m_ReleaseDBRootDirPath;
    protected File   m_RDBRootDir;
    
    protected ArrayList<File> m_Releases; 
    
    public ReleaseDatabase(String aReleaseDBRootDirPath) throws IOException
    {
       m_ReleaseDBRootDirPath = aReleaseDBRootDirPath; 
        
       loadDB();
    }
    
    /**
     * check that the releaseDB Root dir exists and is the DB root dir
     * @throws FileNotFoundException 
     */
    private void loadDB() throws IOException
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
        
        // load Releases    
        File[] files = m_RDBRootDir.listFiles();
        
        // sort by name ascending
        Arrays.sort(files, new Comparator<File>()
        {
            public int compare(File f1, File f2)
            {
                return f1.getName().compareTo(f2.getName());
            } 
        });
        
        // store them in an arrayList
        m_Releases = new ArrayList<File>(Arrays.asList(files));
        
    }
}
