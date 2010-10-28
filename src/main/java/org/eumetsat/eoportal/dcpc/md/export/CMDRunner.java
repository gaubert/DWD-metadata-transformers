package org.eumetsat.eoportal.dcpc.md.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import org.eumetsat.eoportal.dcpc.commons.FileSystem;
import org.eumetsat.eoportal.dcpc.md.fetcher.ProdNavFetcher;
import org.eumetsat.eoportal.dcpc.md.fetcher.ProdNavFetcherFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import static java.util.Arrays.asList;

/**
 * CommandLineRunner for the MetadataExporter
 * 
 * @author guillaume.aubert@eumetsat.int
 * 
 */
public class CMDRunner
{
    static final String            VERSION              = "v1.1";

    static boolean                 DEBUG_ON             = false;

    static final OptionParser      parser               = new OptionParser();

    public final static Logger     logger               = LoggerFactory
                                                                .getLogger(CMDRunner.class);

    static final String            LINE_SEP             = System
                                                                .getProperty("line.separator");

    protected static final boolean TEMP_DIR_DELETION_ON = true;

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
            writer
                    .write("Usage: md-exporter --out <output-dir> --rdb <release-database> [--in <input-dir>]");
            writer.newLine();
            writer
                    .write("                   [--xslt <xslt-file>] [--workdir <working-dir>]");
            writer.newLine();
            writer.write("                   [--version] [--help]");
            writer.newLine();
            writer.newLine();
            writer.flush();

            parser.printHelpOn(aOut);
            writer.newLine();
            writer
                    .write("Set the env variable MD_DEBUG to yes to activate the debugging info.");
            writer.newLine();
            writer.newLine();

            writer.write("Examples:");
            writer.newLine();
            writer
                    .write("1) Using the test dir as the source dir and creating the ReleaseDatabase in /tmp/RDB.");
            writer.newLine();
            writer
                    .write("$>./md-exporter --in ../test --out /tmp/res --rdb /tmp/RDB");
            writer.newLine();
            writer.write("Run it twice to see no changes the second time.");
            writer.newLine();
            writer.newLine();
            writer
                    .write("2) Download the data from the Product Navigator and create the Release");
            writer.newLine();
            writer.write("$>./md-exporter --out /tmp/res --rdb /tmp/RDB");
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
     * 
     * @param args
     * @return
     */
    public static Map<String, Object> parseArguments(String args[])
    {
        HashMap<String, Object> arguments = new HashMap<String, Object>();

        OptionSpec<Void> help = parser.acceptsAll(asList("h", "help"),
                "show usage description.");

        OptionSpec<Void> version = parser.acceptsAll(asList("v", "version"),
                "Display version number only.");

        OptionSpec<Void> noxsltT = parser
                .acceptsAll(
                        asList("n", "no-xslt-trans"),
                        "do not perform the xslt transformation. The input files must have already been transformed.");

        OptionSpec<Void> nocheck = parser.acceptsAll(asList("s", "no-check"),
                "do not check that files have been transformed with xslt.");

        OptionSpec<File> xslt = parser.acceptsAll(asList("x", "xslt"),
                "xslt file.").withRequiredArg().ofType(File.class).describedAs(
                "xslt file").defaultsTo(
                new File("$MDEXPORTER_HOME/xslt/eum2iso_v4.1.xsl"));

        OptionSpec<File> outdir = parser.acceptsAll(asList("o", "out"),
                "output directory.").withRequiredArg().ofType(File.class)
                .describedAs("output-dir");

        OptionSpec<File> indir = parser
                .acceptsAll(
                        asList("i", "in"),
                        "input dir with files to transform. No  -in forces the tool to download the data from the prod nav.")
                .withRequiredArg().ofType(File.class).describedAs("input-dir");

        OptionSpec<File> rdbdir = parser.acceptsAll(asList("r", "rdb"),
                "Release Database Top Directory").withRequiredArg().ofType(
                File.class).describedAs("release-dir");

        OptionSpec<File> workingdir = parser.acceptsAll(asList("w", "workdir"),
                "Working Directory").withRequiredArg().ofType(File.class)
                .describedAs("work-dir").defaultsTo(new File("/tmp"));

        try
        {
            // no args passed so print usage and quit in error
            if (args == null || args.length == 0)
            {
                usage(System.out);
                System.exit(1);
            }

            OptionSet options = parser.parse(args);

            if (options.has(version))
            {
                version(System.out);
                System.exit(0);
            }

            if (options.has(help))
            {
                usage(System.out);
                System.exit(0);
            }

            if (options.has(outdir))
            {
                arguments.put("out", options.valueOf(outdir));
            }
            else
            {
                throw new IllegalArgumentException(
                        "Error: Need more arguments. " + outdir
                                + " option is missing." + CMDRunner.LINE_SEP);
            }

            if (options.has(indir))
            {
                arguments.put("in", options.valueOf(indir));
                arguments.put("download", false);
            }
            else
            {
                // throw new
                // IllegalArgumentException("Error: Need more arguments. " +
                // indir + " option is missing." + CMDRunner.LINE_SEP);
                arguments.put("download", true);
            }

            if (options.has(rdbdir))
            {
                arguments.put("rdb", options.valueOf(rdbdir));
            }
            else
            {
                throw new IllegalArgumentException(
                        "Error: Need more arguments. " + rdbdir
                                + " option is missing." + CMDRunner.LINE_SEP);
            }

            // check if we need to do the sanity check
            if (options.has(nocheck))
            {
                arguments.put("nocheck", true);
            }
            else
            {
                arguments.put("nocheck", false);
            }

            // check if we need to do the xslt trans
            if (options.has(noxsltT))
            {
                arguments.put("noxslt", true);
            }
            else
            {
                arguments.put("noxslt", false);
                // optional arguments
                if (options.has(xslt))
                {
                    arguments.put("xslt", options.valueOf(xslt));
                }
                else
                {
                    // set it to the default value
                    // default val is $MDEXPORTER_HOME/xslt/eum2iso_v4.1.xsl
                    File xsltF = new File(getMDHome() + File.separatorChar
                            + "xslt" + File.separatorChar + "eum2iso_v4.1.xsl");
                    arguments.put("xslt", xsltF);
                    logger.info("The default Xslt file will be used ("
                            + xsltF.getAbsolutePath() + ").");

                }
            }

            if (options.has(workingdir))
            {
                arguments.put("workingdir", options.valueOf(workingdir));
            }
            else
            {
                // set it to the default value
                arguments.put("workingdir", new File("/tmp"));
            }

        }
        catch (OptionException expected)
        {
            // because you still must specify an argument if you give the option
            // on the command line
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

    public static void printArguments(Map<String, Object> aArgs,
            OutputStream aOut)
    {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(aOut));

        try
        {
            writer.write("Arguments :");
            writer.newLine();
            for (String key : aArgs.keySet())
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
     * 
     * @param aArguments
     * @throws IOException
     */
    public static int runWith(Map<String, Object> aArguments)
    {
        // create a temporary working dir for the run
        // it will be deleted once the run is finished
        File workingDir = null;
        int errCode = 2;
        try
        {
            File inDir;

            workingDir = FileSystem.createTempDirectory("mdexport-run-",
                    ((File) aArguments.get("workingdir")));

            MetadataExporter md_Exporter = new MetadataExporter(
                    ((File) aArguments.get("rdb")).getAbsolutePath(),
                    workingDir.getAbsolutePath());

            // check if we need to download the data from the portal first
            if (((Boolean) aArguments.get("download")).booleanValue())
            {
                ProdNavFetcher pNavFetcher = ProdNavFetcherFactory.getFetcher(workingDir.getAbsolutePath());
                inDir = pNavFetcher.fetch();
            }
            else
            {
                inDir = ((File) aArguments.get("in"));
            }

            if (!((Boolean) aArguments.get("noxslt")).booleanValue())
                md_Exporter.setXsltFile(((File) aArguments.get("xslt"))
                        .getAbsolutePath());

            md_Exporter.createExport(inDir.getAbsolutePath(),
                    ((File) aArguments.get("out")).getAbsolutePath(),
                    ((Boolean) aArguments.get("nocheck")).booleanValue());

            errCode = 0;

        }
        catch (Throwable e)
        {
            // System.out.println("Error: " + e.getMessage());
            // e.printStackTrace();
            CMDRunner.logger.error(e.getMessage());
            CMDRunner.logger
                    .error("Set the log levels to DEBUG to have more info.");
            CMDRunner.logger.debug("Stack Trace of the error", e);
            // leave on error
            errCode = 2;

        }
        finally
        {
            // delete the temporary directory in any case
            if (TEMP_DIR_DELETION_ON && (workingDir != null))
            {
                CMDRunner.logger
                        .info("------------ Cleaning Working Dir     ----------");
                FileSystem.deleteDirs(workingDir);
            }
        }

        return errCode;
    }

    public static void setDebugInfo()
    {
        if (System.getProperty("md.debug", "no").equalsIgnoreCase("yes"))
        {
            System.out.println("Debug activated");
            DEBUG_ON = true;
        }
    }

    public static String getMDHome()
    {
        String home;
        if (!(home = System.getProperty("md.home", "")).equals(""))
        {
            return home;
        }

        return null;
    }

    public static int main(String[] args, boolean aExitOnError)
    {
        setDebugInfo();

        Map<String, Object> arguments = parseArguments(args);

        if (DEBUG_ON)
            printArguments(arguments, System.out);

        int errCode = runWith(arguments);

        if (aExitOnError)
        {
            System.exit(errCode);
        }

        return errCode;
    }

    /**
     * run main program and exit on error by default
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        main(args, true);
    }
}
