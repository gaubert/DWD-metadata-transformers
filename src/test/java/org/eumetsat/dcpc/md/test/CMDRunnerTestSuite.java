package org.eumetsat.dcpc.md.test;

import org.eumetsat.dcpc.md.export.CMDRunner;

import junit.framework.TestCase;

public class CMDRunnerTestSuite extends TestCase
{
    public void testMissingArguments()
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
}
