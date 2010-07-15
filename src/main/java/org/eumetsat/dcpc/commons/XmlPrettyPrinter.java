package org.eumetsat.dcpc.commons;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

/**
 * XMLPrettyPrinter
 * @author guillaume.aubert@eumetsat.int
 *
 */
public class XmlPrettyPrinter
{
    /**
     * Pretty Print the XML.
     * @param aFilePath
     * @return the XML document as a String
     * @throws Exception if the source file doesn't exist 
     *                   or if there is problem while doing the XML transformation
     */
    public static String prettyPrintAsString(String aFilePath) throws Exception
    {
        FileInputStream in = new FileInputStream(aFilePath);
        
        Document doc = XmlPrettyPrinter.loadXMLFrom(in);
            
        ByteArrayOutputStream out = new ByteArrayOutputStream();
            
        XmlPrettyPrinter.serialize(doc, out);
            
        return out.toString();
    }
    
    /**
     * Pretty Print the XML as an OutputStream
     * @param aFilePath
     * @param aOut
     * @return the passed OutputStream filled
     * @throws Exception if the source file doesn't exist 
     *                   or if there is problem while doing the XML transformation
     */
    public static OutputStream prettyPrint(String aFilePath, OutputStream aOut) throws Exception
    {
        FileInputStream in = new FileInputStream(aFilePath);
        
        Document doc = XmlPrettyPrinter.loadXMLFrom(in);
            
        XmlPrettyPrinter.serialize(doc, aOut);
            
        return aOut;
    }
    
    /**
     * Pretty Print the XML as an OutputStream
     * @param aIn InputStream
     * @param aOut OutputStream
     * @return the passed OutputStream filled
     * @throws Exception if the source file doesn't exist 
     *                   or if there is problem while doing the XML transformation
     */
    public static OutputStream prettyPrint(InputStream aIn, OutputStream aOut) throws Exception
    {
        Document doc = XmlPrettyPrinter.loadXMLFrom(aIn);
            
        XmlPrettyPrinter.serialize(doc, aOut);
            
        return aOut;
    }
    
    /**
     * internal method doing the xml transformation
     * @param doc
     * @param out
     * @throws Exception
     */
    private static void serialize(Document doc, OutputStream out) throws Exception
    {

        TransformerFactory tfactory = TransformerFactory.newInstance();
        Transformer serializer;
        try
        {
            serializer = tfactory.newTransformer();
            // Setup indenting to "pretty print"
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount", "2");

            serializer.transform(new DOMSource(doc), new StreamResult(out));
        }
        catch (TransformerException e)
        {
            // this is fatal, just dump the stack and throw a runtime exception
            e.printStackTrace();

            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unused")
    private static org.w3c.dom.Document loadXMLFrom(String xml) throws org.xml.sax.SAXException, java.io.IOException
    {
        return loadXMLFrom(new java.io.ByteArrayInputStream(xml.getBytes()));
    }

    private static org.w3c.dom.Document loadXMLFrom(java.io.InputStream is)
            throws org.xml.sax.SAXException, java.io.IOException
    {
        javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory
                .newInstance();
        factory.setNamespaceAware(true);
        javax.xml.parsers.DocumentBuilder builder = null;
        try
        {
            builder = factory.newDocumentBuilder();
        }
        catch (javax.xml.parsers.ParserConfigurationException ex)
        {
        }
        org.w3c.dom.Document doc = builder.parse(is);
        is.close();
        return doc;
    }
}
