package org.eumetsat.dcpc.commons.xml;

import java.io.File;
import java.io.IOException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XPathExtractor
{
    private static final SimpleNamespaceContext ms_NamespaceContext = new SimpleNamespaceContext();
    private XPathExpression _XPathExpr;
    
    private DocumentBuilderFactory _domFactory;
    
    public XPathExtractor()
    {
        this._domFactory = DocumentBuilderFactory.newInstance();
        this._domFactory.setNamespaceAware(true); 
    }
    
    public SimpleNamespaceContext getNewNamespaceContext()
    {
        return new SimpleNamespaceContext();
    }
    
    public void setXPathExpression(String aXPathExpr, NamespaceContext aNSContext) throws XPathExpressionException
    {
        XPath xpath = XPathFactory.newInstance().newXPath();
        
        xpath.setNamespaceContext(aNSContext);
        
        // XPath Query for showing all nodes value
        this._XPathExpr = xpath.compile(aXPathExpr);
    }
    
    /**
     * Evaluate Expression on the given XML file
     * @param aFile
     * @return the result as a String
     * @throws Exception
     */
    public String evaluateAsString(File aXmlFile) throws Exception
    {
        DocumentBuilder builder = _domFactory.newDocumentBuilder();
        
        Document doc = builder.parse(aXmlFile);
        
        String result = (String) _XPathExpr.evaluate(doc, XPathConstants.STRING);
        
        return result;
    }
}
