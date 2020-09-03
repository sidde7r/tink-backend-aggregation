package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.util;

public class UrlParseUtil {
    private UrlParseUtil() {}

    public static String idFromUrl(String resourceIdUrl) {

        return resourceIdUrl.substring(resourceIdUrl.lastIndexOf("/") + 1);
    }
}
