package org.eumetsat.eoportal.dcpc.md.test;

import java.io.File;
import java.io.FileOutputStream;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.eumetsat.eoportal.dcpc.commons.DateUtil;
import org.eumetsat.eoportal.dcpc.commons.Obfuscator;
import org.eumetsat.eoportal.dcpc.md.export.XsltProcessor;
import org.eumetsat.eoportal.dcpc.md.fetcher.ProdNavFetcher;
import org.eumetsat.eoportal.dcpc.md.fetcher.ProdNavFetcherFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;

public class MetadataExporterUnitTestSuite extends TestCase
{
    public final static Logger logger = LoggerFactory.getLogger(MetadataExporterUnitTestSuite.class);
    
    public static String PROJ_DIR = null;        
    static
    {
        PROJ_DIR = System.getProperty("project.dir",".");
        System.out.println("Project dir = " + new File(PROJ_DIR).getAbsolutePath());
    }
    
    public final static String TEST_DIR = PROJ_DIR + "/src/test/resources";
    
    public void ztestXSLTTransformation()
    {
        String xsltFile           = PROJ_DIR + "/etc/xslt/eum2iso_v4.1.xsl";
        String file2Transform     = PROJ_DIR + "/etc/metadata/eo-portal-metadata/1.xml";
        String outputDir          = "/tmp";
        
        // do the transformations
        XsltProcessor xsltTransformer;
        try
        {
            xsltTransformer = new XsltProcessor(new File(xsltFile), new File(outputDir));
        
        
            // do xslt transformation
            xsltTransformer.processFile(new File(file2Transform), true);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void ztestVTD_Modif()
    {
        try 
        {
            // open a file and read the content into a byte array
            VTDGen vg = new VTDGen();
            if (vg.parseFile("/tmp/Z_EO_EUM_DAT_GOES_GWW_C_EUMS_20100512000000.xml", true)){
                VTDNav vn = vg.getNav();
                File fo = new File("/tmp/Z_EO_EUM_DAT_GOES_GWW_C_EUMS_20100512000000.xml");
                FileOutputStream fos = new FileOutputStream(fo);
                AutoPilot ap = new AutoPilot(vn);
                
                ap.declareXPathNameSpace("gmd", "http://www.isotc211.org/2005/gmd");
                ap.declareXPathNameSpace("gco", "http://www.isotc211.org/2005/gco");
                ap.declareXPathNameSpace("gmi", "http://www.isotc211.org/2005/gmi");
                ap.declareXPathNameSpace("gml", "http://www.opengis.net/gml");
                ap.declareXPathNameSpace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
                
                XMLModifier xm = new XMLModifier(vn);
                
                ap.selectXPath("/gmd:MD_Metadata/gmd:dateStamp/gco:Date/text()");
                
                int i = -1;
                while((i=ap.evalXPath())!=-1)
                {
                    xm.updateToken(i,DateUtil.dateToString(DateUtil.getUTCCurrentTime(), DateUtil.ms_ISODATEFORMAT));
                }
                xm.output(fos);
                fos.close();
            }
             }
             catch (Exception e){
                 System.out.println(" Exception "+e);
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
    }
    
    public void ztestObfuscation()
    {
        String in = "yffffffff";
        String out;
        out = Obfuscator.obfuscate(in.trim());
        
        out = Obfuscator.deobfuscate(out);
        
        assertEquals(in,out);
    }
    
    public void ztestProdNavWCSFetcher()
    {   try
        {
            ProdNavFetcher wcsFetcher = ProdNavFetcherFactory.getFetcher("H:/CSW-Results");
       
            wcsFetcher.fetch();
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void testCopyfiles()
    {   try
        {
           String mdName = "Z:EO:EUM:DAT:MFG:HRIADC_C_EUMS.xml";
           // replace all : in _ as : is not supported in windows as a character filename
           mdName = mdName.replaceAll(":", "_");
           
           File oldFile  = new File("H:" + File.separator + "tmp" + File.separator + "mdexport-run-10427344222846480652224464422156180" + File.separator + "temp-19593542864750925942224466615718561" + File.separator + "temp" + File.separator + "XML-Files" + File.separator + "metadata_1.xml");
           File newFile  = new File("H:\\tmp\\mdexport-run-10427344222846480652224464422156180\\temp-19593542864750925942224466615718561\\temp\\XML-Files\\" + mdName);
    
           System.out.println("oldFile = " + oldFile);
           System.out.println("newFile = " + newFile);
           FileUtils.moveFile(oldFile, newFile);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
