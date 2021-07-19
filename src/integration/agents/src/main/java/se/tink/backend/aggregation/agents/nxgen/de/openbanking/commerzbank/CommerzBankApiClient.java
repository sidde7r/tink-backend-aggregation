package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.CommerzBankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.payment.AuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.ApiServices;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public class CommerzBankApiClient extends Xs2aDevelopersApiClient {

    public CommerzBankApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            Xs2aDevelopersProviderConfiguration configuration,
            boolean userPresent,
            String userIp,
            RandomValueGenerator randomValueGenerator) {
        super(client, persistentStorage, configuration, userPresent, userIp, randomValueGenerator);
    }

    public void authenticate(String authenticationUrl, String username) {
        try {

            AuthorizationResponse authorizationResponse =
                    createRequest(new URL(authenticationUrl))
                            .header(HeaderKeys.X_REQUEST_ID, randomValueGenerator.getUUID())
                            .header(HeaderKeys.PSU_ID, username)
                            .get(AuthorizationResponse.class);

            persistentStorage.put(
                    StorageKeys.AUTHORISATION_URL,
                    authorizationResponse.getAuthorizationEndpoint());

        } catch (HttpResponseException e) {
            log.error("Error during CommerzBank payment authorization", e);
            throw e;
        }
    }

    @Override
    public CreatePaymentResponse createPayment(CreatePaymentRequest createPaymentRequest) {
        RequestBuilder requestBuilder =
                createRequest(new URL(configuration.getBaseUrl() + ApiServices.CREATE_PAYMENT))
                        .header(
                                Xs2aDevelopersConstants.HeaderKeys.TPP_REDIRECT_URI,
                                configuration.getRedirectUrl())
                        .header(
                                Xs2aDevelopersConstants.HeaderKeys.X_REQUEST_ID,
                                randomValueGenerator.getUUID())
                        .body(createPaymentRequest);

        requestBuilder.headers(getUserSpecificHeaders());
        return requestBuilder.post(CreatePaymentResponse.class);
    }

    @Override
    protected Map<String, Object> getUserSpecificHeaders() {
        Map<String, Object> headers = super.getUserSpecificHeaders();
        headers.put(HeaderKeys.TPP_EXPLICIT_AUTHORISATION_PREFERRED, false);
        return headers;
    }
}
