package org.eumetsat.eoportal.dcpc.md.fetcher;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.FileUtils;
import org.eumetsat.eoportal.dcpc.commons.FileSystem;
import org.eumetsat.eoportal.dcpc.commons.Pair;
import org.eumetsat.eoportal.dcpc.commons.conf.Config;
import org.eumetsat.eoportal.dcpc.commons.xml.XPathExtractor;

import org.eumetsat.eoportal.dcpc.md.export.MetadataFileRenamer; // for Namespace to be moved

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;



public class ProdNavMDWCSFetcher implements ProdNavFetcher
{
    public static Map<String, String> NAMESPACES = new HashMap<String, String>();

    public static int HTTP_OK        = 200;
    public static int BUFFERSIZE     = 1024*1024*2;
    public static int REC_BATCH_SIZE = 70; // retrieve records by batch of X
    
    static
    {
        NAMESPACES.put("gmd", "http://www.isotc211.org/2005/gmd");
        NAMESPACES.put("gco", "http://www.isotc211.org/2005/gco");
        NAMESPACES.put("gmi", "http://www.isotc211.org/2005/gmi");
        NAMESPACES.put("gml", "http://www.opengis.net/gml");
        NAMESPACES.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        NAMESPACES.put("csw", "http://www.opengis.net/cat/csw/2.0.2");
    }

    public final static Logger logger = LoggerFactory.getLogger(ProdNavMDWCSFetcher.class);
    
    private File  m_WorkingDir;
    
    private static String ms_MD_XPATH_EXPR    = "//gmi:MI_Metadata";
    
    // to complete xpath expression //csw:GetRecordsResponse/SearchResults@numberOfRecordsMatched
    private static String ms_MD_MATCH_RECS    = "//@numberOfRecordsMatched";
    private static String ms_MD_RETURNED_RECS = "//@numberOfRecordsReturned";
    
    public ProdNavMDWCSFetcher() throws IOException
    {
        m_WorkingDir = null;
    }
    
    /**
     * Return info from the CSW Request
     * @param aXmlFile
     * @return Pair(nbRecordsReturned, TotalNumberOfRecordsMatched)
     * @throws Exception
     */
    private Pair<String, String> getRecordInfo(File aXmlFile) throws Exception
    {
        XPathExtractor xpathExtractor = new XPathExtractor();

        xpathExtractor.setXPathExpression(ms_MD_RETURNED_RECS, MetadataFileRenamer.ms_NamespaceContext);
        
        String returned = xpathExtractor.evaluateAsString(aXmlFile);
        
        xpathExtractor.setXPathExpression(ms_MD_MATCH_RECS   , MetadataFileRenamer.ms_NamespaceContext);
        
        String total    = xpathExtractor.evaluateAsString(aXmlFile);
        
        logger.debug("total:" + total + " returned:" + returned);
        
        return new Pair<String,String>(returned, total);
    }
    
    private ArrayList<File> getData(File aTempDir) throws Exception
    {  
        // Request content will be retrieved directly
        // read the request file (It has got variables to be replaced)
        File   CSWFile           = new File(Config.getAsString("ProdNavMDCSWFetcher", "csw_file", "../conf/csw_getrecords.xml"));
        String origCSWReq        = FileUtils.readFileToString(CSWFile);
        String modCSWReq         = null;
        RequestEntity entity = null;
        
        int    begin           = 0;
        int    end             = REC_BATCH_SIZE;
        int    totalReceived   = 0;
        int    max             = 5000; // max theorical at the moment
        
        int    nbFiles         = 0; // to name the different files
        
        ArrayList<File> outputFiles = new ArrayList<File>();
        
        String url = Config.getAsString("ProdNavMDCSWFetcher", "url", "http://vnavigator.eumetsat.int:80/soapServices/CSWStartup");
        
        logger.info("Get Data from OGC CSW server (url=" + url + " )");
        
        // Get HTTP client
        HttpClient httpclient = new HttpClient();
        
        int result = -1;
        
        /*
         *  - Send Request and get retrieved data in memory (limit the number of returned records to something resonable) maybe this can streamed ?
         *  - XPath matchedRecords and returnedRecords (update begin and end)
         *  - go to the records and save them in the file
         *  - iterate over the records 
         * 
         */
        
        while (totalReceived < max)
        {
                // update begin and end to fetch the following metadata records.
                modCSWReq = origCSWReq.replaceAll("\\$Begin", String.valueOf(begin));
                modCSWReq = modCSWReq.replaceAll("\\$End"  , String.valueOf(REC_BATCH_SIZE)); // need to take the modCSWReq otherwise we loose the begin
               
                logger.info("CSWREQUEST: " + modCSWReq);
                
                entity = new StringRequestEntity(modCSWReq, "text/xml;charset=ISO-8859-1", null);
                
                PostMethod post = new PostMethod(url);
                
                // add the request
                post.setRequestEntity(entity);
                 
                result = httpclient.executeMethod(post);
                // Display status code
                logger.debug("HTTP Response status code: " + result);
                
                if (result == HTTP_OK)
                {
                    // Execute request
                    File outputFile = new File(aTempDir + File.separator + "post_response_file_" + String.valueOf(nbFiles) + ".xml");
                    
                    BufferedInputStream in = new BufferedInputStream(post.getResponseBodyAsStream(), BUFFERSIZE);
                    
                    FileOutputStream out = new FileOutputStream(outputFile);
                    
                    // 2 MegaBytes buffer used
                    byte [] buffer = new byte[BUFFERSIZE];
                    int bytesRead = 0;
                    while ( (bytesRead = in.read(buffer, 0, BUFFERSIZE))!= -1 )
                    {
                        out.write(buffer, 0, bytesRead);
                    }
                    
                    in.close();
                    out.close();
                                        
                    // get record info
                    Pair<String, String> recInfo = getRecordInfo(outputFile);
                    
                    // update max which is the number of files to retrieve
                    // and totalReceived which is the number received so far
                    int iReturned = Integer.parseInt(recInfo.getKey());
                    int iMax      = Integer.parseInt(recInfo.getValue());
                    
                    logger.info("iReturned = " + iReturned);
                    
                    totalReceived += iReturned;
                    max            = iMax;
                    
                    begin += iReturned;
                    end    = begin + REC_BATCH_SIZE;
                    
                    // increment file
                    nbFiles++;
                    
                    logger.info("So far read " + totalReceived + " over " + max);
                    
                    logger.info("begin=" + begin + " end=" +end);
                    
                    // Add outputFile in the files to parse
                    outputFiles.add(outputFile);           
                    
                }
                else
                {
                    throw new Exception("Error when accessing WCS Product Navigator Interface (HTTP Response Code " + result + "). See logs for more info.");
                }
        }
        
        return outputFiles;
    }
    
    /**
     * Return Ouput Directory.
     * @return
     * @throws Exception
     */
    public File fetch() throws Exception
    {
        logger.info("------------ Export Data from ProdNav using WCS ----------");
        logger.info("This could take few minutes.");

        File topTempDir = FileSystem.createTempDirectory("download-", this.m_WorkingDir);

        ArrayList<File> xmlRecordsFiles = this.getData(topTempDir);
        
        logger.info("All files " + xmlRecordsFiles);
 
        for (File aFile : xmlRecordsFiles)
        {
            FileInputStream fis =  new FileInputStream(aFile);
            byte[] b = new byte[(int) aFile.length()];
            fis.read(b);
    
            // output file containing fragments
            FileOutputStream fos = null;
            int count = 0;
            
            // instantiate the parser
            VTDGen vg = new VTDGen();
            vg.setDoc(b);
            vg.parse(true);  // set namespace awareness to true 
            VTDNav vn = vg.getNav();
            AutoPilot ap = new AutoPilot(vn);
            
            // add Namespaces
            for (String key : NAMESPACES.keySet())
            {
                ap.declareXPathNameSpace(key, NAMESPACES.get(key));
            }
            
            // get to the SOAP header
            ap.selectXPath(ms_MD_XPATH_EXPR);
            logger.debug("expr string is " + ap.getExprString());
            
            while(ap.evalXPath()!= -1)
            {
                fos = new FileOutputStream(new File(topTempDir + File.separator + "metadata_" + count +".xml"));
                long l = vn.getElementFragment();
                int len = (int) (l>>32);
                int offset = (int) l;
                fos.write(b, offset, len); //write the fragment out into out.txt
                count++;
            }
    
            fis.close();
            fos.close();
            
            //remove post response
            //aFile.delete();
        }
        
        System.exit(1);
        
        // return inDir
        return topTempDir;
    }
    
    @Override
    public void setWorkingDir(String aWorkingDirPath) throws Exception
    {
        FileSystem.createDirs(aWorkingDirPath);

        m_WorkingDir = new File(aWorkingDirPath);
    }
}
