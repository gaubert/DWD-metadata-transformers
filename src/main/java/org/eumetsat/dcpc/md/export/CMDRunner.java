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
    static final String VERSION ="v0.8";
    
    static boolean DEBUG_ON = false;
    
    static final OptionParser parser = new OptionParser();
    
    static final String LINE_SEP = System.getProperty("line.separator");
    
    public static void version(OutputStream aOut)
    {
       BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(aOut));
       
       try
       {
           
            writer.write("version: " + VERSION);
            writer.newLine();
            writer.flush();
        
        }
        catch (IOException ignored)
        {
            // eat it
        }
    }
    
    public static void usage(OutputStream aOut)
    {
        
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(aOut));
        
        // write usage
        try
        {
            writer.write("Usage: md-exporter [--version] [--help] --in <input-dir> --out <output-dir> --xslt <xslt-file> ");
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
    
    /**
     * Parse the command line
     * @param args
     * @return
     */
    public static Map<String, Object> parseArguments(String args[])
    {
       HashMap<String, Object> arguments = new HashMap<String, Object>();   
       
       OptionSpec<Void> help      = parser.acceptsAll( asList( "h", "help"), "show usage description" );
       
       OptionSpec<Void> version   = parser.acceptsAll( asList( "v", "version"), "application version" );
              
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
           
           if (options.has(version))
           {
               version(System.out);
           }
           
           if (options.has(help))
           {
               usage(System.out);
               System.exit(0);
           }
           
           if (options.has(xslt))
           {
               arguments.put("xslt", options.valueOf(xslt));
           }
           else
           {
               throw new IllegalArgumentException("Error: Need more arguments. " + xslt + " option is missing." + CMDRunner.LINE_SEP);
           }
           
           if (options.has(outdir))
           {
               arguments.put("out", options.valueOf(outdir));
           }
           else
           {
               throw new IllegalArgumentException("Error: Need more arguments. " + outdir + " option is missing." + CMDRunner.LINE_SEP);
           }
           
           
           if (options.has(indir))
           {
               arguments.put("in", options.valueOf(indir));
           }
           else
           {
               throw new IllegalArgumentException("Error: Need more arguments. " + indir + " option is missing." + CMDRunner.LINE_SEP);
           }
           
           if (options.has(rdbdir))
           {
               arguments.put("rdb", options.valueOf(rdbdir));
           }
           else
           {
               throw new IllegalArgumentException("Error: Need more arguments. " + indir + " option is missing." + CMDRunner.LINE_SEP);
           }
       }
       catch ( OptionException expected ) 
       {
           // because you still must specify an argument if you give the option on the command line
           System.err.println(expected.getMessage());
           System.exit(1);
       }
       catch (IllegalArgumentException iaE)
       {
           System.err.println(iaE.getMessage());
           CMDRunner.usage(System.err);
           System.exit(1);
       }
       catch (Exception unknwown)
       {
          unknwown.printStackTrace();
          System.exit(1);
       }
       
       return arguments;
    }
    
    public static void printArguments(Map<String, Object> aArgs, OutputStream aOut)
    {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(aOut));
        
        try
        {
            writer.write("Arguments :");
            writer.newLine();
            for (String  key : aArgs.keySet())
            {
                writer.write(key + " :[" + aArgs.get(key) + "]");
                writer.newLine();
                writer.flush();
            }
            writer.write("-------------------------");
            writer.newLine();
            writer.flush();
         }
         catch (IOException ignored)
         {
             // eat it
         }
         
    }
    
    /**
     * Run the program
     * @param aArguments
     */
    public static void runWith(Map<String, Object> aArguments)
    {
        try
        {
            MetadataExporter md_Exporter = new MetadataExporter( ((File) aArguments.get("xslt")).getAbsolutePath()
                                                               , ((File) aArguments.get("rdb")).getAbsolutePath()
                                                               , new File("/tmp").getAbsolutePath());
            
            md_Exporter.createExport( ((File) aArguments.get("in")).getAbsolutePath(), 
                                      ((File) aArguments.get("out")).getAbsolutePath() );
        }
        catch (Exception e)
        {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
            
        }
        
        
    }
    
    public static void setDebugInfo()
    {
        System.out.println("MD DEBUG = " + System.getProperty("md.debug") );
        
        if (System.getProperty("md.debug", "no").equalsIgnoreCase("yes"))
        {
            System.out.println("Debug activated");
            DEBUG_ON = true;
        }
    }
    
    
    public static void main(String[] args)
    {
        setDebugInfo();
        
        Map<String, Object> arguments = parseArguments(args);
        
        if (DEBUG_ON)
            printArguments(arguments, System.out);
        
        runWith(arguments);  
    }
}
