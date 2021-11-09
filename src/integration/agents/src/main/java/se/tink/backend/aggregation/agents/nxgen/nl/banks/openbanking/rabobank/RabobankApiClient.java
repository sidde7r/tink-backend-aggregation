package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.ErrorMessages;
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
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.utils.RabobankUtils;
import se.tink.backend.aggregation.agents.utils.crypto.Certificate;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.CompositePaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.EmptyFinalPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.DateFormat;
import se.tink.libraries.date.DateFormat.Zone;

@Slf4j
@RequiredArgsConstructor
public final class RabobankApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final RabobankConfiguration rabobankConfiguration;
    private final String qsealcPem;
    private final QsealcSigner qsealcSigner;
    private final RabobankUserIpInformation userIpInformation;
    private final AgentComponentProvider componentProvider;
    private String consentStatus;

    public TokenResponse exchangeAuthorizationCode(final Form request) {
        return post(request);
    }

    public TokenResponse refreshAccessToken(final Form request) {
        return post(request);
    }

    private TokenResponse post(final Form request) {
        final String clientId = rabobankConfiguration.getClientId();
        final String clientSecret = rabobankConfiguration.getClientSecret();

        return client.request(rabobankConfiguration.getUrls().getOauth2TokenUrl())
                .body(request.serialize())
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .addBasicAuth(clientId, clientSecret)
                .post(TokenResponse.class);
    }

    private RequestBuilder buildRequest(
            final URL url,
            final String requestId,
            final String digest,
            final String signatureHeader,
            final String date) {
        final String clientId = rabobankConfiguration.getClientId();
        final String digestHeader = Signature.SIGNING_STRING_SHA_512 + digest;

        final RequestBuilder builder;

        final OAuth2Token oAuth2Token = RabobankUtils.getOauthToken(persistentStorage);

        builder =
                client.request(url)
                        .addBearerToken(oAuth2Token)
                        .header(QueryParams.IBM_CLIENT_ID, clientId)
                        .header(QueryParams.TPP_SIGNATURE_CERTIFICATE, qsealcPem)
                        .header(QueryParams.REQUEST_ID, requestId)
                        .header(QueryParams.DIGEST, digestHeader)
                        .header(QueryParams.SIGNATURE, signatureHeader)
                        .header(QueryParams.DATE, date)
                        .accept(MediaType.APPLICATION_JSON_TYPE);

        // This header must be present iff the request was initiated by the PSU
        if (userIpInformation.isUserPresent()) {
            log.info("Request is attended -- adding PSU header for {}", url);
            builder.header(QueryParams.PSU_IP_ADDRESS, userIpInformation.getUserIp());
        } else {
            log.info("Request is unattended -- omitting PSU header for {}", url);
        }

        return builder;
    }

    public void setConsentStatus() {
        final String consentId = persistentStorage.get(StorageKey.CONSENT_ID);

        throwSessionErrorIfConsentIsNull(consentId);

        final String digest = Base64.getEncoder().encodeToString(Hash.sha512(""));
        final String uuid = Psd2Headers.getRequestId();
        final String date =
                DateFormat.getFormattedCurrentDate(RabobankConstants.DATE_FORMAT, Zone.GMT);
        final String signatureHeader = buildSignatureHeader(digest, uuid, date);
        final URL url = rabobankConfiguration.getUrls().buildConsentUrl(consentId);

        ConsentDetailsResponse response;
        try {
            response =
                    buildRequest(url, uuid, digest, signatureHeader, date)
                            .get(ConsentDetailsResponse.class);
        } catch (HttpResponseException e) {
            // Invalid/Revoked Consent response received code 200. Other than that we throw TE
            throw BankServiceError.BANK_SIDE_FAILURE.exception(String.valueOf(e.getResponse()));
        }
        consentStatus = response.getStatus();
    }

    public void checkConsentStatus() {
        fetchNewConsentIfEmpty();

        if (RabobankConstants.Consents.STATUS.stream()
                .anyMatch(s -> s.equalsIgnoreCase(consentStatus))) {
            RabobankUtils.removeOauthToken(persistentStorage);
            RabobankUtils.removeConsent(persistentStorage);
            throw SessionError.CONSENT_EXPIRED.exception(
                    ErrorMessages.ERROR_MESSAGE + consentStatus);
        } else {
            log.debug("Consent status is " + consentStatus);
        }
    }

    private RequestBuilder buildFetchAccountsRequest() {
        final String digest = Base64.getEncoder().encodeToString(Hash.sha512(""));
        final String uuid = Psd2Headers.getRequestId();
        final String date =
                DateFormat.getFormattedCurrentDate(RabobankConstants.DATE_FORMAT, Zone.GMT);
        final String signatureHeader = buildSignatureHeader(digest, uuid, date);

        return buildRequest(
                rabobankConfiguration.getUrls().getAisAccountsUrl(),
                uuid,
                digest,
                signatureHeader,
                date);
    }

    public TransactionalAccountsResponse fetchAccounts() {
        try {
            return buildFetchAccountsRequest().get(TransactionalAccountsResponse.class);
        } catch (HttpResponseException e) {
            ErrorResponse errorResponse = e.getResponse().getBody(ErrorResponse.class);
            if (errorResponse.isNotSubscribedError(e.getResponse().getStatus())) {
                rabobankConfiguration.getUrls().setConsumeLatest(false);
                return buildFetchAccountsRequest().get(TransactionalAccountsResponse.class);
            }
            throw BankServiceError.BANK_SIDE_FAILURE.exception(e.getMessage());
        }
    }

    public BalanceResponse getBalance(final String accountId) {
        final String digest = Base64.getEncoder().encodeToString(Hash.sha512(""));
        final String uuid = Psd2Headers.getRequestId();
        final String date =
                DateFormat.getFormattedCurrentDate(RabobankConstants.DATE_FORMAT, Zone.GMT);
        final String signatureHeader = buildSignatureHeader(digest, uuid, date);
        final URL url = rabobankConfiguration.getUrls().buildBalanceUrl(accountId);

        return buildRequest(url, uuid, digest, signatureHeader, date).get(BalanceResponse.class);
    }

    public PaginatorResponse getTransactions(
            final TransactionalAccount account,
            final Date fromDate,
            final Date toDate,
            final boolean isSandbox) {

        final URL url =
                rabobankConfiguration.getUrls().buildTransactionsUrl(account.getApiIdentifier());
        try {
            return getTransactionsPages(url, fromDate, toDate, QueryValues.BOOKED, isSandbox);
        } catch (HttpResponseException e) {
            ErrorResponse errorResponse = new ErrorResponse();
            if (errorResponse.isPeriodInvalidError(e)) {
                return new EmptyFinalPaginatorResponse();
            }
            throw BankServiceError.BANK_SIDE_FAILURE.exception(e.getMessage());
        }
    }

    private PaginatorResponse getTransactionsPages(
            final URL url,
            final Date fromDate,
            final Date toDate,
            final String bookingStatus,
            final boolean isSandbox) {

        final SimpleDateFormat sdf =
                new SimpleDateFormat(RabobankConstants.TRANSACTION_DATE_FORMAT);
        final String digest = Base64.getEncoder().encodeToString(Hash.sha512(""));
        final String uuid = Psd2Headers.getRequestId();
        final String date =
                DateFormat.getFormattedCurrentDate(RabobankConstants.DATE_FORMAT, Zone.GMT);
        final String signatureHeader = buildSignatureHeader(digest, uuid, date);

        final Collection<PaginatorResponse> pages = new ArrayList<>();
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
                && userIpInformation.isUserPresent();
    }

    private String buildSignatureHeader(
            final String digest, final String requestId, final String date) {
        final String signingString = RabobankUtils.createSignatureString(date, digest, requestId);
        final byte[] signatureBytes = qsealcSigner.getSignature(signingString.getBytes());

        final String b64Signature = Base64.getEncoder().encodeToString(signatureBytes);
        final String clientCertSerial = extractQsealcSerial(qsealcPem);

        return RabobankUtils.createSignatureHeader(
                clientCertSerial, Signature.RSA_SHA_256, b64Signature, Signature.HEADERS_VALUE);
    }

    private static String extractQsealcSerial(final String qsealc) {
        return Certificate.getX509SerialNumber(qsealc);
    }

    private void throwSessionErrorIfConsentIsNull(String consentId) {
        if (Strings.isNullOrEmpty(consentId)) {
            RabobankUtils.removeOauthToken(persistentStorage);
            throw SessionError.CONSENT_INVALID.exception("Missing consent id.");
        }
    }

    private void fetchNewConsentIfEmpty() {
        if (consentStatus == null) {
            setConsentStatus();
        }
    }
}
