package com.abellimz.sgbusbuzz.utils;

import android.support.v4.app.NotificationCompat;

import com.abellimz.sgbusbuzz.models.Bus;
import com.abellimz.sgbusbuzz.models.BusStop;
import com.abellimz.sgbusbuzz.models.Service;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

/**
 * Created by Abel on 9/16/2016.
 */

public class BusFormatUtils {

    // For notifications
    private static final String PREFIX_TITLE_NOTIFICATION = "Bus timings for: ";

    // For validation
    private static final String STATUS_OPERATING = "In Operation";
    private static final String STATUS_NOT_OPERATING = "Not In Operation";

    // For display messages
    private static final String DISPLAY_MSG_ARRIVING = "Arriving";
    private static final String DISPLAY_MSG_NOT_OPERATING = "Not Operating Now";
    private static final String DISPLAY_MSG_NO_ESTIMATE = "No Est. Available";

    // For Bus Load
    private static final String LOAD_LOW = "Seats Available";
    private static final String LOAD_MEDIUM = "Standing Available";
    private static final String LOAD_HIGH = "Limited Standing";

    // For WAB
    private static final String FEATURE_WAB = "WAB";

    public static boolean isOperating(Service service) {
        String status = service.getStatus();
        switch (status) {
            case STATUS_OPERATING:
                return true;
            case STATUS_NOT_OPERATING:
                return false;
            default:
                return false;
        }
    }

    public static String bindTimingsToInboxStyle(NotificationCompat.InboxStyle inboxStyle, DateTime currentDt, List<Service> services) {
        String summary = null;
        for (Service service : services) {
            String serviceNo = service.getServiceNo();
            boolean isOperating = BusFormatUtils.isOperating(service);
            if (!isOperating) {
                continue; // NOT DISPLAYING NON-OPERATING SERVICES
            }
            Bus bus1 = service.getNextBus();
            String bus1Text = getFormattedMinsToBus(currentDt, bus1.getEstimatedArrival());
            Bus bus2 = service.getSubsequentBus();
            String bus2Text = getFormattedMinsToBus(currentDt, bus2.getEstimatedArrival());
            Bus bus3 = service.getSubsequentBus3();
            String bus3Text = getFormattedMinsToBus(currentDt, bus3.getEstimatedArrival());
            String formattedLine = getFormattedNotiline(serviceNo, bus1Text, bus2Text, bus3Text);
            inboxStyle.addLine(formattedLine);
            if (summary == null) {
                summary = formattedLine;
            }
        }
        return summary;
    }

    private static String getFormattedNotiline(String serviceNo, String b1, String b2, String b3) {
        return serviceNo + ": Bus 1: " + b1 + " | Bus 2: " + b2 + " | Bus 3: " + b3;
    }

    public static String getTruncatedContent(BusStop busStop) {
        return PREFIX_TITLE_NOTIFICATION + busStop.getDescription();
    }

    public static String getFormattedDistance(Integer distance) {
        if (distance == null) {
            return "";
        }
        int distInKm = distance / 1000;
        return distInKm > 0 ? distInKm + "km" : distance + "m";
    }

    public static int getLoadIndex(String load) {
        if (load == null) {
            return 0;
        }
        switch (load) {
            case LOAD_LOW:
                return 0;
            case LOAD_MEDIUM:
                return 1;
            case LOAD_HIGH:
                return 2;
            default:
                return -1;
        }
    }

    public static String getFormattedMinsToBus(DateTime currentDt, String arrivalTime) {
        if (arrivalTime == null || arrivalTime.isEmpty()) {
            return DISPLAY_MSG_NO_ESTIMATE;
        }
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ");
        DateTimeZone zone = DateTimeZone.forOffsetHours(8);
        if (currentDt == null) {
            currentDt = new DateTime(zone);
        }
        DateTime nextArrivalDt = formatter.parseDateTime(arrivalTime);
        long diffInSeconds = nextArrivalDt.getMillis() - currentDt.getMillis();
        int days = (int) (diffInSeconds / (1000 * 60 * 60 * 24));
        int hours = (int) ((diffInSeconds - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
        int min = (int) (diffInSeconds - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours)) / (1000 * 60);
        if (min > 0) {
            return String.valueOf(min);
        } else {
            return DISPLAY_MSG_ARRIVING;
        }
    }

    public static String getDisplayMsgNotOperating() {
        return DISPLAY_MSG_NOT_OPERATING;
    }

}
