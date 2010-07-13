package org.eumetsat.dcpc.md.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Metadata Exporter
 * @author guillaume.aubert@eumetsat.int
 *
 */
public class MetadataExporter
{
    protected ReleaseDatabase m_ReleaseDB;
    
    
    public MetadataExporter(String aReleaseDBRootDirPath) throws IOException
    {
       m_ReleaseDB = new ReleaseDatabase(aReleaseDBRootDirPath);
    }
}
