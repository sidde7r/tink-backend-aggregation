package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.QueryParams;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.Signature;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.configuration.RabobankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc.TransactionalAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc.TransactionalTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.utils.RabobankUtils;
import se.tink.backend.aggregation.agents.utils.crypto.Certificate;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.EidasIdentity;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.CompositePaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.EmptyFinalPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.Form;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class RabobankApiClient {

    private static final Logger logger = LoggerFactory.getLogger(RabobankApiClient.class);

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final boolean requestIsManual;
    private final RabobankConfiguration rabobankConfiguration;
    private final EidasProxyConfiguration eidasProxyConf;
    private final EidasIdentity eidasIdentity;
    private final String qsealcPem;

    RabobankApiClient(
            final TinkHttpClient client,
            final PersistentStorage persistentStorage,
            final RabobankConfiguration rabobankConfiguration,
            final EidasProxyConfiguration eidasProxyConf,
            final EidasIdentity eidasIdentity,
            final boolean requestIsManual) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.rabobankConfiguration = rabobankConfiguration;
        this.eidasProxyConf = eidasProxyConf;
        this.eidasIdentity = eidasIdentity;
        this.requestIsManual = requestIsManual;

        this.qsealcPem = rabobankConfiguration.getQsealCert();

        client.addFilter(new AccessExceededFilter());
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

        debugPost(rabobankConfiguration.getUrls().getOauth2TokenUrl(), clientId, clientSecret);

        return client.request(rabobankConfiguration.getUrls().getOauth2TokenUrl())
                .body(request.serialize())
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .addBasicAuth(clientId, clientSecret)
                .post(TokenResponse.class);
    }

    // TODO Remove when MIYAG-737 is resolved
    private static void debugPost(final URL url, final String clientId, final String clientSecret) {

        if (clientId == null) {
            logger.info("Rabobank post MIYAG-737 : {} : client ID was missing", url);
        } else if (clientSecret == null) {
            logger.info("Rabobank post MIYAG-737 : {} : client secret was missing", url);
        } else {
            logger.info("Rabobank post MIYAG-737 : {} : client ID and secret are present", url);
        }
    }

    // TODO Remove when MIYAG-737 is resolved
    private static void debugBuildRequest(
            final URL url, final OAuth2Token oauth2Token, final PersistentStorage storage) {

        if (oauth2Token == null) {
            logger.info(
                    "Rabobank buildRequest MIYAG-737 : {} : Authorization token was NULL, persistentStorage was {}",
                    url,
                    storage);
        } else {
            logger.info(
                    "Rabobank buildRequest MIYAG-737 : {} : Authorization: {}",
                    url,
                    oauth2Token.toAuthorizeHeader());
        }
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

        debugBuildRequest(url, oAuth2Token, persistentStorage);

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

    public TransactionalAccountsResponse fetchAccounts() {
        final String digest = Base64.getEncoder().encodeToString(Hash.sha512(""));
        final String uuid = RabobankUtils.getRequestId();
        final String date = RabobankUtils.getDate();
        final String signatureHeader = buildSignatureHeader(digest, uuid, date);

        return buildRequest(
                        rabobankConfiguration.getUrls().getAisAccountsUrl(),
                        uuid,
                        digest,
                        signatureHeader,
                        date)
                .get(TransactionalAccountsResponse.class);
    }

    public BalanceResponse getBalance(final String accountId) {
        final String digest = Base64.getEncoder().encodeToString(Hash.sha512(""));
        final String uuid = RabobankUtils.getRequestId();
        final String date = RabobankUtils.getDate();
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

        // Order of booking statuses to try fetching transactions with.
        // If Rabobank ever supports booking status "both", we can prepend it to this list.
        final List<String> bookingStatuses = Collections.singletonList(QueryValues.BOOKED);

        for (final String bookingStatus : bookingStatuses) {
            try {
                return getTransactionsPages(url, fromDate, toDate, bookingStatus, isSandbox);
            } catch (HttpResponseException e) {
                final String message = e.getResponse().getBody(String.class);
                if (message.toLowerCase().contains(ErrorMessages.BOOKING_STATUS_INVALID)) {
                    logger.warn("Could not request with booking status \"{}\"", bookingStatus);
                    continue; // Try with some other booking status
                } else if (message.toLowerCase().contains(ErrorMessages.UNAVAILABLE_TRX_HISTORY)) {
                    logger.warn(message);
                    return new EmptyFinalPaginatorResponse();
                }

                throw new IllegalStateException(String.format("Unexpected error: %s", message));
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
        final String uuid = RabobankUtils.getRequestId();
        final String date = RabobankUtils.getDate();
        final String signatureHeader = buildSignatureHeader(digest, uuid, date);

        final Collection<PaginatorResponse> pages = new ArrayList<>();
        int currentPage = 1;
        TransactionalTransactionsResponse page;

        do {
            page =
                    buildRequest(url, uuid, digest, signatureHeader, date)
                            .queryParam(QueryParams.BOOKING_STATUS, bookingStatus)
                            .queryParam(QueryParams.DATE_FROM, sdf.format(fromDate))
                            .queryParam(QueryParams.DATE_TO, sdf.format(toDate))
                            .queryParam(QueryParams.SIZE, "" + QueryValues.TRANSACTIONS_SIZE)
                            .queryParam(QueryParams.PAGE, "" + currentPage)
                            .get(TransactionalTransactionsResponse.class);
            pages.add(page);
            currentPage++;
        } while (currentPage <= page.getLastPage() && !isSandbox);

        return new CompositePaginatorResponse(pages);
    }

    private String buildSignatureHeader(
            final String digest, final String requestId, final String date) {
        final String signingString = RabobankUtils.createSignatureString(date, digest, requestId);

        final String certificateId = rabobankConfiguration.getCertificateId();

        final byte[] signatureBytes =
                QsealcSigner.build(
                                eidasProxyConf.toInternalConfig(),
                                QsealcAlg.EIDAS_RSA_SHA256,
                                eidasIdentity,
                                certificateId)
                        .getSignature(signingString.getBytes());

        final String b64Signature = Base64.getEncoder().encodeToString(signatureBytes);
        final String clientCertSerial = extractQsealcSerial(qsealcPem);

        return RabobankUtils.createSignatureHeader(
                clientCertSerial, Signature.RSA_SHA_256, b64Signature, Signature.HEADERS_VALUE);
    }

    private static String extractQsealcSerial(final String qsealc) {
        return Certificate.getX509SerialNumber(qsealc);
    }
}
