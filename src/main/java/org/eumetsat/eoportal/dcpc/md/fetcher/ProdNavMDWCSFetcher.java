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
import org.apache.commons.httpclient.methods.FileRequestEntity;
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

    public static int HTTP_OK    = 200;
    public static int BUFFERSIZE = 1024*1024*2;
    
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
    
    private static String ms_MD_MATCH_RECS    = "//csw:SearchResults@numberOfRecordsMatched";
    private static String ms_MD_RETURNED_RECS = "//csw:SearchResults@numberOfRecordsReturned";
    
    public ProdNavMDWCSFetcher() throws IOException
    {
        m_WorkingDir = null;
    }
    
    private File old_get_data(File aTempDir) throws Exception
    {  
        String url = Config.getAsString("ProdNavMDCSWFetcher", "url", "http://vnavigator.eumetsat.int:80/soapServices/CSWStartup");
        
        logger.info("Get Data from OGC CSW server (url=" + url + " )");
        
        PostMethod post = new PostMethod(url);
        
        //String agent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/534.3 (KHTML, like Gecko) Chrome/6.0.472.63 Safari/534.3";
        
        // Request content will be retrieved directly
        // from the input stream
        File csw_request = new File(Config.getAsString("ProdNavMDCSWFetcher", "csw_file", "../conf/csw_getrecords.xml"));
        
        RequestEntity entity = new FileRequestEntity(csw_request, "text/xml; charset=ISO-8859-1");
        
        post.setRequestEntity(entity);
        // Get HTTP client
        HttpClient httpclient = new HttpClient();
        // Execute request
        
  
        File outputFile = new File(aTempDir + File.separator + "post_response.xml");
        
        try 
        {
            int result = httpclient.executeMethod(post);
            // Display status code
            logger.debug("HTTP Response status code: " + result);
            
            if (result == HTTP_OK)
            {
                BufferedInputStream in = new BufferedInputStream(post.getResponseBodyAsStream(),BUFFERSIZE);
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
            }
            else
            {
                throw new Exception("Error when accessing WCS Product Navigator Interface (HTTP Response Code " + result + "). See logs for more info.");
            }
        } 
        finally 
        {
            // Release current connection to the connection pool once you are done
            post.releaseConnection();
        }
        
        return outputFile;
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
        String total    = xpathExtractor.evaluateAsString(aXmlFile);
        
        return new Pair<String,String>(returned, total);
    }
    
    private ArrayList<File> getData(File aTempDir) throws Exception
    {  
        // Request content will be retrieved directly
        // read the request file (It has got variables to be replaced)
        File   CSWFile       = new File(Config.getAsString("ProdNavMDCSWFetcher", "csw_file", "../conf/csw_getrecords.xml"));
        String CSWRequest    = FileUtils.readFileToString(CSWFile);
        RequestEntity entity = null;
        
        int    begin           = 1;
        int    end             = 50;
        int    totalReceived   = 0;
        int    max             = 5000;
        int    nbFiles         = 0;
        
        ArrayList<File> outputFiles = new ArrayList<File>();
        
        String url = Config.getAsString("ProdNavMDCSWFetcher", "url", "http://vnavigator.eumetsat.int:80/soapServices/CSWStartup");
        
        logger.info("Get Data from OGC CSW server (url=" + url + " )");
        
        PostMethod post = new PostMethod(url);
        
        // Get HTTP client
        HttpClient httpclient = new HttpClient();
        
        int result = -1;
        
        /*
         * TODO:
         *  - Send Request and get retrieved data in memory (limit the number of returned records to something resonable) maybe this can streamed ?
         *  - XPath matchedRecords and returnedRecords (update begin and end)
         *  - go to the records and save them in the file
         *  - iterate over the records 
         * 
         */
        
        while (totalReceived < max)
        {
        
            try 
            {
                // update begin and end to fetch the following metadata records.
                CSWRequest.replaceAll("$Begin", String.valueOf(begin));
                CSWRequest.replaceAll("$End"  , String.valueOf(end));
                
                entity = new StringRequestEntity(CSWRequest, "text/xml", "charset=ISO-8859-1");
                
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
                    
                    // increment nb_files
                    nbFiles++;
                    
                    // get record info
                    Pair<String,String> recInfo = getRecordInfo(outputFile);
                    
                    // update max and totalReceived
                    totalReceived += Integer.parseInt(recInfo.getKey());
                    max = Integer.parseInt(recInfo.getValue());
                    
                    // Add outputFile in the files to parse
                    outputFiles.add(outputFile);
                    
                    
                }
                else
                {
                    throw new Exception("Error when accessing WCS Product Navigator Interface (HTTP Response Code " + result + "). See logs for more info.");
                }
            } 
            finally 
            {
                // Release current connection to the connection pool once you are done
                post.releaseConnection();
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

        ArrayList<File> xmlRecordsFiles = this.get_data(topTempDir);
 
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
            aFile.delete();
        }
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
