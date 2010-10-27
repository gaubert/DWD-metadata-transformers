package org.eumetsat.eoportal.dcpc.md.fetcher;

import org.eumetsat.eoportal.dcpc.commons.conf.Config;


public class ProdNavFetcherFactory
{

    public static ProdNavFetcher getFetcher(String aWorkingDirPath) throws Exception
    {
        String className = Config.getAsString("ProdNavMDFetcher", "classname", "org.eumetsat.eoportal.dcpc.md.fetcher.ProdNavMDWCSFetcher");
         
        Class<?> storageClass = Class.forName(className);
                
        ProdNavFetcher fetcher = (ProdNavFetcher) storageClass.newInstance();
        
        fetcher.setWorkingDir(aWorkingDirPath);
        
        return fetcher;  
    }
}
