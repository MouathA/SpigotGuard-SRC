package xyz.yooniks.spigotguard.helper;

import java.text.*;
import java.util.*;

public final class DateHelper
{
    private static final DateFormat SIMPLE_DATE_FORMAT;
    
    static {
        SIMPLE_DATE_FORMAT = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
    }
    
    public static String getDate(final Date date) {
        return DateHelper.SIMPLE_DATE_FORMAT.format(date);
    }
    
    private DateHelper() {
    }
}
