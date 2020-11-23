package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class RestrictedDataForLastingConsentsErrorChecker {

    private static List<Pattern> allowedEndpointsPatterns = new LinkedList<>();

    static {
        allowedEndpointsPatterns.add(Pattern.compile("/accounts$"));
        allowedEndpointsPatterns.add(Pattern.compile("/accounts/\\w+$"));
        allowedEndpointsPatterns.add(Pattern.compile("/accounts/\\w+/balances$"));
        allowedEndpointsPatterns.add(Pattern.compile("/accounts/\\w+/transactions$"));
    }

    private final int restrictedDataLastingConsentErrorHttpStatus;

    public RestrictedDataForLastingConsentsErrorChecker(
            int restrictedDataLastingConsentErrorHttpStatus) {
        this.restrictedDataLastingConsentErrorHttpStatus =
                restrictedDataLastingConsentErrorHttpStatus;
    }

    public boolean isRestrictedDataLastingConsentsError(HttpResponseException ex) {
        return ex.getResponse().getStatus() == restrictedDataLastingConsentErrorHttpStatus
                && isDataRestricted(ex.getRequest().getUrl());
    }

    private boolean isDataRestricted(URL url) {
        for (Pattern allowedUrlPattern : allowedEndpointsPatterns) {
            if (allowedUrlPattern.matcher(url.toString()).find()) {
                return false;
            }
        }
        return true;
    }
}
