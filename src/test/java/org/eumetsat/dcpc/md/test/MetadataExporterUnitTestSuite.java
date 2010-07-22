package org.eumetsat.dcpc.md.test;

import java.io.File;
import java.io.FileOutputStream;

import junit.framework.TestCase;

import org.eumetsat.dcpc.commons.DateUtil;
import org.eumetsat.dcpc.md.export.XsltProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;

public class MetadataExporterUnitTestSuite extends TestCase
{
    public final static String TEST_DIR = "H:/Dev/ecli-workspace/DWD-metadata-transformers/src/test/resources";
    public final static Logger logger = LoggerFactory.getLogger(MetadataExporterUnitTestSuite.class);
    
    public void testXSLTTransformation()
    {
        String xsltFile           = "H:/Dev/ecli-workspace/DWD-metadata-transformers/ext/xslt/eum2isoapFull_v4.1.xsl";
        String file2Transform     = "H:/Dev/ecli-workspace/DWD-metadata-transformers/ext/metadata/eo-portal-metadata/1.xml";
        String outputDir          = "H:";
        
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
    
    public void testVTD_Modif()
    {
        try 
        {
            // open a file and read the content into a byte array
            VTDGen vg = new VTDGen();
            if (vg.parseFile("H:/Z_EO_EUM_DAT_GOES_GWW_C_EUMS_20100512000000.xml", true)){
                VTDNav vn = vg.getNav();
                File fo = new File("H:/Z_EO_EUM_DAT_GOES_GWW_C_EUMS_20100512000000.xml");
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
}
