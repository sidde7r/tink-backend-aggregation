package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.filter;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.net.ssl.SSLHandshakeException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterOrder;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterPhases;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.AbstractRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@FilterOrder(category = FilterPhases.REQUEST_HANDLE, order = Integer.MAX_VALUE)
public class ConsentStatusRetryFilter extends AbstractRetryFilter {

    public static final Pattern URL_PATTERN = Pattern.compile(".*/consent.*/status");

    public ConsentStatusRetryFilter() {
        super(5, 1000);
    }

    @Override
    protected boolean shouldRetry(HttpResponse response) {
        Optional<HttpResponse> retry =
                Optional.ofNullable(response)
                        .filter(HttpResponse::hasBody)
                        .filter(this::isResponseFromConsentStatusCall)
                        .filter(this::isConsentStatusNotFinal);

        if (retry.isPresent()) {
            log.info("Consent retry filter will try to retry.");
        }
        return retry.isPresent();
    }

    private boolean isResponseFromConsentStatusCall(HttpResponse response) {
        String uri = response.getRequest().getUrl().toString();

        return URL_PATTERN.matcher(uri).matches();
    }

    private boolean isConsentStatusNotFinal(HttpResponse response) {
        return Optional.of(response.getBody(ConsentStatusResponse.class))
                .filter(body -> !Objects.isNull(body.getTransactionStatus()))
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
