package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.utils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.apache.http.impl.cookie.BasicClientCookie;

public class CaisseEpargneUtils {
    public static Optional<BasicClientCookie> parseRawCookie(String rawCookie) {

        List<String> rawCookieParams = Arrays.asList(rawCookie.split(";"));
        List<String> rawCookieNameAndValue = Arrays.asList(rawCookieParams.get(0).split("="));
        if (rawCookieNameAndValue.size() != 2) {
            return Optional.empty();
        }

        String cookieName = rawCookieNameAndValue.get(0).trim();
        String cookieValue = rawCookieNameAndValue.get(1).trim();
        BasicClientCookie cookie = new BasicClientCookie(cookieName, cookieValue);
        for (String rawCookieParam : rawCookieParams) {
            List<String> rawCookieParamNameAndValue =
                    Arrays.asList(rawCookieParam.trim().split("="));
            String paramName = rawCookieParamNameAndValue.get(0).trim();

            if (paramName.equalsIgnoreCase("secure")) {
                cookie.setSecure(true);
            } else if (paramName.equalsIgnoreCase("httpOnly")) {
                cookie.setAttribute(paramName, null);
            } else {
                if (rawCookieParamNameAndValue.size() != 2) {
                    continue;
                }
                String paramValue = rawCookieParamNameAndValue.get(1).trim();

                if (paramName.equalsIgnoreCase("max-age")) {
                    long maxAge = Long.parseLong(paramValue);
                    Date expiryDate = new Date(System.currentTimeMillis() + maxAge);
                    cookie.setExpiryDate(expiryDate);
                } else if (paramName.equalsIgnoreCase("domain")) {
                    cookie.setDomain(paramValue);
                } else if (paramName.equalsIgnoreCase("path")) {
                    cookie.setPath(paramValue);
                } else if (paramName.equalsIgnoreCase("comment")) {
                    cookie.setComment(paramValue);
                }
            }
        }
        return Optional.of(cookie);
    }
}
