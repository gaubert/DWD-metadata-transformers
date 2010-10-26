package org.eumetsat.eoportal.dcpc.commons.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;

public class XMLInjector
{
    public static Map<String, String> NAMESPACES = new HashMap<String, String>();

    static
    {
        NAMESPACES.put("gmd", "http://www.isotc211.org/2005/gmd");
        NAMESPACES.put("gco", "http://www.isotc211.org/2005/gco");
        NAMESPACES.put("gmi", "http://www.isotc211.org/2005/gmi");
        NAMESPACES.put("gml", "http://www.opengis.net/gml");
        NAMESPACES.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    }

    public XMLInjector()
    {

    }

    public static void changeNodeNameAndContent(String aInputFilePath,
            String aOutputFilePath, String aXPathNodeExpr, String aNewNodeName,
            String aNewContentStr, Map<String, String> aNameSpaces)
            throws Exception
    {
        // open a file and read the content into a byte array
        VTDGen vg = new VTDGen();
        if (vg.parseFile(aInputFilePath, true))
        {
            VTDNav vn = vg.getNav();

            File fo = new File(aOutputFilePath);

            FileOutputStream fos = new FileOutputStream(fo);

            AutoPilot ap = new AutoPilot(vn);

            // add Namespaces
            for (String key : aNameSpaces.keySet())
            {
                ap.declareXPathNameSpace(key, aNameSpaces.get(key));
            }

            XMLModifier xm = new XMLModifier(vn);

            // change value
            ap.selectXPath(aXPathNodeExpr + "/text()");

            int i = -1;
            while ((i = ap.evalXPath()) != -1)
            {
                xm.updateToken(i, aNewContentStr);
            }

            // change Node Name
            ap.selectXPath(aXPathNodeExpr);

            i = -1;
            while ((i = ap.evalXPath()) != -1)
            {
                xm.updateElementName(aNewNodeName);
            }

            xm.output(fos);
            fos.close();
        }
        else
        {
            throw new Exception("Cannot read the XML file " + aInputFilePath);
        }
    }

    public static void injectStringIntoNode(String aInputFilePath,
            String aOutputFilePath, String aXPathExpr, String aValue,
            Map<String, String> aNameSpaces) throws Exception
    {
        // open a file and read the content into a byte array
        VTDGen vg = new VTDGen();
        if (vg.parseFile(aInputFilePath, true))
        {
            VTDNav vn = vg.getNav();

            File fo = new File(aOutputFilePath);

            FileOutputStream fos = new FileOutputStream(fo);

            AutoPilot ap = new AutoPilot(vn);

            // add Namespaces
            for (String key : aNameSpaces.keySet())
            {
                ap.declareXPathNameSpace(key, aNameSpaces.get(key));
            }

            XMLModifier xm = new XMLModifier(vn);

            // change value
            ap.selectXPath(aXPathExpr);

            int i = -1;
            while ((i = ap.evalXPath()) != -1)
            {
                xm.updateToken(i, aValue);
            }

            xm.output(fos);
            fos.close();
        }
        else
        {
            throw new Exception("Cannot read the XML file " + aInputFilePath);
        }

    }
}
