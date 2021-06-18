package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.filter;

import java.util.Optional;
import java.util.regex.Pattern;
import javax.net.ssl.SSLHandshakeException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.AbstractRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class ConsentStatusRetryFilter extends AbstractRetryFilter {

    public static final Pattern URL_PATTERN = Pattern.compile(".*/consent.*/status");

    public ConsentStatusRetryFilter() {
        super(5, 1000);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {

        return Optional.ofNullable(response)
                .filter(HttpResponse::hasBody)
                .filter(this::isResponseFromConsentStatusCall)
                .filter(this::isConsentStatusNotFinal)
                .isPresent();
    }

    private boolean isResponseFromConsentStatusCall(HttpResponse response) {
        String uri = response.getRequest().getUrl().toString();

        return URL_PATTERN.matcher(uri).matches();
    }

    private boolean isConsentStatusNotFinal(HttpResponse response) {
        return Optional.ofNullable(response.getBody(ConsentStatusResponse.class))
                .map(ConsentStatusResponse::getConsentStatus)
                .map(ConsentStatus::isNotFinalStatus)
                .orElse(false);
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        return exception instanceof HttpClientException
                && exception.getCause() instanceof SSLHandshakeException;
    }
}
