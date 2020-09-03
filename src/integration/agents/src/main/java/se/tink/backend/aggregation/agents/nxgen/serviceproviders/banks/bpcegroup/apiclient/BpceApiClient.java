package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.BpceConstants.AUTHORIZE_PATH;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.BpceConstants.BENEFICIARIES_PATH;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.BpceConstants.STEP_PATH;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.apache.http.cookie.Cookie;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize.AuthTransactionResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize.AuthorizeResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize.BpcestaQueryParamDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize.ClaimsQueryParamDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize.ImageItemDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize.Saml2PostDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize.ValidateUnitRequestDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize.ValidationUnitOtpDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize.ValidationUnitPasswordDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize.ValidationUnitRequestItemBaseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.transferdestination.BeneficiariesResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.entities.MembershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.configuration.BpceConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.storage.BpceStorage;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RequiredArgsConstructor
public abstract class BpceApiClient {

    protected final TinkHttpClient httpClient;
    protected final BpceConfiguration bpceConfiguration;
    protected final RandomValueGenerator randomValueGenerator;
    protected final BpceStorage bpceStorage;
    private final BpceTokenExtractor bpceTokenExtractor;
    private final BpceCookieParserHelper cookieParserHelper;

    public AuthorizeResponseDto sendAuthorizeRequest(
            String bankId, String username, MembershipType membershipType) {
        return prepareAuthorizeRequest(bankId, username, membershipType, null)
                .get(AuthorizeResponseDto.class);
    }

    public String getAuthTransactionPath(AuthorizeResponseDto authorizeResponseDto) {
        final String requestBody = createGetTransactionPathRequest(authorizeResponseDto);

        final HttpResponse httpResponse =
                baseAuthRequest(authorizeResponseDto.getAction())
                        .accept(MediaType.WILDCARD)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                        .post(HttpResponse.class, requestBody);

        verifyGetTransactionPathResponse(httpResponse);

        return httpResponse.getLocation().toString();
    }

    public AuthTransactionResponseDto getAuthTransaction(String authTransactionPath) {
        final String url = bpceConfiguration.getIcgAuthBaseUrl() + authTransactionPath;

        return baseAuthRequest(url)
                .accept(MediaType.WILDCARD)
                .get(AuthTransactionResponseDto.class);
    }

    public AuthTransactionResponseDto sendPassword(
            String validationId,
            String validationUnitId,
            String authTransactionPath,
            String password) {
        final ValidationUnitPasswordDto validationUnitPasswordDto =
                ValidationUnitPasswordDto.builder().id(validationUnitId).password(password).build();

        return sendValidateStepRequest(
                authTransactionPath, validationId, validationUnitPasswordDto);
    }

    public AuthTransactionResponseDto sendOtp(
            String validationId,
            String validationUnitId,
            String authTransactionPath,
            String otpCode) {
        final ValidationUnitOtpDto validationUnitOtpDto =
                ValidationUnitOtpDto.builder().id(validationUnitId).otpCode(otpCode).build();

        return sendValidateStepRequest(authTransactionPath, validationId, validationUnitOtpDto);
    }

    public OAuth2Token oAuth2Consume(Saml2PostDto saml2PostDto) {
        final String requestBody = createOauth2ConsumeRequest(saml2PostDto);

        final HttpResponse httpResponse =
                baseAuthRequest(saml2PostDto.getAction())
                        .accept(MediaType.WILDCARD)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                        .post(HttpResponse.class, requestBody);

        verifyOAuth2ConsumeResponse(httpResponse);

        return bpceTokenExtractor.extractToken(httpResponse);
    }

    public BeneficiariesResponseDto getBeneficiaries() {
        final String url = bpceConfiguration.getRsExAthBaseUrl() + BENEFICIARIES_PATH;

        return baseAuthRequest(url)
                .addBearerToken(getTokenFromStorage())
                .accept(MediaType.WILDCARD_TYPE)
                .get(BeneficiariesResponseDto.class);
    }

    public Map<String, byte[]> getKeyboardImages(String imagePath) {
        final String url = bpceConfiguration.getIcgAuthBaseUrl() + imagePath;

        final HttpResponse imagesResponse =
                baseAuthRequest(url)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .get(HttpResponse.class);

        final Deque<Cookie> actualCookies = arrangeCookies(imagesResponse);

        final List<ImageItemDto> imageItems =
                Arrays.asList(imagesResponse.getBody(ImageItemDto[].class));
        final Map<String, byte[]> images =
                imageItems.stream()
                        .map(imageItem -> getImage(imageItem, actualCookies))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        actualCookies.forEach(httpClient::addCookie);
        return images;
    }

    protected RequestBuilder prepareAuthorizeRequest(
            String bankId, String username, MembershipType membershipType, String idTokenHint) {
        final String url = bpceConfiguration.getAuthBaseUrl() + AUTHORIZE_PATH;

        final RequestBuilder requestBuilder =
                baseAuthRequest(url)
                        .queryParam("login_hint", username)
                        .queryParam("bpcesta", createSerializedBpcestaValue(bankId, membershipType))
                        .queryParam("claims", createSerializedClaimsValue())
                        .queryParam("cdetab", bankId)
                        .queryParam("nonce", randomValueGenerator.getUUID().toString())
                        .queryParam("secret_id", bpceConfiguration.getClientSecret())
                        .queryParam("display", "touch")
                        .queryParam("response_type", "id_token token")
                        .queryParam("client_id", bpceConfiguration.getClientId())
                        .queryParam("redirect_uri", "containerApp://BAPIStepUpSuccess")
                        .accept(MediaType.APPLICATION_JSON);

        Optional.ofNullable(idTokenHint)
                .ifPresent(idToken -> requestBuilder.queryParam("id_token_hint", idToken));

        return requestBuilder;
    }

    private AuthTransactionResponseDto sendValidateStepRequest(
            String authTransactionPath,
            String validationId,
            ValidationUnitRequestItemBaseDto validationUnitRequestItem) {
        final String url = bpceConfiguration.getIcgAuthBaseUrl() + authTransactionPath + STEP_PATH;

        final ValidateUnitRequestDto validateUnitRequestDto =
                new ValidateUnitRequestDto(validationId, validationUnitRequestItem);

        return baseAuthRequest(url)
                .accept(MediaType.WILDCARD)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .post(AuthTransactionResponseDto.class, validateUnitRequestDto);
    }

    protected RequestBuilder baseAuthRequest(String url) {
        return httpClient
                .request(url)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "en-us")
                .header(HttpHeaders.USER_AGENT, bpceConfiguration.getAuthUserAgent());
    }

    private String createSerializedBpcestaValue(String bankId, MembershipType membershipType) {
        final BpcestaQueryParamDto bpcestaQueryParamDto =
                BpcestaQueryParamDto.builder()
                        .cdetab(bankId)
                        .enseigne(bpceConfiguration.getBranchId())
                        .csid(randomValueGenerator.getUUID().toString())
                        .termId(randomValueGenerator.getUUID().toString())
                        .typSrv(membershipType.getName())
                        .build();

        bpceStorage.storeTermId(bpcestaQueryParamDto.getTermId());

        return SerializationUtils.serializeToString(bpcestaQueryParamDto);
    }

    protected OAuth2Token getTokenFromStorage() {
        return bpceStorage.getOAuth2Token().orElseThrow(SessionError.SESSION_EXPIRED::exception);
    }

    private Deque<Cookie> arrangeCookies(HttpResponse imagesResponse) {
        List<String> setCookies =
                imagesResponse.getHeaders().entrySet().stream()
                        .filter(entry -> "Set-Cookie".equalsIgnoreCase(entry.getKey()))
                        .map(Map.Entry::getValue)
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
        List<Cookie> newCookies =
                setCookies.stream()
                        .map(cookieParserHelper::parseRawCookie)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

        Deque<Cookie> actualCookies = new ArrayDeque<>();
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
        return actualCookies;
    }

    private Map.Entry<String, byte[]> getImage(ImageItemDto imageItem, Deque<Cookie> cookies) {

        final String url = bpceConfiguration.getIcgAuthBaseUrl() + imageItem.getUri();

        final String cookieHeader =
                cookies.stream()
                        .map(c -> String.join("=", c.getName(), c.getValue()))
                        .collect(Collectors.joining("; "));

        final byte[] bytes =
                baseAuthRequest(url)
                        .accept(MediaType.WILDCARD_TYPE)
                        .header("Cookie", cookieHeader)
                        .get(byte[].class);

        return new AbstractMap.SimpleEntry<>(imageItem.getValue(), bytes);
    }

    private static String createSerializedClaimsValue() {
        return SerializationUtils.serializeToString(new ClaimsQueryParamDto());
    }

    private static String createGetTransactionPathRequest(
            AuthorizeResponseDto authorizeResponseDto) {
        return "SAMLRequest=" + encodeString(authorizeResponseDto.getParameters().getSAMLRequest());
    }

    protected static String createOauth2ConsumeRequest(Saml2PostDto saml2PostDto) {
        return "SAMLResponse=" + encodeString(saml2PostDto.getSamlResponse());
    }

    private static String encodeString(String toEncode) {
        try {
            return URLEncoder.encode(toEncode, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Could not encode request");
        }
    }

    private static void verifyGetTransactionPathResponse(HttpResponse httpResponse) {
        if (httpResponse.getStatus() != HttpStatus.SC_SEE_OTHER) {
            throw new IllegalArgumentException(
                    "Get transaction path response status is not 303. Actual value: "
                            + httpResponse.getStatus());
        }
    }

    private static void verifyOAuth2ConsumeResponse(HttpResponse httpResponse) {
        if (httpResponse.getStatus() != HttpStatus.SC_MOVED_TEMPORARILY) {
            throw new IllegalArgumentException(
                    "OAuth2 consume response status is not 302. Actual value: "
                            + httpResponse.getStatus());
        }
    }
}
