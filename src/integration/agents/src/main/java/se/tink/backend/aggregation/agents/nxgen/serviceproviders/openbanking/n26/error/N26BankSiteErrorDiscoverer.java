package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.error;

import com.google.common.collect.Sets;
import java.util.Set;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class N26BankSiteErrorDiscoverer {

    private static final Set<String> bankSiteErrorBodyMessageParts =
            Sets.newHashSet(
                    "upstream request timeout",
                    "MIME media type text/plain was not found",
                    "upstream connect error");

    public boolean isBankSiteError(HttpClientException ex) {
        return isBankSiteError(ex.getMessage());
    }

    public boolean isBankSiteError(HttpResponse httpResponse) {
        return isBankSiteError(httpResponse.getBody(String.class));
    }

    private boolean isBankSiteError(String message) {
        return bankSiteErrorBodyMessageParts.stream().anyMatch(v -> message.contains(v));
    }
}
