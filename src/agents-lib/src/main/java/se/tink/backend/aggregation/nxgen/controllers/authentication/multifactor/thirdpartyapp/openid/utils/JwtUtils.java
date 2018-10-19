package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.utils;

import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class JwtUtils {
    private static final SecureRandom random = new SecureRandom();

    public static Date addHours(Date input, int hours) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(input);
        cal.add(Calendar.HOUR_OF_DAY, hours);
        return cal.getTime();
    }

    public static String generateId() {
        byte[] id = new byte[16];
        random.nextBytes(id);
        return EncodingUtils.encodeAsBase64String(id);
    }

    public static String[] listToStringArray(List<String> list) {
        return list.toArray(new String[list.size()]);
    }
}
