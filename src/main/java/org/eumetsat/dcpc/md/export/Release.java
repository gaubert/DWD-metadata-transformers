package org.eumetsat.dcpc.md.export;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eumetsat.dcpc.commons.FileSystem;

/**
 * Represent a Release
 * @author guillaume.aubert@eumetsat.int
 *
 */
public class Release
{
   // Internal directories
   public final static String MD5_FILES = "MD5-Files";
   public final static String XML_FILES = "XML-Files";
    
   // Root dirs
   public final static String DELTA     = "DELTA";
   public final static String SOURCE    = "SOURCE";
   
   public final static String SRC_XMLS  = SOURCE + File.separator + XML_FILES;
   public final static String SRC_MD5S  = SOURCE + File.separator + MD5_FILES;
   public final static String RESULT    = DELTA  + File.separator + "RESULT";
   public final static String DELTA_MD5S = DELTA  + File.separator + MD5_FILES;
   
   protected File m_ReleaseTopDir;
   protected File m_Delta;
   protected File m_Src;
   protected File m_SrcXmls;
   protected File m_SrcMd5s;
   protected File m_Result;
   protected File m_DeltaMd5s;
   
   
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
       FileSystem.createDirs(aReleaseTopDir + File.separator + DELTA);
       FileSystem.createDirs(aReleaseTopDir + File.separator + SOURCE);
       
       FileSystem.createDirs(aReleaseTopDir + File.separator + SRC_XMLS);
       FileSystem.createDirs(aReleaseTopDir + File.separator + SRC_MD5S);
       FileSystem.createDirs(aReleaseTopDir + File.separator + RESULT);
       FileSystem.createDirs(aReleaseTopDir + File.separator + DELTA_MD5S);
       
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
       String relevant_dirs[] = this.m_ReleaseTopDir.list(new FilenameFilter() {
           public boolean accept(File dir, String name)
           {
               return ( name.equals(DELTA) || name.equals(SOURCE) ) ? true : false;
           }
       });
       
       if (relevant_dirs.length != 2)
       {
           throw new Exception("Error. " + m_ReleaseTopDir + " doesn't seem to be a Release directory as it doesn't contain " + MD5_FILES + " ," + XML_FILES + " and" + DELTA);
       }
       
       m_Delta     = new File(this.m_ReleaseTopDir + File.separator + DELTA);
       m_Src       = new File(this.m_ReleaseTopDir + File.separator + SOURCE);
       m_Result    = new File(this.m_ReleaseTopDir + File.separator + RESULT);
       m_DeltaMd5s = new File(this.m_ReleaseTopDir + File.separator + DELTA_MD5S);
       
   }
   
   public String getName()
   {
       return this.m_ReleaseTopDir.getName();
   }
   
   /**
    * Add all files contained in aXmlDir into the SRC XML Release directory
    * @param aXmlDir
 * @throws IOException 
    */
   public void addInSrcXmls(File aXmlDir) throws IOException
   {
       FileUtils.copyDirectory(aXmlDir, this.m_SrcXmls, false);
   }
   
   public void addInSrcMD5s(File aMD5Dir) throws IOException
   {
       FileUtils.copyDirectory(aMD5Dir, this.m_SrcMd5s, false);
   }
   
   public void addInDeltaResult(File aDir) throws IOException
   {
       FileUtils.copyDirectory(aDir, this.m_Result, false);
   }
   
   public void addFileInDeltaResult(File aFile) throws IOException
   {
       FileUtils.copyFileToDirectory(aFile, this.m_Result, false);
   }
   
   /**
    * Return the DeltaMD5 info in a workable data structure.
    * A HashMap<filename,MD5>
    * @return the hashMap<filename,MD5>
    */
   public HashMap<String,String> getDeltaMD5s() throws Exception
   {
       HashMap<String, String> hMap = new HashMap<String,String>();
       
       // get list of MD5 files
       String relevant_files[] = this.m_DeltaMd5s.list(new FilenameFilter() {
           public boolean accept(File dir, String name)
           {
               return name.endsWith(".xml");
           }
       });
       
       File       md5F = null;
       String key      = null;
       String val      = null;
       for (String filepath : relevant_files)
       {
          md5F = new File(filepath);
          key  = FilenameUtils.getBaseName(filepath);
          val  = FileUtils.readFileToString(md5F);
          
          hMap.put(key, val);
       }
       
       return hMap;
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
