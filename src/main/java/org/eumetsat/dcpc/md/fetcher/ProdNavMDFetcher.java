package org.eumetsat.dcpc.md.fetcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

public class ProdNavMDFetcher
{
    public final static String P1XPATH     = "//div[@id='contentLoginBox']/form";
    public final static String LFILESXPATH = "//div[@class='mainWrapper']/form[@id='EOExportForm']/div[@id='backups']/ul[@class='zip']/li/a";
    public final static String ORIGINALURL = "http://vnavigator.eumetsat.int/discovery/Start/Admin/Quick.do";

    public ProdNavMDFetcher()
    {
    }

    public void fetch(String aOutputPath, File aWorkingDirectory)
            throws Exception
    {
        HtmlSubmitInput button;
        HtmlForm form;
        HtmlPage page;
        SortedSet<String> beforeSet = new TreeSet<String>();
        SortedSet<String> afterSet = new TreeSet<String>();
        List<?> fileList;
        String url2download;

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

        // Get the first page
        page = webClient.getPage(ORIGINALURL);

        // get list of all divs
        List<?> oList = page.getByXPath(P1XPATH);

        if (oList.size() != 1)
        {
            throw new Exception("Error cannot find " + P1XPATH + " in "
                    + ORIGINALURL);
        }

        // Get the form that we are dealing with and within that form,
        // find the submit button and the field that we want to change.
        form = (HtmlForm) oList.get(0);

        button = form.getInputByName("submit");

        // set login and password
        form.getInputByName("user").setValueAttribute("aubert");
        form.getInputByName("password").setValueAttribute("garruk25");

        // Now submit the form by clicking the button and get back the admin
        // page
        page = form.getInputByName("submit").click();

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

        FileOutputStream fOut = new FileOutputStream(aOutputPath);

        IOUtils.copy(in, fOut);

        System.out.println("Copied in "
                + new File(aOutputPath).getAbsolutePath());

    }
}
