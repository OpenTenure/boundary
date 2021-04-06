package org.sola.opentenure.services.boundary.beans.helpers;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.sola.common.DateUtility;

/**
 * Holds methods and fields to support dates operations
 */
@Named
@ApplicationScoped
public class DateBean {
    private final String dateFormat = "dd/MM/yy";
    private final String dateFormatForDisplay = "dd/MM/yyyy";
    private final String timeFormat = "HH:mm";
    
    public DateBean(){
    }
    
    /** Returns date pattern for short format. */
    public String getDatePattern(){
        return dateFormat;
    }

    /** Returns date pattern for displaying dates. */
    public String getDateFormatForDisplay() {
        return dateFormatForDisplay;
    }
    
    /** Returns short date and time string in short format */
    public String getShortDate(Date date){
        return DateUtility.formatDate(date, dateFormatForDisplay);
    }
    
    /** Localized short date */
    public String getShortLocalizedDate(Date date){
        return DateUtility.formatDate(date, dateFormatForDisplay);
    }
    
    /** Returns current date and time in short format. */
    public String getShortDateAndTime(){
        return DateUtility.formatDate(Calendar.getInstance().getTime(), dateFormatForDisplay + " " + timeFormat);
    }
    
    /** Returns date and time string in short format */
    public String getDateTime(Date date){
        return DateUtility.getDateTimeString(date, DateFormat.SHORT, DateFormat.SHORT);
    }
    
    /** Returns date and time string in short format */
    public String getTime(Date date){
        if (date == null) {
            return "";
        }
        DateFormat f = DateFormat.getTimeInstance(DateFormat.SHORT);
        return f.format(date);
    }
    
    /** Formats date using predefined date format. Formated date includes time */
    public String formatDate(Date date){
        return DateUtility.formatDate(date, dateFormatForDisplay + " " + timeFormat);
    }
}
