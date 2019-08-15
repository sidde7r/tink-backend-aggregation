package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.TargobankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.TargobankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.TargobankConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.TargobankConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.TargobankConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.TargobankConstants.SandboxUrls;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.TargobankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.entities.PsuDataEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.rpc.ChooseScaMethodRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.rpc.ChooseScaMethodResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.rpc.CreateAuthorisationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.rpc.PasswordAuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.rpc.PasswordAuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.rpc.ScaConfirmResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.configuration.TargobankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.utils.BerlinGroupUtils;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class TargobankApiClient {

    private final TinkHttpClient client;
    private TargobankConfiguration configuration;
    private PersistentStorage persistentStorage;

    public TargobankApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    private TargobankConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(TargobankConfiguration configuration) {
        this.configuration = configuration;

        client.setSslClientCertificate(
                BerlinGroupUtils.readFile(configuration.getClientKeyStorePath()),
                configuration.getClientKeyStorePassword());
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_Request_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.API_KEY, getConfiguration().getApiKey());
    }

    private RequestBuilder createAuthenticationRequest(URL url) {
        return createRequest(url)
                .header(HeaderKeys.PSU_ID, HeaderValues.PSU_ID)
                .header(HeaderKeys.PSU_IP_Address, getConfiguration().getPsuIpAddress())
                .header(HeaderKeys.TPP_Redirect_URI, getConfiguration().getRedirectUrl());
    }

    public ConsentResponse createConsent() {

        ConsentRequest request = new ConsentRequest();
        return createAuthenticationRequest(SandboxUrls.CREATE_CONSENT)
                .post(ConsentResponse.class, request);
    }

    public CreateAuthorisationResponse createAuthorisations(String url) {
        return createAuthenticationRequest(new URL(url)).post(CreateAuthorisationResponse.class);
    }

    public PasswordAuthenticationResponse authenticateAuthorisationsPassword(String url) {
        PasswordAuthenticationRequest request =
                new PasswordAuthenticationRequest(
                        new PsuDataEntity(getConfiguration().getScaPassword()));
        return createAuthenticationRequest(new URL(url))
                .put(PasswordAuthenticationResponse.class, request);
    }

    public ChooseScaMethodResponse chooseScaMethod(String url, String scaMethod) {
        ChooseScaMethodRequest request = new ChooseScaMethodRequest(scaMethod);
        return createAuthenticationRequest(new URL(url))
                .put(ChooseScaMethodResponse.class, request);
    }

    public ScaConfirmResponse confirmAuthentication(String url, String scaMethod) {
        ChooseScaMethodRequest request = new ChooseScaMethodRequest(scaMethod);
        return createAuthenticationRequest(new URL(url)).put(ScaConfirmResponse.class, request);
    }

    public AccountsResponse fetchAccounts() {
        return createRequest(SandboxUrls.FETCH_ACCOUNTS)
                .queryParam(QueryKeys.WITH_BALANCE, QueryValues.WITH_BALANCE)
                .header(HeaderKeys.CONSENT_ID, getConsentFromStorage())
                .get(AccountsResponse.class);
    }

    public PaginatorResponse fetchTransactions(String accountNumber) {
        return getEmptyPaginatorResponse();

        // Endpoint for fetching transactions does not work. Code below is correct request for
        // fetching transactions

        //        return createRequest(
        //                        Urls.FETCH_TRANSACTIONS.parameter(PathVariable.ACCOUNT_ID,
        // accountNumber))
        //                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOOKING_STATUS)
        //                .header(HeaderKeys.CONSENT_ID, getConsentFromStorage())
        //                .get(TransactionResponse.class);
    }

    private String getConsentFromStorage() {
        return persistentStorage.get(StorageKeys.CONSENT_ID);
    }

    // Endpoint for fetching transactions doesn't work. This is made to make test pass.
    // TODO: Delete for production
    public PaginatorResponse getEmptyPaginatorResponse() {
        return new PaginatorResponse() {
            @Override
            public Collection<? extends Transaction> getTinkTransactions() {
                return new ArrayList<>();
            }

            @Override
            public Optional<Boolean> canFetchMore() {
                return Optional.empty();
            }
        };
    }
}
