package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.HEADERS_TO_SIGN;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator.rpc.ScaResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.configuration.SparebankApiConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.QsealcSignerImpl;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

@RequiredArgsConstructor
public class SparebankApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final EidasProxyConfiguration eidasProxyConfiguration;
    private final EidasIdentity eidasIdentity;
    private final SparebankApiConfiguration apiConfiguration;

    public void setPsuId(String psuId) {
        persistentStorage.put(StorageKeys.PSU_ID, psuId);
    }

    public void setTppSessionId(String tppSessionId) {
        persistentStorage.put(StorageKeys.SESSION_ID, tppSessionId);
    }

    private void storeAccounts(AccountResponse accountResponse) {
        persistentStorage.put(StorageKeys.ACCOUNTS, accountResponse);
    }

    public void clearSessionData() {
        persistentStorage.remove(StorageKeys.ACCOUNTS);
        persistentStorage.remove(StorageKeys.SESSION_ID);
        persistentStorage.remove(StorageKeys.PSU_ID);
        persistentStorage.remove(StorageKeys.STATE);
        persistentStorage.remove(StorageKeys.TRANSACTIONS_ALL_FETCHED);
    }

    public Optional<String> getPsuId() {
        return Optional.ofNullable(persistentStorage.get(StorageKeys.PSU_ID));
    }

    public Optional<String> getSessionId() {
        return Optional.ofNullable(persistentStorage.get(StorageKeys.SESSION_ID));
    }

    public Optional<AccountResponse> getStoredAccounts() {
        return persistentStorage.get(StorageKeys.ACCOUNTS, AccountResponse.class);
    }

    private RequestBuilder createRequest(URL url) {
        return createRequest(url, Optional.empty());
    }

    private RequestBuilder createRequest(URL url, Optional<String> digest) {
        Map<String, Object> headers = getHeaders(UUID.randomUUID().toString(), digest);
        headers.put(HeaderKeys.SIGNATURE, generateSignatureHeader(headers));
        return client.request(url).headers(headers);
    }

    public ScaResponse getScaRedirect(String state) throws HttpResponseException {
        persistentStorage.put(StorageKeys.STATE, state);
        return createRequest(new URL(apiConfiguration.getBaseUrl() + Urls.GET_SCA_REDIRECT))
                .post(ScaResponse.class);
    }

    public AccountResponse fetchAccounts() {
        Optional<AccountResponse> maybeAccounts = getStoredAccounts();
        if (maybeAccounts.isPresent()) {
            return maybeAccounts.get();
        } else {
            AccountResponse accountResponse =
                    createRequest(new URL(apiConfiguration.getBaseUrl() + Urls.GET_ACCOUNTS))
                            .queryParam(QueryKeys.WITH_BALANCE, "false")
                            .get(AccountResponse.class);
            storeAccounts(accountResponse);
            return accountResponse;
        }
    }

    public BalanceResponse fetchBalances(String resourceId) {
        return createRequest(
                        new URL(apiConfiguration.getBaseUrl() + Urls.FETCH_BALANCES)
                                .parameter(IdTags.RESOURCE_ID, resourceId))
                .get(BalanceResponse.class);
    }

    public TransactionResponse fetchTransactions(String resourceId) {
        LocalDate fromDate;
        LocalDate toDate = LocalDate.now();
        if (persistentStorage.get(StorageKeys.TRANSACTIONS_ALL_FETCHED, String.class).isPresent()) {
            fromDate = LocalDate.now().minusDays(89);
        } else {
            fromDate = LocalDate.of(1970, 1, 1);
            persistentStorage.put(StorageKeys.TRANSACTIONS_ALL_FETCHED, "true");
        }

        return createRequest(
                        new URL(apiConfiguration.getBaseUrl() + Urls.FETCH_TRANSACTIONS)
                                .parameter(IdTags.RESOURCE_ID, resourceId))
                .queryParam(
                        SparebankConstants.QueryKeys.DATE_FROM,
                        fromDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .queryParam(
                        SparebankConstants.QueryKeys.DATE_TO,
                        toDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .queryParam(
                        SparebankConstants.QueryKeys.LIMIT,
                        SparebankConstants.QueryValues.TRANSACTION_LIMIT)
                .queryParam(
                        SparebankConstants.QueryKeys.BOOKING_STATUS,
                        SparebankConstants.QueryValues.BOOKING_STATUS)
                .get(TransactionResponse.class);
    }

    public TransactionResponse fetchTransactions(String resourceId, Date fromDate, Date toDate) {
        return createRequest(
                        new URL(apiConfiguration.getBaseUrl() + Urls.FETCH_TRANSACTIONS)
                                .parameter(IdTags.RESOURCE_ID, resourceId))
                .queryParam(
                        SparebankConstants.QueryKeys.DATE_FROM,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(
                        SparebankConstants.QueryKeys.DATE_TO,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .queryParam(
                        SparebankConstants.QueryKeys.LIMIT,
                        SparebankConstants.QueryValues.TRANSACTION_LIMIT)
                .queryParam(
                        SparebankConstants.QueryKeys.BOOKING_STATUS,
                        SparebankConstants.QueryValues.BOOKING_STATUS)
                .get(TransactionResponse.class);
    }

    public TransactionResponse fetchNextTransactions(String path) {
        return createRequest(new URL(apiConfiguration.getBaseUrl() + path))
                .get(TransactionResponse.class);
    }

    private Map<String, Object> getHeaders(String requestId, Optional<String> digest) {
        String tppRedirectUrl =
                new URL(apiConfiguration.getRedirectUrl())
                        .queryParam(QueryKeys.STATE, persistentStorage.get(StorageKeys.STATE))
                        .toString();

        Map<String, Object> headers = new HashMap<>();
        headers.put(HeaderKeys.ACCEPT, MediaType.APPLICATION_JSON);
        headers.put(
                HeaderKeys.DATE, ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
        headers.put(HeaderKeys.X_REQUEST_ID, requestId);
        headers.put(HeaderKeys.TPP_REDIRECT_URI, tppRedirectUrl);
        headers.put(HeaderKeys.TPP_SIGNATURE_CERTIFICATE, apiConfiguration.getQsealcBase64());

        // if this condition is correct is currently a mistery while auto auth is not implemented.
        // Added info about that in ticket ITE-1648
        if (apiConfiguration.isManual()) {
            headers.put(HeaderKeys.PSU_IP_ADDRESS, apiConfiguration.getUserIp());
        }

        digest.ifPresent(digestString -> headers.put(HeaderKeys.DIGEST, digestString));
        getSessionId().ifPresent(sessionId -> headers.put(HeaderKeys.TPP_SESSION_ID, sessionId));
        getPsuId().ifPresent(psuId -> headers.put(HeaderKeys.PSU_ID, psuId));

        return headers;
    }

    private String generateSignatureHeader(Map<String, Object> headers) {
        QsealcSigner signer =
                QsealcSignerImpl.build(
                        eidasProxyConfiguration.toInternalConfig(),
                        QsealcAlg.EIDAS_RSA_SHA256,
                        eidasIdentity);

        StringBuilder signedWithHeaderKeys = new StringBuilder();
        StringBuilder signedWithHeaderKeyValues = new StringBuilder();

        Arrays.stream(HEADERS_TO_SIGN.values())
                .map(HEADERS_TO_SIGN::getHeader)
                .filter(headers::containsKey)
                .forEach(
                        header -> {
                            signedWithHeaderKeyValues.append(
                                    String.format("%s: %s\n", header, headers.get(header)));
                            signedWithHeaderKeys.append(
                                    (signedWithHeaderKeys.length() == 0) ? header : " " + header);
                        });

        String signature =
                signer.getSignatureBase64(signedWithHeaderKeyValues.toString().trim().getBytes());

        String encodedSignature =
                Base64.getEncoder()
                        .encodeToString(
                                String.format(
                                                "keyId=\"%s\",algorithm=\"rsa-sha256\",headers=\"%s\",signature=\"%s\"",
                                                prepareSignatureHeaderKeyId(),
                                                signedWithHeaderKeys.toString(),
                                                signature)
                                        .getBytes(StandardCharsets.UTF_8));

        return String.format("=?utf-8?B?%s?=", encodedSignature);
    }

    private String prepareSignatureHeaderKeyId() {
        return String.format(
                "SN=%s,CA=%s",
                apiConfiguration.getCertificateSerialNumberInHex(),
                apiConfiguration.getCertificateIssuerDN());
    }
}
