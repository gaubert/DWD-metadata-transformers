package org.eumetsat.eoportal.dcpc.md.fetcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.eumetsat.eoportal.dcpc.commons.FileSystem;
import org.eumetsat.eoportal.dcpc.commons.conf.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.FastLongBuffer;
import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

public class ProdNavMDWCSFetcher
{
    public static Map<String, String> NAMESPACES = new HashMap<String, String>();

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
    
    private static String ms_MD_XPATH_EXPR = "//gmi:MI_Metadata";
    
    public ProdNavMDWCSFetcher(String aWorkingDirPath) throws IOException
    {
        FileSystem.createDirs(aWorkingDirPath);

        m_WorkingDir = new File(aWorkingDirPath);
    }
    
    private File get_data() throws Exception
    {  
        PostMethod post = new PostMethod("http://vnavigator.eumetsat.int:80/soapServices/CSWStartup");
        
        //String agent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/534.3 (KHTML, like Gecko) Chrome/6.0.472.63 Safari/534.3";
        
        // Request content will be retrieved directly
        // from the input stream
        RequestEntity entity = new FileRequestEntity(new File("H:/WCS/csw_getrecords.xml"), "text/xml; charset=ISO-8859-1");
        post.setRequestEntity(entity);
        // Get HTTP client
        HttpClient httpclient = new HttpClient();
        // Execute request
        try 
        {
            int result = httpclient.executeMethod(post);
            // Display status code
            System.out.println("Response status code: " + result);
            // Display response
            System.out.println("Response body: ");
            System.out.println(post.getResponseBodyAsString());
        } 
        finally 
        {
            // Release current connection to the connection pool once you are done
            post.releaseConnection();
        }
        
        return null;
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

        this.get_data();
        System.exit(1);
        
        
        File topTempDir = FileSystem.createTempDirectory("download-", this.m_WorkingDir);

        
        File f = new File("H:/WCS/res.txt");
        FileInputStream fis =  new FileInputStream(f);
        byte[] b = new byte[(int) f.length()];
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

       
         return null;
    }
}
