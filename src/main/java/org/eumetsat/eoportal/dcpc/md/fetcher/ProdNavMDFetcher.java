package org.eumetsat.eoportal.dcpc.md.fetcher;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.eumetsat.eoportal.dcpc.commons.FileSystem;
import org.eumetsat.eoportal.dcpc.commons.Obfuscator;
import org.eumetsat.eoportal.dcpc.commons.Pair;
import org.eumetsat.eoportal.dcpc.commons.Unzipper;
import org.eumetsat.eoportal.dcpc.md.export.CMDRunner;
import org.eumetsat.eoportal.dcpc.commons.conf.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

public class ProdNavMDFetcher
{
    public final static String P1XPATH     = "//div[@id='contentLoginBox']/form";
    public final static String LFILESXPATH = "//div[@class='mainWrapper']/form[@id='EOExportForm']/div[@id='backups']/ul[@class='zip']/li/a";
    private File       m_WorkingDir;
    private boolean    m_Obfuscated;
    
    
    public final static Logger     logger               = LoggerFactory.getLogger(CMDRunner.class);

    public ProdNavMDFetcher(String aWorkingDirPath) throws IOException
    {
        FileSystem.createDirs(aWorkingDirPath);

        m_WorkingDir = new File(aWorkingDirPath);
        
        m_Obfuscated = Config.booleanAt("ProdNavMDFetcher","obfuscated",true);
    }
    
    private Pair<String, String> getCredentials() throws Exception
    {
        String log;
        String pass;
        
        if ( (log  = Config.at("ProdNavMDFetcher","login", null)) == null)
            throw new Exception("Cannot find in the configuration file a login key in the group [ProdNavMDFetcher]");
        
        if ( (pass = Config.at("ProdNavMDFetcher","password", null)) == null)
            throw new Exception("Cannot find in the configuration file a password key in the group [ProdNavMDFetcher]");
       
        if (this.m_Obfuscated)
        {
            // decode password
            pass = Obfuscator.deobfuscate(pass);
        }
        
        return new Pair<String, String>(log, pass);
    }

    /**
     * Fetch an export from the product navigator. 
     * @return OutputDir where the retrieved zip file has been cracked.
     * @throws Exception
     */
    public File fetch() throws Exception
    {              
        logger.info("------------ Export Data from ProdNav ----------");
        logger.info("This could take few minutes.");
        
        File topTempDir = FileSystem.createTempDirectory("download-",this.m_WorkingDir);
        
        HtmlSubmitInput button;
        HtmlForm form;
        HtmlPage page;
        HtmlInput hInput;
        SortedSet<String> beforeSet = new TreeSet<String>();
        SortedSet<String> afterSet = new TreeSet<String>();
        List<?> fileList;
        String url2download;
        String url;
        
        

        final WebClient webClient = new WebClient();

        // Configuring the webClient
        webClient.setJavaScriptEnabled(true);
        webClient.setThrowExceptionOnScriptError(false);
        webClient.setCssEnabled(true);
        webClient.setUseInsecureSSL(true);
        webClient.setRedirectEnabled(true);
        webClient.setActiveXNative(false);
        webClient.setAppletEnabled(false);
        webClient.setPrintContentOnFailingStatusCode(true);
        
        if ( (url = Config.at("ProdNavMDFetcher", "url", null)) == null)
            throw new Exception("Cannot find in the configuration file a login key in the group [ProdNavMDFetcher]");
       
        logger.debug("Starting URL {}." + url);
        
        // Get the first page
        page = webClient.getPage(url);

        // get list of all divs
        List<?> oList = page.getByXPath(P1XPATH);

        if (oList.size() != 1)
        {
            throw new Exception("Error cannot find " + P1XPATH + " in "
                    + url);
        }

        // Get the form that we are dealing with and within that form,
        // find the submit button and the field that we want to change.
        form = (HtmlForm) oList.get(0);

        button = form.getInputByName("submit");
        
        Pair<String, String> cred = this.getCredentials();

        // set login and password
        // need to be verbose otherwise non supported by java 1.5 (deferencement issue)
        hInput = form.getInputByName("user");
        hInput.setValueAttribute(cred.getKey());
        hInput = form.getInputByName("password");
        hInput.setValueAttribute(cred.getValue());

        // Now submit the form by clicking the button and get back the admin
        // page
        hInput = form.getInputByName("submit");
        page = hInput.click();

        // check that it is ok
        // System.out.println("Export Page " + page.asText());

        // click on the export page
        page = webClient
                .getPage("http://vnavigator.eumetsat.int/discovery/Admin/export/init.do");

        // Get the list of existing files before to click on the export button
        fileList = page.getByXPath(LFILESXPATH);

        // beforeSet will contain the list of existing files before export
        for (Object object : fileList)
        {
            if (((HtmlAnchor) object).getHrefAttribute().contains("BACKUP_TC"))
            {
                beforeSet.add(((HtmlAnchor) object).getHrefAttribute());
            }
        }

        // get button from the form
        oList = page
                .getByXPath("//div[@class='mainWrapper']/form[@id='EOExportForm']/input[@type='submit' and @class='button']");

        button = (HtmlSubmitInput) oList.get(0);

        // click to trigger the export
        page = button.click();

        // check that this is fine

        // wait 5 sec while the file is being generated
        Thread.sleep(5000);

        // go back to the metadata export page to get the file
        page = webClient
                .getPage("http://vnavigator.eumetsat.int/discovery/Admin/export/init.do");

        fileList = page
                .getByXPath("//div[@class='mainWrapper']/form[@id='EOExportForm']/div[@id='backups']/ul[@class='zip']/li/a");

        for (Object object : fileList)
        {
            if (((HtmlAnchor) object).getHrefAttribute().contains("BACKUP_TC"))
            {
                afterSet.add(((HtmlAnchor) object).getHrefAttribute());
            }
        }

        afterSet.removeAll(beforeSet);

        // afterSet should now contain only one file
        if (afterSet.size() == 0)
        {
            throw new Exception(
                    "Error there are no elements in the AfterSet. Elements in the beforeSet = "
                            + beforeSet);
        }

        url2download = afterSet.last();

        Page dataPage = webClient.getPage(url2download);

        InputStream in = dataPage.getWebResponse().getContentAsStream();

        File destination = new File(topTempDir + File.separator + "downloaded_data.zip");
        FileOutputStream fOut = new FileOutputStream(destination);

        IOUtils.copy(in, fOut);

        logger.info("Retrieved {} from the Product Navigator.", destination.getName());
        logger.debug("Full Path {}." + destination.getAbsolutePath());
        
        //unzip it there
        return Unzipper.unzip(destination.getAbsolutePath());

    }
}
