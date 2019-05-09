package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.Formats;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.PathParameterKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.configuration.SibsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.utils.SibsUtils;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SibsBaseApiClient {

    protected final TinkHttpClient client;
    protected final PersistentStorage persistentStorage;
    protected SibsConfiguration configuration;

    public SibsBaseApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    protected void setConfiguration(SibsConfiguration configuration) {
        this.configuration = Preconditions.checkNotNull(configuration);
    }

    protected RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createSignedRequest(URL url, String digest) {

        String transactionId = SibsUtils.getRequestId();
        String requestId = SibsUtils.getRequestId();

        String requestTimestamp =
                new SimpleDateFormat(Formats.CONSENT_REQUEST_DATE_FORMAT).format(new Date());

        String signature =
                SibsUtils.getSignature(
                        digest,
                        transactionId,
                        requestId,
                        requestTimestamp,
                        configuration.getClientSigningKeyPath(),
                        configuration.getClientSigningCertificateSerialNumber());

        RequestBuilder signedRequest =
                createRequest(url)
                        .header(HeaderKeys.X_IBM_CLIENT_ID, configuration.getClientId())
                        .header(
                                HeaderKeys.TPP_CERTIFICATE,
                                SibsUtils.readSigningCertificate(
                                        configuration.getClientSigningCertificatePath()))
                        .header(HeaderKeys.SIGNATURE, signature)
                        .header(HeaderKeys.TPP_TRANSACTION_ID, transactionId)
                        .header(HeaderKeys.TPP_REQUEST_ID, requestId)
                        .header(HeaderKeys.DATE, requestTimestamp);

        if (!Strings.isNullOrEmpty(digest)) {
            signedRequest =
                    signedRequest.header(HeaderKeys.DIGEST, HeaderValues.DIGEST_PREFIX + digest);
        }

        return signedRequest;
    }

    private RequestBuilder createSignedRequestInSession(URL url, String digest) {
        return createSignedRequest(url, digest)
                .header(HeaderKeys.CONSENT_ID, getConsentFromStorage());
    }

    private String getConsentFromStorage() {
        return persistentStorage
                .get(StorageKeys.CONSENT_ID, String.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public AccountsResponse fetchAccounts() {

        return createSignedRequestInSession(
                        Urls.ACCOUNTS.parameter(
                                PathParameterKeys.ASPSP_CDE, configuration.getAspspCode()),
                        "")
                .queryParam(QueryKeys.WITH_BALANCE, String.valueOf(true))
                .get(AccountsResponse.class);
    }

    public BalancesResponse getAccountBalances(String accountId) {

        return createSignedRequestInSession(
                        Urls.ACCOUNT_BALANCES
                                .parameter(
                                        PathParameterKeys.ASPSP_CDE, configuration.getAspspCode())
                                .parameter(PathParameterKeys.ACCOUNT_ID, accountId),
                        "")
                .queryParam(QueryKeys.PSU_INVOLVED, String.valueOf(true))
                .get(BalancesResponse.class);
    }

    public TransactionsResponse getAccountTransactions(
            TransactionalAccount account, Date fromDate, Date toDate) {

        SimpleDateFormat formatter = new SimpleDateFormat(Formats.PAGINATION_DATE_FORMAT);

        return createSignedRequestInSession(
                        Urls.ACCOUNT_TRANSACTIONS
                                .parameter(
                                        PathParameterKeys.ASPSP_CDE, configuration.getAspspCode())
                                .parameter(
                                        PathParameterKeys.ACCOUNT_ID, account.getApiIdentifier()),
                        "")
                .queryParam(QueryKeys.WITH_BALANCE, String.valueOf(true))
                .queryParam(QueryKeys.PSU_INVOLVED, String.valueOf(true))
                .queryParam(QueryKeys.BOOKING_STATUS, SibsConstants.QueryValues.BOTH)
                .queryParam(QueryKeys.DATE_FROM, formatter.format(fromDate))
                .queryParam(QueryKeys.DATE_TO, formatter.format(toDate))
                .get(TransactionsResponse.class);
    }

    public URL buildAuthorizeUrl(String state) {

        ConsentRequest consentRequest = getConsentRequest();
        String digest = SibsUtils.getDigest(consentRequest);

        ConsentResponse consentResponse =
                createSignedRequest(
                                Urls.CREATE_CONSENT.parameter(
                                        PathParameterKeys.ASPSP_CDE, configuration.getAspspCode()),
                                digest)
                        .header(
                                HeaderKeys.TPP_REDIRECT_URI,
                                new URL(configuration.getRedirectUrl())
                                        .queryParam(QueryKeys.STATE, state)
                                        .queryParam(QueryKeys.CODE, SibsUtils.getRequestId()))
                        .post(ConsentResponse.class, consentRequest);

        persistentStorage.put(StorageKeys.CONSENT_ID, consentResponse.getConsentId());

        return new URL(consentResponse.getLinks().getRedirect());
    }

    public ConsentStatusResponse getConsentStatus() {

        return createSignedRequestInSession(
                        Urls.CONSENT_STATUS
                                .parameter(
                                        PathParameterKeys.ASPSP_CDE, configuration.getAspspCode())
                                .parameter(PathParameterKeys.CONSENT_ID, getConsentFromStorage()),
                        "")
                .get(ConsentStatusResponse.class);
    }

    private ConsentRequest getConsentRequest() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, 1); // Consent valid for 1 day

        SimpleDateFormat formatter = new SimpleDateFormat(Formats.CONSENT_BODY_DATE_FORMAT);

        return new ConsentRequest(
                new ConsentAccessEntity(SibsConstants.FormValues.ALL_ACCOUNTS),
                false,
                formatter.format(c.getTime()),
                SibsConstants.FormValues.FREQUENCY_PER_DAY,
                false);
    }
}
