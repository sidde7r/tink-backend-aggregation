package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.NoSuchElementException;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.agent.sdk.operation.User;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.QueryParams;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.Signature;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.rpc.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.configuration.RabobankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc.TransactionalAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc.TransactionalTransactionsResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.CompositePaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.cryptography.hash.Hash;
import se.tink.libraries.date.DateFormat;
import se.tink.libraries.date.DateFormat.Zone;

@Slf4j
@RequiredArgsConstructor
public final class RabobankApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final RabobankConfiguration rabobankConfiguration;
    private final RabobankSignatureHeaderBuilder signatureHeaderBuilder;
    private final User user;

    public TokenResponse exchangeAuthorizationCode(Form request) {
        return post(request);
    }

    public TokenResponse refreshAccessToken(Form request) {
        return post(request);
    }

    public TransactionalAccountsResponse fetchAccounts() {
        try {
            return buildFetchAccountsRequest().get(TransactionalAccountsResponse.class);
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();
            checkErrorBodyType(response);

            ErrorResponse errorResponse = response.getBody(ErrorResponse.class);
            if (errorResponse.isNotSubscribedError(response.getStatus())) {
                rabobankConfiguration.getUrls().setConsumeLatest(false);
                return buildFetchAccountsRequest().get(TransactionalAccountsResponse.class);
            }
            throw BankServiceError.BANK_SIDE_FAILURE.exception(e.getMessage());
        }
    }

    public BalanceResponse getBalance(String accountId) {
        String digest = Base64.getEncoder().encodeToString(Hash.sha512(""));
        String uuid = Psd2Headers.getRequestId();
        String date = DateFormat.getFormattedCurrentDate(RabobankConstants.DATE_FORMAT, Zone.GMT);
        String signatureHeader = signatureHeaderBuilder.buildSignatureHeader(digest, uuid, date);
        URL url = rabobankConfiguration.getUrls().buildBalanceUrl(accountId);

        return buildRequest(url, uuid, digest, signatureHeader, date).get(BalanceResponse.class);
    }

    public PaginatorResponse getTransactions(
            TransactionalAccount account, Date fromDate, Date toDate, boolean isSandbox) {

        URL url = rabobankConfiguration.getUrls().buildTransactionsUrl(account.getApiIdentifier());
        try {
            return getTransactionsPages(url, fromDate, toDate, QueryValues.BOOKED, isSandbox);
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();
            checkErrorBodyType(response);

            ErrorResponse errorResponse = response.getBody(ErrorResponse.class);
            if (errorResponse.isPeriodInvalidError()) {
                return PaginatorResponseImpl.createEmptyFinal();
            }
            throw BankServiceError.BANK_SIDE_FAILURE.exception(e.getMessage());
        }
    }

    ConsentDetailsResponse getConsentStatus(
            URL consentUrl, String uuid, String digest, String signatureHeader, String date) {
        return buildRequest(consentUrl, uuid, digest, signatureHeader, date)
                .get(ConsentDetailsResponse.class);
    }

    private TokenResponse post(Form request) {
        String clientId = rabobankConfiguration.getClientId();
        String clientSecret = rabobankConfiguration.getClientSecret();

        return client.request(rabobankConfiguration.getUrls().getOauth2TokenUrl())
                .body(request.serialize())
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .addBasicAuth(clientId, clientSecret)
                .post(TokenResponse.class);
    }

    private RequestBuilder buildRequest(
            URL url, String requestId, String digest, String signatureHeader, String date) {
        String clientId = rabobankConfiguration.getClientId();
        String digestHeader = Signature.SIGNING_STRING_SHA_512 + digest;

        RequestBuilder builder;

        OAuth2Token oAuth2Token = getOauthToken();

        builder =
                client.request(url)
                        .addBearerToken(oAuth2Token)
                        .header(QueryParams.IBM_CLIENT_ID, clientId)
                        .header(
                                QueryParams.TPP_SIGNATURE_CERTIFICATE,
                                signatureHeaderBuilder.getQsealcPem())
                        .header(QueryParams.REQUEST_ID, requestId)
                        .header(QueryParams.DIGEST, digestHeader)
                        .header(QueryParams.SIGNATURE, signatureHeader)
                        .header(QueryParams.DATE, date)
                        .accept(MediaType.APPLICATION_JSON_TYPE);

        // This header must be present iff the request was initiated by the PSU
        if (user.isPresent()) {
            log.info("Request is attended -- adding PSU header for {}", url);
            builder.header(QueryParams.PSU_IP_ADDRESS, user.getIpAddress());
        } else {
            log.info("Request is unattended -- omitting PSU header for {}", url);
        }

        return builder;
    }

    private RequestBuilder buildFetchAccountsRequest() {
        String digest = Base64.getEncoder().encodeToString(Hash.sha512(""));
        String uuid = Psd2Headers.getRequestId();
        String date = DateFormat.getFormattedCurrentDate(RabobankConstants.DATE_FORMAT, Zone.GMT);
        String signatureHeader = signatureHeaderBuilder.buildSignatureHeader(digest, uuid, date);

        return buildRequest(
                rabobankConfiguration.getUrls().getAisAccountsUrl(),
                uuid,
                digest,
                signatureHeader,
                date);
    }

    private PaginatorResponse getTransactionsPages(
            URL url, Date fromDate, Date toDate, String bookingStatus, boolean isSandbox) {

        SimpleDateFormat sdf = new SimpleDateFormat(RabobankConstants.TRANSACTION_DATE_FORMAT);
        String digest = Base64.getEncoder().encodeToString(Hash.sha512(""));
        String uuid = Psd2Headers.getRequestId();
        String date = DateFormat.getFormattedCurrentDate(RabobankConstants.DATE_FORMAT, Zone.GMT);
        String signatureHeader = signatureHeaderBuilder.buildSignatureHeader(digest, uuid, date);

        Collection<PaginatorResponse> pages = new ArrayList<>();
        TransactionalTransactionsResponse page =
                buildRequest(url, uuid, digest, signatureHeader, date)
                        .queryParam(QueryParams.BOOKING_STATUS, bookingStatus)
                        .queryParam(QueryParams.DATE_FROM, sdf.format(fromDate))
                        .queryParam(QueryParams.DATE_TO, sdf.format(toDate))
                        .queryParam(QueryParams.SIZE, "" + QueryValues.TRANSACTIONS_SIZE)
                        .get(TransactionalTransactionsResponse.class);

        pages.add(page);
        while (shouldFetchMoreTransactions(isSandbox, page)) {
            page =
                    buildRequest(
                                    new URL(
                                            rabobankConfiguration
                                                            .getUrls()
                                                            .buildNextTransactionBaseUrl()
                                                    + page.getTransactions()
                                                            .getLinks()
                                                            .getNextKey()),
                                    uuid,
                                    digest,
                                    signatureHeader,
                                    date)
                            .get(TransactionalTransactionsResponse.class);
            pages.add(page);
        }

        return new CompositePaginatorResponse(pages);
    }

    private boolean shouldFetchMoreTransactions(
            boolean isSandbox, TransactionalTransactionsResponse page) {
        return page.getTransactions().getLinks().getNextKey() != null
                && !isSandbox
                && user.isPresent();
    }

    private void checkErrorBodyType(HttpResponse httpResponse) {
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(httpResponse.getType())) {
            log.info("Invalid response body: {}", httpResponse.getBody(String.class));
            throw BankServiceError.BANK_SIDE_FAILURE.exception("Incorrect error body response.");
        }
    }

    private OAuth2Token getOauthToken() {
        return persistentStorage
                .get(StorageKey.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new NoSuchElementException("Missing Oauth token!"));
    }
}
