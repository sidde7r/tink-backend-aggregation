package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.rpc.AccountSetupRequest;

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

    // Not required to send actual values, sending dummy data
    public static String getJWSSignature(AccountSetupRequest request) {
        return "";
    }

    // Can fetch transactions max 2 years back
    public static Date getFromDate() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.YEAR, -2);
        return c.getTime();
    }

    public static Date getToDate() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        return c.getTime();
    }

    // Can be max 90 days in the future
    public static Date getExpirationDate() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, 90);
        return c.getTime();
    }

    public static String getLastLoggedTime(Date date) {
        return ICSConstants.Date.LAST_LOGGED_TIME_FORMAT.format(date);
    }
}
