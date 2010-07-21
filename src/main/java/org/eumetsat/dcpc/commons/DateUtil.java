package org.eumetsat.dcpc.commons;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;


public class DateUtil
{
    public final static String            ms_RELEASEDATEFORMAT     = "yyyy-MM-dd'T'HH'h'mm'm'ss's'";   
    public final static String            ms_DELETEDATEFORMAT      = "yyyyMMddHHmmss";
    public final static String            ms_MDDATEFORMAT          = "yyyy-MM-dd"; 
    public final static String            ms_ISODATEFORMAT         = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    
    private final static FieldPosition    ms_FieldPosition         = new FieldPosition(0);
    
    private final static HashMap<String,SimpleDateFormat> ms_Formats = new HashMap<String,SimpleDateFormat>();
    
    static
    {
        ms_Formats.put(ms_RELEASEDATEFORMAT, new SimpleDateFormat(ms_RELEASEDATEFORMAT) );
        ms_Formats.put(ms_DELETEDATEFORMAT, new SimpleDateFormat(ms_DELETEDATEFORMAT) );
        ms_Formats.put(ms_MDDATEFORMAT, new SimpleDateFormat(ms_MDDATEFORMAT) );
        ms_Formats.put(ms_ISODATEFORMAT, new SimpleDateFormat(ms_ISODATEFORMAT) );  
    }
    
    public static Date createDate(String aStringDate, String aFormat) throws Exception
    {
        SimpleDateFormat sdF = ms_Formats.get(aFormat);
        
        if (sdF == null)
            throw new Exception("Format " + aFormat + " is an unknown format");
        
        return sdF.parse(aStringDate);
    }
    
    public static String dateToString(Date aDate, String aFormat) throws Exception
    {
         SimpleDateFormat sdF = ms_Formats.get(aFormat);
        
         if (sdF == null)
            throw new Exception("Format " + aFormat + " is an unknown format");
        
         return sdF.format(aDate, new StringBuffer(),ms_FieldPosition).toString();
    }
    
    public static Date getUTCCurrentTime()
    {
        DateTime dt = new DateTime(DateTimeZone.UTC);
                     
        return dt.toDate();
    }
}
