package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.filter;

import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.rpc.ErrorEntity;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RequiredArgsConstructor
public class BankErrorResponseFilter extends Filter {

    private final PersistentStorage persistentStorage;

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        try {
            return nextFilter(httpRequest);
        } catch (HttpResponseException e) {
            checkErrorResponseBodyType(e);
            throwIfConsentError(e);
            throw BankServiceError.BANK_SIDE_FAILURE.exception(e.getMessage());
        }
    }

    private void throwIfConsentError(HttpResponseException e) {
        List<ErrorEntity> errors =
                Arrays.asList(
                        SerializationUtils.deserializeFromString(
                                e.getResponse().getBody(String.class), ErrorEntity[].class));
        if (errors.stream().anyMatch(ErrorEntity::isConsentExpiredOrInvalid)) {
            final String consentStatus = errors.stream().findFirst().get().getCode();
            persistentStorage.remove(Storage.CONSENT);
            persistentStorage.remove(Storage.OAUTH_TOKEN);
            throw SessionError.valueOf(consentStatus).exception("Consent Status: " + consentStatus);
        }
    }

    private void checkErrorResponseBodyType(HttpResponseException e) {
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(e.getResponse().getType())) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(
                    "Invalid error response format : " + e.getResponse().getBody(String.class));
        }
    }
}
