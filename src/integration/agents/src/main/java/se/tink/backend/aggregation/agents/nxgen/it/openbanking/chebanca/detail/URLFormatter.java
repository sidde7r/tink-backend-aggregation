package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail;

import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants;
import se.tink.backend.aggregation.nxgen.http.url.URL;

class URLFormatter {
    private static final String EXPECTED_FORMAT = "%s %s";

    static String formatToString(String httpMethod, URL url) {
        return String.format(EXPECTED_FORMAT, httpMethod, extractUrlWithoutBase(url));
    }

    private static String extractUrlWithoutBase(URL url) {
        return url.get().replace(ChebancaConstants.Urls.BASE_URL, "");
    }
}
