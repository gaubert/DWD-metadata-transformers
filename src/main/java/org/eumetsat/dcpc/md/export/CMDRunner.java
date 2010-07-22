package org.eumetsat.dcpc.md.export;

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
    
    public static void usage(OutputStream aOut) throws IOException
    {
        OutputStreamWriter writer = new OutputStreamWriter(aOut);
        //writer.p
        parser.printHelpOn(aOut);
    }
    public static Map<String, Object> parseArguments(String args[])
    {
       HashMap<String, Object> arguments = new HashMap<String, Object>();   
       
       
       OptionSpec<File> xslt      = parser.acceptsAll( asList( "x", "xslt"), "xslt file used for the transformation" )
                                          .withRequiredArg()
                                          .ofType( File.class )
                                          .defaultsTo( new File("H:/xslt.file") );
       
       OptionSpec<File> outdir    = parser.acceptsAll( asList("o", "out"), "output directory")
                                          .withRequiredArg()
                                          .ofType( File.class );
       
       OptionSpec<File> indir    = parser.acceptsAll( asList("i", "in"), "input dir with files to transform" )
                                         .withRequiredArg()
                                         .ofType( File.class );
       
       OptionSpec<File> rdbdir   = parser.acceptsAll( asList("r", "rdb"), "Release Database Top Directory" )
                                         .withRequiredArg()
                                         .ofType( File.class );
       
       try 
       {
           
           
           OptionSet options = parser.parse( "-x", "H:/tmp", "-o", "A:/b");
           
           if (options.has(xslt))
           {
               arguments.put("xslt", options.valueOf(xslt));
           }
           
           if (options.has(outdir))
           {
               arguments.put("out", options.valueOf(outdir));
           }
           
           if (options.has(indir))
           {
               arguments.put("in", options.valueOf(indir));
           }
           
           if (options.has(rdbdir))
           {
               arguments.put("rdb", options.valueOf(indir));
           }
           
           
           
       }
       catch ( OptionException expected ) {
           // because you still must specify an argument if you give the option on the command line
           expected.printStackTrace();
       }
       catch (IOException e)
       {
           // TODO Auto-generated catch block
           e.printStackTrace();
       }
       
       return arguments;
    }
    
    
    public static void main(String[] args)
    {
        parseArguments(args);
    }
}
