package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base;

import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.rpc.AccountPermissionRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.PartyV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.TrustedBeneficiaryEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.rpc.AccountBalanceV31Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.rpc.AccountsV31Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.rpc.PartiesV31Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.rpc.PartyV31Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.rpc.TrustedBeneficiariesV31Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingConstants.PartyEndpoint;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public class UkOpenBankingApiClient extends OpenIdApiClient {

    private final PersistentStorage persistentStorage;
    private final UkOpenBankingAisConfig aisConfig;

    public UkOpenBankingApiClient(
            TinkHttpClient httpClient,
            JwtSigner signer,
            SoftwareStatementAssertion softwareStatement,
            String redirectUrl,
            ClientInfo providerConfiguration,
            RandomValueGenerator randomValueGenerator,
            PersistentStorage persistentStorage,
            UkOpenBankingAisConfig aisConfig) {
        super(
                httpClient,
                signer,
                softwareStatement,
                redirectUrl,
                providerConfiguration,
                aisConfig.getWellKnownURL(),
                randomValueGenerator);

        this.persistentStorage = persistentStorage;
        this.aisConfig = aisConfig;
    }

    public List<AccountEntity> fetchV31Accounts() {
        return createAisRequest(aisConfig.getBulkAccountRequestURL())
                .get(AccountsV31Response.class)
                .getData()
                .orElse(Collections.emptyList());
    }

    public List<AccountBalanceEntity> fetchV31AccountBalances(String accountId) {
        return createAisRequest(aisConfig.getAccountBalanceRequestURL(accountId))
                .get(AccountBalanceV31Response.class)
                .getData()
                .orElse(Collections.emptyList());
    }

    public List<TrustedBeneficiaryEntity> fetchV31AccountBeneficiaries(String accountId) {
        return createAisRequest(aisConfig.getAccountBeneficiariesRequestURL(accountId))
                .get(TrustedBeneficiariesV31Response.class)
                .getData()
                .orElse(Collections.emptyList());
    }

    public Optional<PartyV31Entity> fetchV31Party() {
        return executeV31FetchPartyRequest(
                createAisRequest(aisConfig.getApiBaseURL().concat(PartyEndpoint.PARTY.getPath())));
    }

    public Optional<PartyV31Entity> fetchV31Party(String accountId) {
        String path = String.format(PartyEndpoint.ACCOUNT_ID_PARTY.getPath(), accountId);
        return executeV31FetchPartyRequest(
                createAisRequest(aisConfig.getApiBaseURL().concat(path)));
    }

    private Optional<PartyV31Entity> executeV31FetchPartyRequest(RequestBuilder requestBuilder) {
        try {
            return requestBuilder.get(PartyV31Response.class).getData();
        } catch (HttpResponseException ex) {
            checkForRestrictedDataForLastingConsentsError(ex);
            return Optional.empty();
        }
    }

    private void checkForRestrictedDataForLastingConsentsError(
            HttpResponseException responseException) {
        if (!new RestrictedDataForLastingConsentsErrorChecker(403)
                .isRestrictedDataLastingConsentsError(responseException)) {
            throw responseException;
        }
    }

    public List<PartyV31Entity> fetchV31Parties(String accountId) {
        try {
            String path = String.format(PartyEndpoint.ACCOUNT_ID_PARTIES.getPath(), accountId);
            return createAisRequest(aisConfig.getApiBaseURL().concat(path))
                    .get(PartiesV31Response.class)
                    .getData()
                    .orElse(Collections.emptyList());
        } catch (HttpResponseException ex) {
            if (!new RestrictedDataForLastingConsentsErrorChecker(401)
                    .isRestrictedDataLastingConsentsError(ex)) {
                throw ex;
            }
        }
        return Collections.emptyList();
    }

    public <T> T fetchAccountBalance(String accountId, Class<T> responseType) {
        return createAisRequest(aisConfig.getAccountBalanceRequestURL(accountId)).get(responseType);
    }

    public <T> T fetchAccountTransactions(String paginationKey, Class<T> responseType) {

        // Check if the key provided is a complete url or if it should be appended on the apiBase
        URL url = new URL(paginationKey);
        if (url.getScheme() == null) url = aisConfig.getApiBaseURL().concat(paginationKey);

        return createAisRequest(url).get(responseType);
    }

    public <T> T fetchUpcomingTransactions(String accountId, Class<T> responseType) {
        try {

            return createAisRequest(aisConfig.getUpcomingTransactionRequestURL(accountId))
                    .get(responseType);
        } catch (Exception e) {
            // TODO: Ukob testdata has an error in it which makes some transactions impossible to
            // parse.
            // TODO: This combined with the null check in UpcomingTransactionFetcher discards those
            // transactions to prevents crash.
            return null;
        }
    }

    public AccountPermissionResponse fetchIntentDetails(String consentId) {
        return createAisRequest(aisConfig.getConsentDetailsRequestURL(consentId))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .get(AccountPermissionResponse.class);
    }

    public String fetchIntentIdString() {
        AccountPermissionRequest accountPermissionRequest =
                AccountPermissionRequest.create(aisConfig.getPermissions());
        String intentId;
        try {
            intentId =
                    aisConfig.getIntentId(
                            createAisRequest(aisConfig.createConsentRequestURL())
                                    .type(MediaType.APPLICATION_JSON_TYPE)
                                    .body(accountPermissionRequest)
                                    .post(aisConfig.getIntentIdResponseType()));
        } catch (HttpResponseException e) {
            HttpResponse errorResponse = e.getResponse();
            if (errorResponse.getStatus() >= 500) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception();
            }
            throw e;
        }
        saveIntentId(intentId);
        saveAppliedPermissions(aisConfig.getPermissions());

        return intentId;
    }

    public String saveIntentId(String intentId) {
        return persistentStorage.put(
                UkOpenBankingV31Constants.PersistentStorageKeys.AIS_ACCOUNT_CONSENT_ID, intentId);
    }

    private String saveAppliedPermissions(ImmutableSet<String> permissions) {
        return persistentStorage.put(
                UkOpenBankingV31Constants.PersistentStorageKeys.AIS_ACCOUNT_PERMISSIONS_GRANTED,
                permissions);
    }

    private RequestBuilder createAisRequest(URL url) {
        return httpClient
                .request(url)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .addFilter(getAisAuthFilter());
    }
}
