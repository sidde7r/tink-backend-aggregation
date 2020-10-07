package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class ICSUtils {

    public static String getInteractionId() {
        return UUID.randomUUID().toString();
    }

    // Not required to send actual values, sending dummy data
    public static String getFinancialId() {
        return "e3213dfd-435fgrd5-e7edr4";
    }

    // Not required to send actual values, sending dummy data
    public static String getCustomerIpAdress() {
        return "234.213.323.123";
    }

    // Can fetch transactions max 3 years back
    public static Date getFromDate() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.YEAR, -3);
        return c.getTime();
    }

    public static Date getToAndExpiredDate() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, 89);
        return c.getTime();
    }

    public static String getLastLoggedTime(Date date) {
        final SimpleDateFormat lastLoggedTimeFormat =
                new SimpleDateFormat("EEE, dd MMMM yyyy HH:mm:ss z");
        return lastLoggedTimeFormat.format(date);
    }
}
