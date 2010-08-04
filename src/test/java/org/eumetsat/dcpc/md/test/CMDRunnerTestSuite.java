package org.eumetsat.dcpc.md.test;

import org.eumetsat.eoportal.dcpc.md.export.CMDRunner;

import junit.framework.TestCase;

public class CMDRunnerTestSuite extends TestCase
{
    public void ztestMissingArguments()
    {
        String [] args = new String[] { "-x", "H:/tmp", "-o", "A:/b"};
        
        try
        {
           CMDRunner.parseArguments(args);
        }
        catch(IllegalArgumentException ex)
        {
            
        }
        catch(Exception e)
        {
            fail("Received" + e + " Should have an IllegalArgumentException");
        }
    }
    
    public void testWithDownload()
    {
        String [] args = new String[] { "-out", "H:/tmp", "-rdb" , "H:/tmp/RDB" };
        
        CMDRunner.main(args);
        
    }
    
    
}
