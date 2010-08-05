package org.eumetsat.dcpc.md.test;

import org.eumetsat.eoportal.dcpc.md.export.CMDRunner;

import junit.framework.TestCase;

public class CMDRunnerTestSuite extends TestCase
{
    public void testMissingArguments()
    {
        String [] args = new String[] { "-x", "/tmp", "-o", "/b/c"};
        
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
        String [] args = new String[] { "-out", "/tmp", "-rdb" , "/tmp/RDB" };
        
        CMDRunner.main(args);
        
    }
    
    
}
