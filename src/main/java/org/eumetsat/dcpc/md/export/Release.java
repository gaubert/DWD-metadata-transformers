package org.eumetsat.dcpc.md.export;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
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
   public final static String DELTA_MD5S = DELTA + File.separator + MD5_FILES;
   
   public final static String DELETED   = RESULT + File.separator + "deleted";
   
   protected File m_ReleaseTopDir;
   protected File m_Delta;
   protected File m_Src;
   protected File m_SrcXmls;
   protected File m_SrcMd5s;
   protected File m_Result;
   protected File m_DeltaMd5s;
   
   // Deleted file
   protected File m_Deleted;
   
   
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
       
       
       // preload src info
       m_Src       = new File(this.m_ReleaseTopDir + File.separator + SOURCE);
       m_SrcMd5s   = new File(this.m_ReleaseTopDir + File.separator + SRC_MD5S);
       m_SrcXmls   = new File(this.m_ReleaseTopDir + File.separator + SRC_XMLS);
       
       // preload delta info
       m_Delta     = new File(this.m_ReleaseTopDir + File.separator + DELTA);
       m_Result    = new File(this.m_ReleaseTopDir + File.separator + RESULT);
       m_DeltaMd5s = new File(this.m_ReleaseTopDir + File.separator + DELTA_MD5S);
       
       m_Deleted   = new File(this.m_ReleaseTopDir + File.separator + DELETED);
       
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
   
   /**
    * Add all files contained in aMD5Dir into SRC MD5s
    * @param aMD5Dir
    * @throws IOException
    */
   public void addInSrcMD5s(File aMD5Dir) throws IOException
   {
       FileUtils.copyDirectory(aMD5Dir, this.m_SrcMd5s, false);
   }
   
   /**
    * Add all files contained in aDir into Delta Result
    * @param aDir
    * @throws IOException
    */
   public void addInDeltaResult(File aDir) throws IOException
   {
       FileUtils.copyDirectory(aDir, this.m_Result, false);
   }
   
   /**
    * Add a unique file in Delta Result
    * @param aFile
    * @throws IOException
    */
   public void addFileInDeltaResult(File aFile) throws IOException
   {
       FileUtils.copyFileToDirectory(aFile, this.m_Result, true);
   }
   
   /**
    * Add all files contained in aDir into Delta Result
    * @param aDir
    * @throws IOException
    */
   public void addInDeltaMD5s(File aDir) throws IOException
   {
       FileUtils.copyDirectory(aDir, this.m_DeltaMd5s, true);
   }
   
   /**
    * Add a unique file in Delta MD5s
    * @param aFile
    * @throws IOException
    */
   public void addFileInDeltaMD5s(File aFile) throws IOException
   {
       FileUtils.copyFileToDirectory(aFile, this.m_DeltaMd5s, false);
   }
   
   public void flagAsDeleted(String aFileName) throws Exception
   {
       RandomAccessFile deleted = new RandomAccessFile(m_Deleted, "rwd");
       deleted.writeBytes(aFileName);
       deleted.close();
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
               return name.endsWith(".md5");
           }
       });
       
       File       md5F = null;
       String key      = null;
       String val      = null;
       for (String filename : relevant_files)
       {
          md5F = new File(this.m_DeltaMd5s.getAbsolutePath() + File.separator + filename);
          key  = FilenameUtils.getBaseName(filename);
          val  = FileUtils.readFileToString(md5F);
          
          hMap.put(key, val);
       }
       
       return hMap;
   }
   
   /**
    * Return the SrcMD5s info in a workable data structure.
    * A HashMap<filename,MD5>
    * @return the hashMap<filename,MD5>
    */
   public HashMap<String,String> getSrcMD5s() throws Exception
   {
       HashMap<String, String> hMap = new HashMap<String,String>();
       
       // get list of MD5 files
       String relevant_files[] = this.m_SrcMd5s.list(new FilenameFilter() {
           public boolean accept(File dir, String name)
           {
               return name.endsWith(".md5");
           }
       });
       
       File   md5F     = null;
       String key      = null;
       String val      = null;
       
       for (String filename : relevant_files)
       {
          md5F = new File(this.m_SrcMd5s.getAbsolutePath() + File.separator + filename);
          key  = FilenameUtils.getBaseName(filename);
          val  = FileUtils.readFileToString(md5F);
          
          hMap.put(key, val);
       }
       
       return hMap;
   }
   
   /**
    * Check if there is a Delta. If no Delta, this is an empty Release
    * @return true if there is a Delta, false otherwise
    */
   public boolean hasADelta()
   {
       // get list of MD5 files
       String relevant_files[] = this.m_Result.list(new FilenameFilter() {
           public boolean accept(File dir, String name)
           {
               // xml file or deleted
               return name.endsWith(".xml") || name.equals("deleted");
           }
       });
       
       return (relevant_files.length > 0) ? true : false;
   }
   
   
   public File getRootDir()
   {
       return this.m_ReleaseTopDir;
   }
   
   
   
   
}
