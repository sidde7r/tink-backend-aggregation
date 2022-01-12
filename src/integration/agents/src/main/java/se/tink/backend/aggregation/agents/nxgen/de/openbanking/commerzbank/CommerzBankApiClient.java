package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.ApiServices;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
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
            RandomValueGenerator randomValueGenerator,
            LogMasker logMasker) {
        super(
                client,
                persistentStorage,
                configuration,
                userPresent,
                userIp,
                randomValueGenerator,
                logMasker);
    }

    public HttpResponse createPayment(CreatePaymentRequest createPaymentRequest, String username) {
        RequestBuilder requestBuilder =
                createRequest(new URL(configuration.getBaseUrl() + ApiServices.CREATE_PAYMENT))
                        .header(HeaderKeys.PSU_ID, username)
                        .header(HeaderKeys.PSU_ID_TYPE, HeaderValues.RETAIL)
                        .header(HeaderKeys.TPP_REDIRECT_PREFFERED, "false")
                        .header(
                                Xs2aDevelopersConstants.HeaderKeys.TPP_REDIRECT_URI,
                                configuration.getRedirectUrl())
                        .header(
                                Xs2aDevelopersConstants.HeaderKeys.X_REQUEST_ID,
                                randomValueGenerator.getUUID())
                        .body(createPaymentRequest);

        requestBuilder.headers(getUserSpecificHeaders());
        return requestBuilder.post(HttpResponse.class);
    }
}
