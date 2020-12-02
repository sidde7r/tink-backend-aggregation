package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.checkingaccount.rpc.AccountListResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.creditcardaccount.rpc.FetchCreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.creditcardaccount.rpc.GetCardResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.creditcardaccount.rpc.ListCardResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.authenticator.rpc.CollectChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.authenticator.rpc.InitiateBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.authenticator.rpc.InstrumentInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.investmentfetcher.rpc.FetchFundDetailResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.investmentfetcher.rpc.FetchFundsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.investmentfetcher.rpc.FetchPensionResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.oauth.DnbOAuthEncoder;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.oauth.DnbOAuthHeaderFormatter;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.oauth.DnbSignDataUrlEncoder;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.oauth.DnbTimestampUtil;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.oauth.rpc.InitMyWealthResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@Slf4j
public class DnbApiClient {

    public static final String TRUE = "TRUE";
    private static final Pattern ISIN = Pattern.compile("ISIN:\\s*([A-Za-z0-9]{11}[0-9])");
    private final TinkHttpClient client;
    private final DnbTimestampUtil timestampUtil;

    public DnbApiClient(TinkHttpClient client) {
        this.client = client;
        this.timestampUtil = new DnbTimestampUtil();
    }

    public void getStartMobile() {
        this.client
                .request(DnbConstants.Url.INIT_LOGIN)
                .header(DnbConstants.Header.ORIGIN, DnbConstants.Url.BASE_URL)
                .header(DnbConstants.Header.REFERER, DnbConstants.Url.INIT_LOGIN)
                .get(HttpResponse.class);
    }

    public HttpResponse postStartMobile(String ssn) {
        MultivaluedMapImpl startMobileParameters = new MultivaluedMapImpl();
        startMobileParameters.add(DnbConstants.PostParameter.SSN, ssn);
        startMobileParameters.add(DnbConstants.PostParameter.START_PAGE, "");
        startMobileParameters.add(DnbConstants.PostParameter.USER_CONTEXT, "");

        return this.client
                .request(DnbConstants.Url.INIT_LOGIN)
                .header(DnbConstants.Header.ORIGIN, DnbConstants.Url.BASE_URL)
                .header(DnbConstants.Header.REFERER, DnbConstants.Url.INIT_LOGIN)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_ATOM_XML_TYPE)
                .post(HttpResponse.class, startMobileParameters);
    }

    public void initiateSession(URI uri) {
        this.client
                .request(uri.toString())
                .header(DnbConstants.Header.ORIGIN, DnbConstants.Url.BASE_URL)
                .header(DnbConstants.Header.REFERER, DnbConstants.Url.INIT_LOGIN)
                .accept(MediaType.WILDCARD)
                .get(HttpResponse.class);
    }

    public InstrumentInfoResponse getInstrumentInfo(URI referer) {
        return this.client
                .request(DnbConstants.Url.INSTRUMENT_INFO)
                .header(DnbConstants.Header.REFERER, referer)
                .header(
                        DnbConstants.Header.REQUEST_WITH_KEY,
                        DnbConstants.Header.REQUEST_WITH_VALUE)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .get(InstrumentInfoResponse.class);
    }

    public InitiateBankIdResponse getInitiateBankId(URI referer) {
        return this.client
                .request(DnbConstants.Url.INIT_BANKID)
                .queryParam(DnbConstants.QueryParam.COOKIE_SUPPORT, TRUE)
                .queryParam(
                        DnbConstants.QueryParam.PREVENT_CACHE, String.valueOf(new Date().getTime()))
                .header(DnbConstants.Header.REFERER, referer)
                .header(
                        DnbConstants.Header.REQUEST_WITH_KEY,
                        DnbConstants.Header.REQUEST_WITH_VALUE)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .get(InitiateBankIdResponse.class);
    }

    public CollectChallengeResponse postCollectChallenge(URI referer, String mobileNumber) {
        MultivaluedMapImpl startMobileParameters = new MultivaluedMapImpl();
        startMobileParameters.add(DnbConstants.PostParameter.PHONE_NUMBER, mobileNumber);

        return this.client
                .request(DnbConstants.Url.CHALLENGE)
                .queryParam(
                        DnbConstants.QueryParam.PREVENT_CACHE, String.valueOf(new Date().getTime()))
                .header(DnbConstants.Header.ORIGIN, DnbConstants.Url.BASE_URL)
                .header(DnbConstants.Header.REFERER, referer)
                .header(
                        DnbConstants.Header.REQUEST_WITH_KEY,
                        DnbConstants.Header.REQUEST_WITH_VALUE)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(CollectChallengeResponse.class, startMobileParameters);
    }

    public CollectChallengeResponse getCollectBankId(URI referer) {
        return this.client
                .request(DnbConstants.Url.COLLECT_BANKID)
                .queryParam(
                        DnbConstants.QueryParam.PREVENT_CACHE, String.valueOf(new Date().getTime()))
                .header(DnbConstants.Header.REFERER, referer)
                .header(
                        DnbConstants.Header.REQUEST_WITH_KEY,
                        DnbConstants.Header.REQUEST_WITH_VALUE)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .get(CollectChallengeResponse.class);
    }

    public void getFinalizeLogon(URI referer) {
        this.client
                .request(DnbConstants.Url.FINALIZE_LOGON)
                .header(DnbConstants.Header.REFERER, referer)
                .get(HttpResponse.class);
    }

    public void getFirstRequestAfterLogon(URI referer) {
        this.client
                .request(DnbConstants.Url.FIRST_REQUEST)
                .header(DnbConstants.Header.REFERER, referer)
                .get(HttpResponse.class);
    }

    public AccountListResponse fetchAccounts() {
        return this.client
                .request(DnbConstants.Url.FETCH_ACCOUNT_DETAILS)
                .queryParam(
                        DnbConstants.QueryParam.PREVENT_CACHE, String.valueOf(new Date().getTime()))
                .header(
                        DnbConstants.Header.REQUEST_WITH_KEY,
                        DnbConstants.Header.REQUEST_WITH_VALUE)
                .get(AccountListResponse.class);
    }

    public HttpResponse fetchTransactions(Account account, int count) {
        return this.client
                .request(
                        String.format(
                                DnbConstants.Url.FETCH_TRANSACTIONS, account.getAccountNumber()))
                .queryParam(DnbConstants.QueryParam.COUNT, String.valueOf(count))
                .queryParam(
                        DnbConstants.QueryParam.PREVENT_CACHE, String.valueOf(new Date().getTime()))
                .header(
                        DnbConstants.Header.REQUEST_WITH_KEY,
                        DnbConstants.Header.REQUEST_WITH_VALUE)
                .get(HttpResponse.class);
    }

    ////// Credit Cards Begin //////
    public ListCardResponse listCards() {
        HttpResponse httpResponse =
                this.client
                        .request(DnbConstants.Url.LIST_CARDS)
                        .queryParam(
                                DnbConstants.QueryParam.PREVENT_CACHE,
                                String.valueOf(new Date().getTime()))
                        .header(
                                DnbConstants.Header.REQUEST_WITH_KEY,
                                DnbConstants.Header.REQUEST_WITH_VALUE)
                        .get(HttpResponse.class);

        if (httpResponse.getStatus() == 302) {
            // Known redirects are handled by DnbRedirectFilter
            log.warn(
                    "Card list response was a redirect, that we don't handle, with location "
                            + httpResponse.getHeaders().getFirst("Location"));
            return new ListCardResponse();
        }

        return httpResponse.getBody(ListCardResponse.class);
    }

    public GetCardResponse getCard(String cardId) {
        try {
            return this.client
                    .request(
                            String.format(
                                    DnbConstants.Url.GET_CARD,
                                    URLEncoder.encode(cardId, DnbConstants.CHARSET)))
                    .queryParam(
                            DnbConstants.QueryParam.PREVENT_CACHE,
                            String.valueOf(new Date().getTime()))
                    .header(
                            DnbConstants.Header.REQUEST_WITH_KEY,
                            DnbConstants.Header.REQUEST_WITH_VALUE)
                    .get(GetCardResponse.class);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Charset not found while encoding string: UTF-8", e);
        }
    }

    public FetchCreditCardTransactionsResponse fetchCreditCardTransactions(Account account) {

        String cardId = account.getApiIdentifier();

        if (cardId == null) {
            throw new IllegalStateException("No matching card Id found");
        }

        try {
            return this.client
                    .request(
                            String.format(
                                    DnbConstants.Url.FETCH_CARD_TRANSACTIONS,
                                    URLEncoder.encode(cardId, DnbConstants.CHARSET)))
                    .queryParam(
                            DnbConstants.QueryParam.PREVENT_CACHE,
                            String.valueOf(new Date().getTime()))
                    .header(
                            DnbConstants.Header.REQUEST_WITH_KEY,
                            DnbConstants.Header.REQUEST_WITH_VALUE)
                    .get(FetchCreditCardTransactionsResponse.class);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Charset not found while encoding string: UTF-8", e);
        }
    }
    ////// Credit Cards End   //////

    ////// OAuth Begin ///////
    public String oauthGetRequestToken() {
        try {
            // The order here matters as it composes the signing data.
            DnbSignDataUrlEncoder encoder =
                    new DnbSignDataUrlEncoder(
                            DnbConstants.Header.METHOD_POST, DnbConstants.Url.GET_REQUEST_TOKEN);
            String timestamp = this.timestampUtil.getTimestampInSeconds();
            String nonce = this.timestampUtil.getNonce();
            encoder.putPair(
                    DnbConstants.Header.OAUTH_CALLBACK_KEY,
                    DnbConstants.Header.OAUTH_CALLBACK_VALUE_ANDROID);
            encoder.putPair(
                    DnbConstants.Header.OAUTH_CONSUMER_KEY_KEY,
                    DnbOAuthEncoder.encode(DnbConstants.Header.OAUTH_CONSUMER_KEY_VALUE));
            encoder.putPair(DnbConstants.Header.OAUTH_NONCE_KEY, nonce);
            encoder.putPair(
                    DnbConstants.Header.OAUTH_SIGNATURE_METHOD_KEY,
                    DnbConstants.Header.OAUTH_SIGNATURE_METHOD_VALUE);
            encoder.putPair(DnbConstants.Header.OAUTH_TIMESTAMP_KEY, timestamp);
            encoder.putPair(
                    DnbConstants.Header.OAUTH_VERSION_KEY, DnbConstants.Header.OAUTH_VERSION_VALUE);

            String signData = encoder.toString();

            // xiacheng NOTE: key is a combination of api secret "&" with token secret, here token
            // secret is "".
            String signature = this.doSign(signData, DnbConstants.OAuth.DNB_API_SECRET + "&");

            DnbOAuthHeaderFormatter oAuthHeaderFormatter = new DnbOAuthHeaderFormatter();
            oAuthHeaderFormatter.putPair(
                    DnbConstants.Header.OAUTH_SIGNATURE_METHOD_KEY,
                    DnbConstants.Header.OAUTH_SIGNATURE_METHOD_VALUE);
            oAuthHeaderFormatter.putPair(
                    DnbConstants.Header.OAUTH_CONSUMER_KEY_KEY,
                    DnbOAuthEncoder.encode(DnbConstants.Header.OAUTH_CONSUMER_KEY_VALUE));
            oAuthHeaderFormatter.putPair(
                    DnbConstants.Header.OAUTH_VERSION_KEY, DnbConstants.Header.OAUTH_VERSION_VALUE);
            oAuthHeaderFormatter.putPair(DnbConstants.Header.OAUTH_TIMESTAMP_KEY, timestamp);
            oAuthHeaderFormatter.putPair(DnbConstants.Header.OAUTH_NONCE_KEY, nonce);
            oAuthHeaderFormatter.putPair(
                    DnbConstants.Header.OAUTH_CALLBACK_KEY,
                    DnbConstants.Header.OAUTH_CALLBACK_VALUE_ANDROID);
            oAuthHeaderFormatter.putPair(
                    DnbConstants.Header.OAUTH_SIGNATURE_KEY, DnbOAuthEncoder.encode(signature));
            return this.client
                    .request(DnbConstants.Url.GET_REQUEST_TOKEN)
                    .header(
                            DnbConstants.Header.AUTHORIZATION_HEADER,
                            oAuthHeaderFormatter.toString())
                    .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                    .post(String.class, "");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unsupported encoding exception!", e);
        }
    }

    public String oauthVerifierService(String oauthToken) {
        return this.client
                .request(DnbConstants.Url.VERIFIER_SERVICE)
                .queryParam(DnbConstants.Header.OAUTH_TOKEN_KEY, oauthToken)
                .queryParam(
                        DnbConstants.Header.FRIENDLY_NAME_KEY,
                        DnbConstants.Header.FRIENDLY_NAME_VALUE)
                .post(String.class, "");
    }

    public String oauthGetAccessToken(String oauthToken, String oauthVerifier, String oauthSecret) {
        try {
            // The order here matters as it composes the signing data.
            DnbSignDataUrlEncoder encoder =
                    new DnbSignDataUrlEncoder(
                            DnbConstants.Header.METHOD_POST, DnbConstants.Url.GET_ACCESS_TOKEN);
            String timestamp = this.timestampUtil.getTimestampInSeconds();
            String nonce = this.timestampUtil.getNonce();
            fillEncoderData(nonce, timestamp, oauthToken, encoder);
            String signData = encoder.toString();

            // xiacheng NOTE: key is a combination of api secret "&" with tokensecret
            String signature =
                    this.doSign(signData, DnbConstants.OAuth.DNB_API_SECRET + "&" + oauthSecret);

            DnbOAuthHeaderFormatter oAuthHeaderFormatter =
                    getAndFillHeaderFormatter(timestamp, nonce, oauthToken, signature);
            oAuthHeaderFormatter.putPair(DnbConstants.Header.OAUTH_VERIFIER_KEY, oauthVerifier);

            return this.createOAuthRequest(
                            DnbConstants.Url.GET_ACCESS_TOKEN, oAuthHeaderFormatter.toString())
                    .post(String.class, "");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unsupported encoding exception!", e);
        }
    }

    private DnbOAuthHeaderFormatter getAndFillHeaderFormatter(
            String timestamp, String nonce, String oauthToken, String signature)
            throws UnsupportedEncodingException {
        DnbOAuthHeaderFormatter oAuthHeaderFormatter = new DnbOAuthHeaderFormatter();
        oAuthHeaderFormatter.putPair(
                DnbConstants.Header.OAUTH_SIGNATURE_METHOD_KEY,
                DnbConstants.Header.OAUTH_SIGNATURE_METHOD_VALUE);
        oAuthHeaderFormatter.putPair(
                DnbConstants.Header.OAUTH_CONSUMER_KEY_KEY,
                DnbOAuthEncoder.encode(DnbConstants.Header.OAUTH_CONSUMER_KEY_VALUE));
        oAuthHeaderFormatter.putPair(
                DnbConstants.Header.OAUTH_VERSION_KEY, DnbConstants.Header.OAUTH_VERSION_VALUE);
        oAuthHeaderFormatter.putPair(DnbConstants.Header.OAUTH_TIMESTAMP_KEY, timestamp);
        oAuthHeaderFormatter.putPair(DnbConstants.Header.OAUTH_NONCE_KEY, nonce);
        oAuthHeaderFormatter.putPair(DnbConstants.Header.OAUTH_TOKEN_KEY, oauthToken);
        oAuthHeaderFormatter.putPair(
                DnbConstants.Header.OAUTH_SIGNATURE_KEY, DnbOAuthEncoder.encode(signature));
        return oAuthHeaderFormatter;
    }

    ////// OAuth End ///////

    //// Spare App (Investment) Begin //////
    public InitMyWealthResponse initMyWealth(String oauthToken, String oauthSecret) {
        String timestamp = this.timestampUtil.getTimestampInSeconds();
        String nonce = this.timestampUtil.getNonce();
        String signature =
                this.getGenericSignature(
                        DnbConstants.Header.METHOD_GET,
                        DnbConstants.Url.INIT_MY_WEALTH,
                        nonce,
                        timestamp,
                        oauthToken,
                        oauthSecret);
        String authorizationHeader =
                this.formGenericAuthorizationHeader(timestamp, nonce, oauthToken, signature);

        return this.createOAuthRequest(DnbConstants.Url.INIT_MY_WEALTH, authorizationHeader)
                .get(InitMyWealthResponse.class);
    }

    public FetchPensionResponse fetchPension(String oauthToken, String oauthSecret) {
        String timestamp = this.timestampUtil.getTimestampInSeconds();
        String nonce = this.timestampUtil.getNonce();
        String signature =
                this.getGenericSignature(
                        DnbConstants.Header.METHOD_GET,
                        DnbConstants.Url.GET_PENSION,
                        nonce,
                        timestamp,
                        oauthToken,
                        oauthSecret);
        String authorizationHeader =
                this.formGenericAuthorizationHeader(timestamp, nonce, oauthToken, signature);

        // xiacheng NOTE: if there is queryParam, queryParam shall be included in the signature
        // calculation, usually
        // after the URL.
        return this.createOAuthRequest(DnbConstants.Url.GET_PENSION, authorizationHeader)
                .get(FetchPensionResponse.class);
    }

    public FetchFundsResponse fetchFunds(String oauthToken, String oauthSecret) {
        String timestamp = this.timestampUtil.getTimestampInSeconds();
        String nonce = this.timestampUtil.getNonce();
        String signature =
                this.getGenericSignature(
                        DnbConstants.Header.METHOD_GET,
                        DnbConstants.Url.GET_FUNDS_OVERVIEW,
                        nonce,
                        timestamp,
                        oauthToken,
                        oauthSecret);
        String authorizationHeader =
                this.formGenericAuthorizationHeader(timestamp, nonce, oauthToken, signature);

        return this.createOAuthRequest(DnbConstants.Url.GET_FUNDS_OVERVIEW, authorizationHeader)
                .get(FetchFundsResponse.class);
    }

    public FetchFundDetailResponse fetchFundDetails(
            String oauthToken, String oauthSecret, String fundSystem, String fundId) {
        String timestamp = this.timestampUtil.getTimestampInSeconds();
        String nonce = this.timestampUtil.getNonce();
        String signature =
                this.getGenericSignature(
                        DnbConstants.Header.METHOD_GET,
                        DnbConstants.Url.GET_FUND_DETAIL
                                .parameter(DnbConstants.QueryParam.SYSTEM, fundSystem)
                                .parameter(DnbConstants.QueryParam.ID, fundId)
                                .get(),
                        nonce,
                        timestamp,
                        oauthToken,
                        oauthSecret);
        String authorizationHeader =
                this.formGenericAuthorizationHeader(timestamp, nonce, oauthToken, signature);

        return this.createOAuthRequest(
                        DnbConstants.Url.GET_FUND_DETAIL
                                .parameter(DnbConstants.QueryParam.SYSTEM, fundSystem)
                                .parameter(DnbConstants.QueryParam.ID, fundId)
                                .get(),
                        authorizationHeader)
                .get(FetchFundDetailResponse.class);
    }

    public Optional<String> getIsinFromFundDetailPdf(String pdfUrl) {
        try {
            PDDocument pdfDoc = PDDocument.load(this.client.request(pdfUrl).get(byte[].class));
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(pdfDoc);
            pdfDoc.close();
            Matcher matcher = ISIN.matcher(text);
            if (!matcher.find()) {
                return Optional.empty();
            } else {
                return Optional.of(matcher.group(1));
            }
        } catch (IOException e) {
            throw new IllegalStateException("PDF parsing exception", e);
        }
    }
    //// Spare App (Investment) End //////

    private String formGenericAuthorizationHeader(
            String timestamp, String nonce, String oauthToken, String signature) {
        try {
            DnbOAuthHeaderFormatter oAuthHeaderFormatter =
                    getAndFillHeaderFormatter(timestamp, nonce, oauthToken, signature);
            return oAuthHeaderFormatter.toString();
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unsupported encoding exception!", e);
        }
    }

    private String getGenericSignature(
            String method,
            String url,
            String nonce,
            String timestamp,
            String oauthToken,
            String oauthSecret) {
        try {
            // The order here matters as it composes the signing data.
            DnbSignDataUrlEncoder encoder = new DnbSignDataUrlEncoder(method, url);
            fillEncoderData(nonce, timestamp, oauthToken, encoder);
            String signData = encoder.toString();

            // xiacheng NOTE: key is a combination of api secret "&" with tokensecret
            return this.doSign(signData, DnbConstants.OAuth.DNB_API_SECRET + "&" + oauthSecret);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unsupported encoding exception!", e);
        }
    }

    private void fillEncoderData(
            String nonce, String timestamp, String oauthToken, DnbSignDataUrlEncoder encoder)
            throws UnsupportedEncodingException {
        encoder.putPair(
                DnbConstants.Header.OAUTH_CONSUMER_KEY_KEY,
                DnbOAuthEncoder.encode(DnbConstants.Header.OAUTH_CONSUMER_KEY_VALUE));
        encoder.putPair(DnbConstants.Header.OAUTH_NONCE_KEY, nonce);
        encoder.putPair(
                DnbConstants.Header.OAUTH_SIGNATURE_METHOD_KEY,
                DnbConstants.Header.OAUTH_SIGNATURE_METHOD_VALUE);
        encoder.putPair(DnbConstants.Header.OAUTH_TIMESTAMP_KEY, timestamp);
        encoder.putPair(DnbConstants.Header.OAUTH_TOKEN_KEY, oauthToken);
        encoder.putPair(
                DnbConstants.Header.OAUTH_VERSION_KEY, DnbConstants.Header.OAUTH_VERSION_VALUE);
    }

    private String doSign(String data, String key) {
        try {
            SecretKeySpec keySpec =
                    new SecretKeySpec(
                            key.getBytes(DnbConstants.CHARSET), DnbConstants.OAuth.SIGN_ALGORITHM);
            Mac HMACSHA1 = Mac.getInstance(DnbConstants.OAuth.SIGN_ALGORITHM);
            HMACSHA1.init(keySpec);
            return new String(
                            Base64.encodeBase64(
                                    HMACSHA1.doFinal(data.getBytes(DnbConstants.CHARSET))))
                    .replace("\r\n", "");
        } catch (Exception e) {
            throw new IllegalStateException("do sign failed", e);
        }
    }

    private RequestBuilder createOAuthRequest(String url, String authorizationHeader) {
        return this.client
                .request(url)
                .header(DnbConstants.Header.AUTHORIZATION_HEADER, authorizationHeader)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }
}
