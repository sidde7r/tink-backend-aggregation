package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Stack;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.http.HttpStatus;
import org.apache.http.cookie.Cookie;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneConstants.FormKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneConstants.RequestValues;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.entities.BpcestaQueryParamData;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.entities.Characteristics;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.entities.ClaimsQueryParamData;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.entities.ITEntityType;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.entities.IdToken;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.entities.ImageItem;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.entities.TokenFromLocation;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.entities.Userinfo;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.entities.ValidationUnit;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.rpc.CredentialValidationRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.rpc.IdentificationRoutingRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.rpc.IdentificationRoutingResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.rpc.OAuth2TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.rpc.OAuth2V2AuthorizeResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.rpc.SamlAuthnResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.fetcher.transactionalaccount.rpc.TransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.fetcher.transferdestination.rpc.BeneficiariesResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.utils.CaisseEpargneUtils;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.utils.SoapHelper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CaisseEpargneApiClient {
    private final TinkHttpClient httpClient;
    private final TinkHttpClient notRedirectFollowingHttpClient;
    private final SessionStorage sessionStorage;
    private final Storage instanceStorage;

    public CaisseEpargneApiClient(
            TinkHttpClient httpClient, SessionStorage sessionStorage, Storage instanceStorage) {
        this.httpClient = httpClient;
        this.httpClient.disableAggregatorHeader();
        this.notRedirectFollowingHttpClient = httpClient;
        this.notRedirectFollowingHttpClient.disableAggregatorHeader();
        this.notRedirectFollowingHttpClient.setFollowRedirects(false);
        this.sessionStorage = sessionStorage;
        this.instanceStorage = instanceStorage;
    }

    public Optional<OAuth2Token> getOAuth2Token() {
        Optional<OAuth2Token> token =
                Optional.ofNullable(
                        httpClient
                                .request(Urls.OAUTH2_TOKEN)
                                .header(
                                        HeaderKeys.CONTENT_TYPE,
                                        MediaType.APPLICATION_FORM_URLENCODED)
                                .accept(MediaType.APPLICATION_JSON_TYPE)
                                .acceptLanguage(Locale.US)
                                .body(new TokenRequest())
                                .post(OAuth2TokenResponse.class)
                                .toTinkToken());
        token.ifPresent(t -> sessionStorage.put(PersistentStorageKeys.OAUTH_2_TOKEN, t));
        return token;
    }

    public IdentificationRoutingResponse identificationRouting(String userCode)
            throws SessionException {
        OAuth2Token bearerToken =
                sessionStorage
                        .get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class)
                        .orElseThrow(SessionError.SESSION_EXPIRED::exception);
        IdentificationRoutingRequest request =
                IdentificationRoutingRequest.builder()
                        .characteristics(
                                Characteristics.builder()
                                        .bankId("")
                                        .subscribeTypeItems(Collections.emptyList())
                                        .iTEntityType(
                                                ITEntityType.builder()
                                                        .code(RequestValues.IT_ENTITY_02)
                                                        .build())
                                        .userCode(userCode)
                                        .build())
                        .build();
        return httpClient
                .request(Urls.IDENTIFICATION_ROUTING)
                .addBearerToken(bearerToken)
                .acceptLanguage(Locale.US)
                .accept(MediaType.WILDCARD)
                .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .body(request)
                .post(IdentificationRoutingResponse.class);
    }

    public OAuth2V2AuthorizeResponse oAuth2Authorize(
            String userCode, String bankIdentifier, String membershipType) {
        HttpResponse httpResponse =
                httpClient
                        .request(Urls.OAUTH_V2_AUTHORIZE)
                        .queryParam(QueryKeys.LOGIN_HINT, userCode)
                        .queryParam(
                                QueryKeys.BPCESTA,
                                createSerializedBpcestaValue(bankIdentifier, membershipType))
                        .queryParam(QueryKeys.CLAIMS, createSerializedClaimsValue())
                        .queryParam(QueryKeys.CDETAB, bankIdentifier)
                        .queryParam(QueryKeys.NONCE, UUID.randomUUID().toString())
                        .queryParam(QueryKeys.SECRED_IT, QueryValues.SECRET)
                        .queryParam(QueryKeys.DISPLAY, QueryValues.TOUCH)
                        .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.ID_TOKEN_TOKEN)
                        .queryParam(QueryKeys.CLIENT_ID, QueryValues.CLIENT_ID)
                        .queryParam(
                                QueryKeys.REDIRECT_URI,
                                QueryValues.CONTAINER_APP_BAPI_SETUP_SUCCESS)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .get(HttpResponse.class);
        return httpResponse.getBody(OAuth2V2AuthorizeResponse.class);
    }

    public SamlAuthnResponse samlAuthorize(URL action, String samlRequest) {
        try {
            String urlEncodedSamlRequest =
                    URLEncoder.encode(samlRequest, StandardCharsets.UTF_8.toString());
            String requestBody = FormKeys.SAML_REQUEST + "=" + urlEncodedSamlRequest;
            HttpResponse redirect =
                    notRedirectFollowingHttpClient
                            .request(action)
                            .accept(MediaType.WILDCARD)
                            .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                            .header(HeaderKeys.USER_AGENT, HeaderValues.CAISSE_DARWIN)
                            .body(requestBody)
                            .post(HttpResponse.class);
            if (redirect.getStatus() != HttpStatus.SC_SEE_OTHER) {
                throw new IllegalStateException("Expected redirect, got: " + redirect.getStatus());
            }
            String redirectLocation = redirect.getLocation().toString();
            sessionStorage.put(StorageKeys.REDIRECT_LOCATION, redirectLocation);
            return httpClient
                    .request(Urls.ICG_AUTH_BASE.concat(redirectLocation))
                    .accept(MediaType.WILDCARD)
                    .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .header(HeaderKeys.USER_AGENT, HeaderValues.CAISSE_DARWIN)
                    .get(SamlAuthnResponse.class);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Could not URL encode", e);
        }
    }

    public Map<String, byte[]> getKeyboardImages(URL imageUrl) {
        HttpResponse imagesResponse =
                httpClient
                        .request(imageUrl)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .header(HeaderKeys.USER_AGENT, HeaderValues.CAISSE_DARWIN)
                        .get(HttpResponse.class);
        MultivaluedMap<String, String> a = imagesResponse.getHeaders();
        List<String> collect =
                imagesResponse.getHeaders().entrySet().stream()
                        .filter(entry -> HeaderKeys.SET_COOKIE.equals(entry.getKey()))
                        .map(Entry::getValue)
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
        List<Cookie> newCookies =
                collect.stream()
                        .map(CaisseEpargneUtils::parseRawCookie)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
        Stack<Cookie> actualCookies = new Stack<>();
        List<Cookie> oldCookies = httpClient.getCookies();
        httpClient.clearCookies();
        newCookies.forEach(
                newCookie -> {
                    for (Cookie oldCookie : oldCookies) {
                        if (oldCookie.getValue().equals(newCookie.getValue())) {
                            oldCookies.remove(oldCookie);
                        }
                    }
                });
        newCookies.forEach(actualCookies::push);
        oldCookies.forEach(actualCookies::push);

        List<ImageItem> imageItems = Arrays.asList(imagesResponse.getBody(ImageItem[].class));
        Map<String, byte[]> images =
                imageItems.stream()
                        .map(imageItem -> getImage(imageItem, actualCookies))
                        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        actualCookies.forEach(httpClient::addCookie);
        return images;
    }

    public SamlAuthnResponse submitPassword(
            String validationId, String validationUnitId, String passwordKeyString) {
        String redirectLocation = sessionStorage.get(StorageKeys.REDIRECT_LOCATION);
        Map<String, List<ValidationUnit>> validationUnits =
                Collections.singletonMap(
                        validationId,
                        Collections.singletonList(
                                ValidationUnit.builder()
                                        .id(validationUnitId)
                                        .password(passwordKeyString)
                                        .type(RequestValues.PASSWORD)
                                        .build()));
        CredentialValidationRequest request =
                CredentialValidationRequest.builder().validate(validationUnits).build();
        return httpClient
                .request(Urls.ICG_AUTH_BASE.concat(redirectLocation).concat(Urls.STEP_PATH))
                .accept(MediaType.WILDCARD_TYPE)
                .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HeaderKeys.USER_AGENT, HeaderValues.CAISSE_DARWIN)
                .body(request)
                .post(SamlAuthnResponse.class);
    }

    private Entry<String, byte[]> getImage(ImageItem imageItem, List<Cookie> cookies) {
        String cookieHeader =
                cookies.stream()
                        .map(c -> String.join("=", c.getName(), c.getValue()))
                        .collect(Collectors.joining("; "));
        byte[] bytes =
                httpClient
                        .request(Urls.ICG_AUTH_BASE.concat(imageItem.getUri()))
                        .accept(MediaType.WILDCARD_TYPE)
                        .header(HeaderKeys.COOKIE, cookieHeader)
                        .header(HeaderKeys.USER_AGENT, HeaderValues.CAISSE_DARWIN)
                        .get(byte[].class);
        return new SimpleEntry<>(imageItem.getValue(), bytes);
    }

    private String createSerializedBpcestaValue(String bankId, String membershipType) {
        BpcestaQueryParamData bpcestaQueryParamData =
                new BpcestaQueryParamData(bankId, membershipType);
        sessionStorage.put(StorageKeys.TERM_ID, bpcestaQueryParamData.getTermId());
        return SerializationUtils.serializeToString(bpcestaQueryParamData);
    }

    private String createSerializedClaimsValue() {
        return SerializationUtils.serializeToString(
                ClaimsQueryParamData.builder()
                        .idToken(new IdToken())
                        .userinfo(new Userinfo())
                        .build());
    }

    public void oAuth2Consume(String oAuth2ConsumeUrl, String samlResponse) {
        String urlEncodedSamlRequest = null;
        try {
            urlEncodedSamlRequest =
                    URLEncoder.encode(samlResponse, StandardCharsets.UTF_8.toString());
            String requestBody = FormKeys.SAML_REQUEST + "=" + urlEncodedSamlRequest;
            HttpResponse redirect =
                    notRedirectFollowingHttpClient
                            .request(new URL(oAuth2ConsumeUrl))
                            .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                            .accept(MediaType.WILDCARD)
                            .body(requestBody)
                            .post(HttpResponse.class);
            if (redirect.getStatus() != HttpStatus.SC_MOVED_TEMPORARILY) {
                throw new IllegalStateException("Expected redirect, got: " + redirect.getStatus());
            }
            TokenFromLocation token = TokenFromLocation.of(redirect.getLocation().toString());
            if (!token.isValidBearerToken()) {
                throw new IllegalStateException("Could not parse token from location url.");
            }
            sessionStorage.put(StorageKeys.TOKEN, token.toOAuth2Token());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Could not URL encode", e);
        }
    }

    public void soapActionSsoBapi(String bankId) {
        Optional<OAuth2Token> token = sessionStorage.get(StorageKeys.TOKEN, OAuth2Token.class);
        Optional<String> termId = sessionStorage.get(StorageKeys.TERM_ID, String.class);
        HttpResponse response =
                httpClient
                        .request(Urls.WS_BAD)
                        .header(HeaderKeys.SOAP_ACTION, HeaderValues.SSO_BAPI)
                        .header(HeaderKeys.VERSION_WS_BAD, HeaderValues.VERSION_WS_BAD_22)
                        .header(HeaderKeys.ESTABLISHMENT_ID, bankId)
                        .header(HeaderKeys.CONTENT_TYPE, MediaType.TEXT_XML)
                        .accept(MediaType.WILDCARD_TYPE)
                        .body(
                                SoapHelper.createSsoBapiRequest(
                                        token.orElseThrow(
                                                        () ->
                                                                new IllegalStateException(
                                                                        "No token found in storage."))
                                                .getAccessToken(),
                                        termId.orElseThrow(
                                                () ->
                                                        new IllegalStateException(
                                                                "No term id found in storage."))))
                        .post(HttpResponse.class);
        String xmlBody = response.getBody(String.class);
        instanceStorage.put(StorageKeys.FINAL_AUTH_RESPONSE, xmlBody);
    }

    public AccountsResponse getAccounts() {
        String accountsResponse =
                httpClient
                        .request(Urls.WS_BAD)
                        .header(HeaderKeys.SOAP_ACTION, HeaderValues.GET_ACCOUNTS)
                        .accept(MediaType.WILDCARD_TYPE)
                        .header(HeaderKeys.CONTENT_TYPE, MediaType.TEXT_XML)
                        .body(SoapHelper.createGetAccountsRequest())
                        .post(String.class);
        return SoapHelper.getAccounts(accountsResponse);
    }

    public AccountDetailsResponse getAccountDetails(String fullAccountNumber) {
        String request = SoapHelper.createGetAccountDetailsRequest(fullAccountNumber);
        String accountDetailsResponse =
                httpClient
                        .request(Urls.WS_BAD)
                        .header(HeaderKeys.SOAP_ACTION, HeaderValues.GET_ACCOUNT_DETAILS)
                        .accept(MediaType.WILDCARD_TYPE)
                        .header(HeaderKeys.CONTENT_TYPE, MediaType.TEXT_XML)
                        .body(request)
                        .post(String.class);
        return SoapHelper.getAccountDetails(accountDetailsResponse);
    }

    public TransactionsResponse getTransactionsForAccount(String fullAccountNumber, String key) {
        TransactionsRequest transactionsRequest = new TransactionsRequest();
        transactionsRequest.setAccount(fullAccountNumber);
        transactionsRequest.setPaginationKey(key);
        String request = SoapHelper.formRequest(transactionsRequest);
        String transactionsResponse =
                httpClient
                        .request(Urls.WS_BAD)
                        .accept(MediaType.WILDCARD_TYPE)
                        .header(HeaderKeys.CONTENT_TYPE, MediaType.TEXT_XML)
                        .header(HeaderKeys.SOAP_ACTION, HeaderValues.GET_TRANSACTIONS)
                        .body(request)
                        .post(String.class);
        return SoapHelper.getTransactions(transactionsResponse);
    }

    public BeneficiariesResponse getBeneficiaries() {
        OAuth2Token bearerToken =
                sessionStorage
                        .get(StorageKeys.TOKEN, OAuth2Token.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                SessionError.SESSION_EXPIRED.exception()));
        return httpClient
                .request(Urls.GET_BENEFICIARIES)
                .addBearerToken(bearerToken)
                .accept(MediaType.WILDCARD_TYPE)
                .get(BeneficiariesResponse.class);
    }
}
