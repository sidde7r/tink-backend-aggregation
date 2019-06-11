package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia;

import java.util.Date;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.rpc.AuthorizeConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.rpc.CreateConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.rpc.CreateConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.configuration.FiduciaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public final class FiduciaApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private FiduciaConfiguration configuration;

    public FiduciaApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    private FiduciaConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(FiduciaConfiguration configuration) {
        this.configuration = configuration;
    }

    private RequestBuilder createRequest(
            URL url,
            String reqId,
            String digest,
            String signature,
            String certificate,
            String date) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_REQUEST_ID, reqId)
                .header(HeaderKeys.DIGEST, digest)
                .header(HeaderKeys.SIGNATURE, signature)
                .header(HeaderKeys.TPP_SIGNATURE_CERTIFICATE, certificate)
                .header(HeaderKeys.DATE, date);
    }

    private RequestBuilder createRequestInSession(
            URL url,
            String reqId,
            String digest,
            String signature,
            String certificate,
            String date) {
        // Consent id is mocked to work only with their default value
        return createRequest(url, reqId, digest, signature, certificate, date)
                .header(HeaderKeys.CONSENT_ID, HeaderValues.CONSENT_VALID);
    }

    public CreateConsentResponse createConsent(
            CreateConsentRequest body,
            String digest,
            String certificate,
            String signature,
            String reqId,
            String date,
            String psuId) {
        return createRequest(Urls.CREATE_CONSENT, reqId, digest, signature, certificate, date)
                .header(HeaderKeys.PSU_ID, psuId)
                .post(CreateConsentResponse.class, body);
    }

    public void authorizeConsent(
            CreateConsentResponse createConsentResponse,
            String digest,
            String certificate,
            String signature,
            String reqId,
            String date,
            AuthorizeConsentRequest body) {
        createRequest(
                        Urls.AUTHORIZE_CONSENT.parameter(
                                IdTags.CONSENT_ID, createConsentResponse.getConsentId()),
                        reqId,
                        digest,
                        signature,
                        certificate,
                        date)
                .post(HttpResponse.class, body);
    }

    public GetAccountsResponse getAccounts(
            String digest, String certificate, String signature, String reqId, String date) {
        return createRequestInSession(
                        Urls.GET_ACCOUNTS, reqId, digest, signature, certificate, date)
                .get(GetAccountsResponse.class);
    }

    public GetBalancesResponse getBalances(
            AccountEntity acc,
            String digest,
            String certificate,
            String signature,
            String reqId,
            String date) {
        return createRequestInSession(
                        Urls.GET_BALANCES.parameter(IdTags.ACCOUNT_ID, acc.getResourceId()),
                        reqId,
                        digest,
                        signature,
                        certificate,
                        date)
                .get(GetBalancesResponse.class);
    }

    public PaginatorResponse getTransactions(
            TransactionalAccount account,
            Date fromDate,
            Date toDate,
            String digest,
            String certificate,
            String signature,
            String reqId,
            String date) {
        return createRequestInSession(
                        Urls.GET_TRANSACTIONS.parameter(
                                IdTags.ACCOUNT_ID, account.getApiIdentifier()),
                        reqId,
                        digest,
                        signature,
                        certificate,
                        date)
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOOKED)
                .queryParam(
                        QueryKeys.DATE_FROM, ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(QueryKeys.DATE_TO, ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .get(GetTransactionsResponse.class);
    }
}
