package org.eumetsat.dcpc.md.export;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import org.eumetsat.dcpc.commons.FileSystem;

/**
 * Represent a Release
 * @author guillaume.aubert@eumetsat.int
 *
 */
public class Release
{
   public final static String MD5_FILES = "MD5-Files";
   public final static String XML_FILES = "XML-Files";
   public final static String DELTA     = "DELTA";
   
   protected File m_ReleaseTopDir;
   protected File m_MD5s;
   protected File m_XMLs;
   protected File m_Delta;
   
   /** 
    * Create a Release directory and its underneath structure
    * @param aReleaseTopDir
    * @return
    * @throws Exception
    */
   public static Release createRelease(File aReleaseTopDir) throws Exception
   {
       Release release = null;
       
       // Create Release and the 3 dirs if necessary
       FileSystem.createDirs(aReleaseTopDir + File.separator + MD5_FILES);
       FileSystem.createDirs(aReleaseTopDir + File.separator + XML_FILES);
       FileSystem.createDirs(aReleaseTopDir + File.separator + DELTA);
       
       release = new Release(aReleaseTopDir);
       
       return release;
   }
   
   public static void deleteRelease(Release aRelease)
   {
       FileSystem.deleteDirs(aRelease.getRootDir());
   }
   
   /**
   * Constructor
 * @throws Exception 
   */
   public Release(File aReleaseTopDir) throws Exception
   {
       m_ReleaseTopDir = aReleaseTopDir;
       init();
   }
   
   private void init() throws Exception
   {
       String relevant_dirs[] = m_ReleaseTopDir.list(new FilenameFilter() {
           public boolean accept(File dir, String name)
           {
               return ( name.equals(MD5_FILES) || name.equals(XML_FILES) || name.equals(DELTA) ) ? true : false;
           }
       });
       
       if (relevant_dirs.length != 3)
       {
           throw new Exception("Error. " + m_ReleaseTopDir + " doesn't seem to be a Release directory as it doesn't contain " + MD5_FILES + " ," + XML_FILES + " and" + DELTA);
       }
   }
   
   public String getName()
   {
       return this.m_ReleaseTopDir.getName();
   }
   
   public ArrayList<?> getMD5s()
   {
       return null;
   }
   
   
   
   public ArrayList<?> getXmls()
   {
       return null;
   }
   
   public void getDelta()
   {
       
   }
   
   public File getRootDir()
   {
       return this.m_ReleaseTopDir;
   }
   
   
   
   
}
