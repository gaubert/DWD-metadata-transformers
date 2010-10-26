package org.eumetsat.eoportal.dcpc.commons;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.codec.binary.Base64;

public class Obfuscator
{

    /**
     * Really rudimentary obfuscation that does a double Base64 encoding
     * 
     * @param aIn
     * @return the Obfuscated String
     */
    public static String obfuscate(String aIn)
    {
        byte[] result = Base64.encodeBase64(aIn.getBytes());

        // trim string because for some reasons Base64 adds CRLF
        return new String(Base64.encodeBase64String(result)).trim();

    }

    public static String deobfuscate(String aIn)
    {
        byte[] res = Base64.decodeBase64(aIn);

        return new String(Base64.decodeBase64(res));
    }

    public static void usage(OutputStream aOut) throws IOException
    {
        aOut.write(new String("Usage: obfuscate [-h] string\n\n").getBytes());
        aOut
                .write(new String(
                        "Obfuscate a string (as required for the Product Navigator password).\n\n")
                        .getBytes());
        aOut.write(new String("Options:\n").getBytes());
        aOut.write(new String("--------\n").getBytes());
        aOut.write(new String("-h     : print usage.\n").getBytes());
    }

    public static void main(String[] args)
    {
        try
        {
            if (args == null || args.length == 0)
            {
                usage(System.out);

                System.exit(1);
            }

            String param = args[0];

            if (param.equalsIgnoreCase("-h") || param.equalsIgnoreCase("?"))
            {
                usage(System.out);
                System.exit(0);
            }

            System.out.println("Result in square brackets : ["
                    + Obfuscator.obfuscate(args[0]) + "]");
            System.exit(0);

        }
        catch (Throwable th)
        {
            System.err.println("Unexcepted Error" + th.getMessage());
            th.printStackTrace(System.err);
            System.exit(2);
        }
    }
}
