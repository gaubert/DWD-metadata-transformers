package org.eumetsat.dcpc.md.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;


import static java.util.Arrays.asList;


/**
 * CommandLineRunner for the MetadataExporter
 * @author guillaume.aubert@eumetsat.int
 *
 */
public class CMDRunner
{
    static final OptionParser parser = new OptionParser();
    
    static final String LINE_SEP = System.getProperty("line.separator");
    
    public static void usage(OutputStream aOut)
    {
        
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(aOut));
        
        // write usage
        try
        {
            writer.write("Usage: md-exporter --in <input-dir> --out <output-dir> --xslt <xslt-file>");
            writer.newLine();
            writer.newLine();
            writer.flush();
        
            parser.printHelpOn(aOut);
            writer.newLine();
            writer.flush();
            
        }
        catch (IOException ignored)
        {
            // eat it
        }
    }
    public static Map<String, Object> parseArguments(String args[])
    {
       HashMap<String, Object> arguments = new HashMap<String, Object>();   
       
       
       OptionSpec<File> xslt      = parser.acceptsAll( asList( "x", "xslt") )
                                          .withRequiredArg()
                                          .ofType( File.class )
                                          .describedAs("xslt file")
                                          .defaultsTo( new File("H:/xslt.file") );
       
       OptionSpec<File> outdir    = parser.acceptsAll( asList("o", "out"), "output directory")
                                          .withRequiredArg()
                                          .ofType( File.class ).describedAs("output-dir");
       
       OptionSpec<File> indir    = parser.acceptsAll( asList("i", "in"), "input dir with files to transform" )
                                         .withRequiredArg()
                                         .ofType( File.class ).describedAs("input-dir");
       
       OptionSpec<File> rdbdir   = parser.acceptsAll( asList("r", "rdb"), "Release Database Top Directory" )
                                         .withRequiredArg()
                                         .ofType( File.class ).describedAs("release-dir");
       
       try 
       {
           OptionSet options = parser.parse(args);
           
           if (options.has(xslt))
           {
               arguments.put("xslt", options.valueOf(xslt));
           }
           else
           {
               throw new IllegalArgumentException("Error: " + xslt + " option is missing." + CMDRunner.LINE_SEP);
           }
           
           if (options.has(outdir))
           {
               arguments.put("out", options.valueOf(outdir));
           }
           else
           {
               throw new IllegalArgumentException("Error: " + outdir + " option is missing." + CMDRunner.LINE_SEP);
           }
           
           
           if (options.has(indir))
           {
               arguments.put("in", options.valueOf(indir));
           }
           else
           {
               throw new IllegalArgumentException("Error: " + indir + " option is missing." + CMDRunner.LINE_SEP);
           }
           
           if (options.has(rdbdir))
           {
               arguments.put("rdb", options.valueOf(indir));
           }
           else
           {
               throw new IllegalArgumentException("Error: " + indir + " option is missing." + CMDRunner.LINE_SEP);
           }
       }
       catch ( OptionException expected ) 
       {
           // because you still must specify an argument if you give the option on the command line
           System.err.println(expected.getMessage());
           throw new IllegalArgumentException(expected);
       }
       catch (IllegalArgumentException iaE)
       {
           System.err.println(iaE.getMessage());
           CMDRunner.usage(System.err);
       }
       catch (Exception e)
       {
        
        e.printStackTrace();
       }
       
       return arguments;
    }
    
    
    public static void main(String[] args)
    {
        args = new String[] { "-x", "H:/tmp", "-o", "A:/b"};
        parseArguments(args);
    }
}
