package org.eumetsat.eoportal.dcpc.md.fetcher;

import java.io.File;

public interface ProdNavFetcher
{
    /**
     * Return Ouput Directory.
     * @return
     * @throws Exception
     */
    public File fetch() throws Exception;
    
    public void setWorkingDir(String aWorkingDirPath) throws Exception;
}
