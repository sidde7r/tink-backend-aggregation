package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.HEADERS_TO_SIGN;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator.rpc.ScaResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.configuration.SparebankApiConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.rpc.CardResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.rpc.CardTransactionResponse;
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

@RequiredArgsConstructor
public class SparebankApiClient {

    private final TinkHttpClient client;
    private final EidasProxyConfiguration eidasProxyConfiguration;
    private final EidasIdentity eidasIdentity;
    private final SparebankApiConfiguration apiConfiguration;
    private final SparebankStorage storage;

    private RequestBuilder createRequest(URL url) {
        return createRequest(url, Optional.empty());
    }

    private RequestBuilder createRequest(URL url, Optional<String> digest) {
        Map<String, Object> headers = getHeaders(UUID.randomUUID().toString(), digest);
        headers.put(HeaderKeys.SIGNATURE, generateSignatureHeader(headers));
        return client.request(url).headers(headers);
    }

    public ScaResponse getScaRedirect(String state) throws HttpResponseException {
        storage.storeState(state);
        return createRequest(new URL(apiConfiguration.getBaseUrl() + Urls.GET_SCA_REDIRECT))
                .post(ScaResponse.class);
    }

    public AccountResponse fetchAccounts() {
        Optional<AccountResponse> maybeAccounts = storage.getStoredAccounts();
        if (maybeAccounts.isPresent()) {
            return maybeAccounts.get();
        }
        AccountResponse accountResponse =
                createRequest(new URL(apiConfiguration.getBaseUrl() + Urls.GET_ACCOUNTS))
                        .queryParam(QueryKeys.WITH_BALANCE, "false")
                        .get(AccountResponse.class);
        storage.storeAccounts(accountResponse);
        return accountResponse;
    }

    public BalanceResponse fetchBalances(String resourceId) {
        Optional<BalanceResponse> maybeBalanceResponse =
                storage.getStoredBalanceResponse(resourceId);
        if (maybeBalanceResponse.isPresent()) {
            storage.removeBalanceResponseFromStorage(resourceId);
            return maybeBalanceResponse.get();
        }
        return createRequest(
                        new URL(apiConfiguration.getBaseUrl() + Urls.FETCH_BALANCES)
                                .parameter(IdTags.RESOURCE_ID, resourceId))
                .get(BalanceResponse.class);
    }

    public TransactionResponse fetchTransactions(String resourceId) {
        return fetchTransactions(Urls.FETCH_TRANSACTIONS, resourceId, TransactionResponse.class);
    }

    public TransactionResponse fetchNextTransactions(String path) {
        return createRequest(new URL(apiConfiguration.getBaseUrl() + path))
                .get(TransactionResponse.class);
    }

    public CardResponse fetchCards() {
        Optional<CardResponse> maybeCards = storage.getStoredCards();
        if (maybeCards.isPresent()) {
            return maybeCards.get();
        }
        CardResponse cardResponse =
                createRequest(new URL(apiConfiguration.getBaseUrl() + Urls.GET_CARDS))
                        .queryParam(QueryKeys.WITH_BALANCE, "false")
                        .header(HeaderKeys.X_ACCEPT_FIX, HeaderValues.X_ACCEPT_FIX)
                        .get(CardResponse.class);
        storage.storeCards(cardResponse);
        return cardResponse;
    }

    public BalanceResponse fetchCardBalances(String resourceId) {
        Optional<BalanceResponse> maybeBalanceResponse =
                storage.getStoredBalanceResponse(resourceId);
        if (maybeBalanceResponse.isPresent()) {
            storage.removeBalanceResponseFromStorage(resourceId);
            return maybeBalanceResponse.get();
        }
        return createRequest(
                        new URL(apiConfiguration.getBaseUrl() + Urls.GET_CARD_BALANCES)
                                .parameter(IdTags.RESOURCE_ID, resourceId))
                .get(BalanceResponse.class);
    }

    public CardTransactionResponse fetchCardTransactions(String resourceId) {
        return fetchTransactions(
                Urls.GET_CARD_TRANSACTIONS, resourceId, CardTransactionResponse.class);
    }

    public CardTransactionResponse fetchNextCardTransactions(String path) {
        return createRequest(new URL(apiConfiguration.getBaseUrl() + path))
                .get(CardTransactionResponse.class);
    }

    private <T> T fetchTransactions(
            String transactionUrl, String resourceId, Class<T> responseClass) {
        LocalDate fromDate;
        if (storage.isStoredConsentTooOldForFullFetch()) {
            fromDate = LocalDate.now().minusDays(89);
        } else {
            fromDate = LocalDate.of(1970, 1, 1);
        }
        LocalDate toDate = LocalDate.now();

        return createRequest(
                        new URL(apiConfiguration.getBaseUrl() + transactionUrl)
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
                .get(responseClass);
    }

    private Map<String, Object> getHeaders(String requestId, Optional<String> digest) {
        String tppRedirectUrl =
                new URL(apiConfiguration.getRedirectUrl())
                        .queryParam(QueryKeys.STATE, storage.getState())
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
        storage.getSessionId()
                .ifPresent(sessionId -> headers.put(HeaderKeys.TPP_SESSION_ID, sessionId));
        storage.getPsuId().ifPresent(psuId -> headers.put(HeaderKeys.PSU_ID, psuId));

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
