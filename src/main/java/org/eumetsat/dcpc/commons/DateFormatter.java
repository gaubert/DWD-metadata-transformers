package org.eumetsat.dcpc.commons;

import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DateFormatter
{
    public final static String           ms_DATEFORMAT    = "yyyy-MM-dd'T'HH'h'mm'm'ss's'";
                                                           
    private final static SimpleDateFormat ms_DateFormat    = new SimpleDateFormat(ms_DATEFORMAT);
    private final static FieldPosition    ms_FieldPosition = new FieldPosition(0);
    
    
    public static Date createDate(String aStringDate) throws ParseException
    {
        return ms_DateFormat.parse(aStringDate);
    }
    
    public static String dateToString(Date aDate)
    {
         return ms_DateFormat.format(aDate, new StringBuffer(),ms_FieldPosition).toString();
    }
}
