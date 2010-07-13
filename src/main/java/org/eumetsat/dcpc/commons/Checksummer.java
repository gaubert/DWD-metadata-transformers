package org.eumetsat.dcpc.commons;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Formatter;

/**
 * Create a different kind of checksums provided a file
 * @author guillaume.aubert@eumetsat.int
 *
 */
public class Checksummer
{
    private static MessageDigest MD5 = null;

    /**
     * perform a MD5 checksum of the given input stream
     * @param aIn the input stream to hash
     * @return the MD5 checksum as a java String
     * @throws Exception
     */
    public static String doMD5Checksum(InputStream aIn) throws Exception
    {
        if (MD5 == null)
        {
           MD5 = MessageDigest.getInstance("MD5");
        }
        
        // reset the MD5 message digest
        MD5.reset();
        
        BufferedInputStream bis = new BufferedInputStream(aIn);
        DigestInputStream   dis = new DigestInputStream(bis, MD5);

        // read the file and update the hash calculation
        while (dis.read() != -1);

        // get the hash value as byte array
        byte[] hash = MD5.digest();

        return byteArray2Hex(hash); 
    }

    private static String byteArray2Hex(byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

}
