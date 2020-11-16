package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSignerImpl;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
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

public final class RabobankApiClient {

    private static final Logger logger = LoggerFactory.getLogger(RabobankApiClient.class);

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final boolean requestIsManual;
    private final RabobankConfiguration rabobankConfiguration;
    private final EidasProxyConfiguration eidasProxyConf;
    private final EidasIdentity eidasIdentity;
    private String qsealcPem;
    private String consentStatus;

    RabobankApiClient(
            final TinkHttpClient client,
            final PersistentStorage persistentStorage,
            final RabobankConfiguration rabobankConfiguration,
            final String qsealcPem,
            final EidasProxyConfiguration eidasProxyConf,
            final EidasIdentity eidasIdentity,
            final boolean requestIsManual) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.rabobankConfiguration = rabobankConfiguration;
        this.eidasProxyConf = eidasProxyConf;
        this.eidasIdentity = eidasIdentity;
        this.requestIsManual = requestIsManual;

        this.qsealcPem = qsealcPem;
    }

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
        final String clientCert = qsealcPem;
        final String digestHeader = Signature.SIGNING_STRING_SHA_512 + digest;

        final RequestBuilder builder;

        final OAuth2Token oAuth2Token = RabobankUtils.getOauthToken(persistentStorage);

        builder =
                client.request(url)
                        .addBearerToken(oAuth2Token)
                        .header(QueryParams.IBM_CLIENT_ID, clientId)
                        .header(QueryParams.TPP_SIGNATURE_CERTIFICATE, clientCert)
                        .header(QueryParams.REQUEST_ID, requestId)
                        .header(QueryParams.DIGEST, digestHeader)
                        .header(QueryParams.SIGNATURE, signatureHeader)
                        .header(QueryParams.DATE, date)
                        .accept(MediaType.APPLICATION_JSON_TYPE);

        // This header must be present iff the request was initiated by the PSU
        if (requestIsManual) {
            logger.info("Request is attended -- adding PSU header for {}", url);
            builder.header(QueryParams.PSU_IP_ADDRESS, QueryValues.PSU_IP_ADDRESS);
        } else {
            logger.info("Request is unattended -- omitting PSU header for {}", url);
        }

        return builder;
    }

    public void setConsentStatus() {
        final String consentId = persistentStorage.get(StorageKey.CONSENT_ID);
        if (consentId == null) {
            RabobankUtils.removeOauthToken(persistentStorage);
            throw SessionError.CONSENT_INVALID.exception("Missing consent id.");
        }

        final String digest = Base64.getEncoder().encodeToString(Hash.sha512(""));
        final String uuid = Psd2Headers.getRequestId();
        final String date =
                DateFormat.getFormattedCurrentDate(RabobankConstants.DATE_FORMAT, Zone.GMT);
        final String signatureHeader = buildSignatureHeader(digest, uuid, date);
        final URL url = rabobankConfiguration.getUrls().buildConsentUrl(consentId);

        ConsentDetailsResponse response = new ConsentDetailsResponse();
        try {
            response =
                    buildRequest(url, uuid, digest, signatureHeader, date)
                            .get(ConsentDetailsResponse.class);
        } catch (HttpResponseException e) {
            logger.warn(String.valueOf(e.getResponse()), e);
        }
        consentStatus = response.getStatus();
    }

    public void checkConsentStatus() {
        if (consentStatus == null) {
            setConsentStatus();
        }

        if (StringUtils.containsIgnoreCase(consentStatus, RabobankConstants.Consents.EXPIRE)) {
            RabobankUtils.removeOauthToken(persistentStorage);
            RabobankUtils.removeConsent(persistentStorage);
            throw SessionError.CONSENT_EXPIRED.exception(
                    ErrorMessages.ERROR_MESSAGE + consentStatus);
        } else if (StringUtils.containsIgnoreCase(
                consentStatus, RabobankConstants.Consents.INVALID)) {
            RabobankUtils.removeOauthToken(persistentStorage);
            RabobankUtils.removeConsent(persistentStorage);
            throw SessionError.CONSENT_INVALID.exception(
                    ErrorMessages.ERROR_MESSAGE + consentStatus);
        } else if (StringUtils.containsIgnoreCase(
                consentStatus, RabobankConstants.Consents.REVOKED_BY_USER)) {
            RabobankUtils.removeOauthToken(persistentStorage);
            RabobankUtils.removeConsent(persistentStorage);
            throw SessionError.CONSENT_REVOKED_BY_USER.exception(
                    ErrorMessages.ERROR_MESSAGE + consentStatus);
        } else {
            logger.debug("Consent status is " + consentStatus);
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
            if (e.getResponse().getStatus() == HttpStatus.SC_UNAUTHORIZED
                    && errorResponse
                            .getMoreInformation()
                            .trim()
                            .equalsIgnoreCase(ErrorMessages.NOT_SUBSCRIBED)) {
                rabobankConfiguration.getUrls().setConsumeLatest(false);
                return buildFetchAccountsRequest().get(TransactionalAccountsResponse.class);
            }
            throw e;
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
        final String accountId = account.getFromTemporaryStorage(StorageKey.RESOURCE_ID);
        final URL url = rabobankConfiguration.getUrls().buildTransactionsUrl(accountId);

        // Stop fetching if fromDate is older than 8 years from current (Rabobank specification).
        int years = -8;
        if (fromDate.before(getPreviousYearsDate(years))) {
            return new EmptyFinalPaginatorResponse();
        }
        // Order of booking statuses to try fetching transactions with.
        // If Rabobank ever supports booking status "both", we can prepend it to this list.
        final List<String> bookingStatuses = Collections.singletonList(QueryValues.BOOKED);

        for (final String bookingStatus : bookingStatuses) {
            try {
                return getTransactionsPages(url, fromDate, toDate, bookingStatus, isSandbox);
            } catch (HttpResponseException e) {
                final String message = e.getResponse().getBody(String.class);
                if (message.toLowerCase().contains(ErrorMessages.BOOKING_STATUS_INVALID)) {
                    logger.warn("Could not request with booking status \"{}\"", bookingStatus, e);
                    continue; // Try with some other booking status
                } else if (message.toLowerCase().contains(ErrorMessages.UNAVAILABLE_TRX_HISTORY)) {
                    logger.warn(message, e);
                    return new EmptyFinalPaginatorResponse();
                }
                throw new IllegalStateException(String.format("Unexpected error: %s", message), e);
            }
        }
        throw new IllegalStateException("Failed to fetch transactions");
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
        while (page.getTransactions().getLinks().getNextKey() != null && !isSandbox) {
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

    private String buildSignatureHeader(
            final String digest, final String requestId, final String date) {
        final String signingString = RabobankUtils.createSignatureString(date, digest, requestId);
        final byte[] signatureBytes =
                QsealcSignerImpl.build(
                                eidasProxyConf.toInternalConfig(),
                                QsealcAlg.EIDAS_RSA_SHA256,
                                eidasIdentity)
                        .getSignature(signingString.getBytes());

        final String b64Signature = Base64.getEncoder().encodeToString(signatureBytes);
        final String clientCertSerial = extractQsealcSerial(qsealcPem);

        return RabobankUtils.createSignatureHeader(
                clientCertSerial, Signature.RSA_SHA_256, b64Signature, Signature.HEADERS_VALUE);
    }

    private static String extractQsealcSerial(final String qsealc) {
        return Certificate.getX509SerialNumber(qsealc);
    }

    private Date getPreviousYearsDate(int years) {
        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();
        cal.add(Calendar.YEAR, years);
        return cal.getTime();
    }
}
