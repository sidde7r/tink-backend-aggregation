package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole;

import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.CreditAgricoleConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.CreditAgricoleConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.CreditAgricoleConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.CreditAgricoleConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.CreditAgricoleConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.configuration.CreditAgricoleConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity.HalPaymentRequestCreation;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity.HalPaymentRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity.PaymentRequestResourceEntity;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class CreditAgricoleApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private CreditAgricoleConfiguration configuration;

    public CreditAgricoleApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    private CreditAgricoleConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(CreditAgricoleConfiguration configuration) {
        this.configuration = configuration;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        final String authToken = getTokenFromStorage();

        return createRequest(url).header(HeaderKeys.AUTHORIZATION, authToken);
    }

    private String getTokenFromStorage() {
        return persistentStorage.get(StorageKeys.OAUTH_TOKEN);
    }

    public GetAccountsResponse getAccounts() {
        return createRequestInSession(Urls.ACCOUNTS).get(GetAccountsResponse.class);
    }

    public GetTransactionsResponse getTransactions(String id) {
        return createRequestInSession(Urls.TRANSACTIONS.parameter(IdTags.ACCOUNT_ID, id))
                .get(GetTransactionsResponse.class);
    }

    public HalPaymentRequestCreation makePayment(
            PaymentRequestResourceEntity paymentRequestResourceEntity) {

        String body = SerializationUtils.serializeToString(paymentRequestResourceEntity);

        return createRequestInSession(Urls.CREATE_PAYMENT_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .post(HalPaymentRequestCreation.class, body);
    }

    public HalPaymentRequestEntity fetchPayment(String uniqueId) {
        return createRequestInSession(
                        Urls.FETCH_PAYMENT_REQUEST.parameter(
                                IdTags.PAYMENT_REQUEST_RESOURCE_ID, uniqueId))
                .get(HalPaymentRequestEntity.class);
    }
}
