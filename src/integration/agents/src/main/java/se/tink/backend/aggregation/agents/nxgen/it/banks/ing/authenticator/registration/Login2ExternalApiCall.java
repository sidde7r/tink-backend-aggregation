package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.ConfigurationProvider;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.Login2ExternalApiCall.Arg;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.Login2ExternalApiCall.Result;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.scaffold.ExternalApiCallResult;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.scaffold.SimpleExternalApiCall;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class Login2ExternalApiCall extends SimpleExternalApiCall<Arg, Result> {

    private final ConfigurationProvider configurationProvider;
    private final CommonDataProvider commonDataProvider;

    public Login2ExternalApiCall(
            TinkHttpClient httpClient,
            ConfigurationProvider configurationProvider,
            CommonDataProvider commonDataProvider) {
        super(httpClient);
        this.configurationProvider = configurationProvider;
        this.commonDataProvider = commonDataProvider;
    }

    @Override
    protected HttpRequest prepareRequest(Arg arg) {
        return new HttpRequestImpl(
                HttpMethod.POST,
                new URL(configurationProvider.getBaseUrl() + "/MobileFlow/login2.htm"),
                prepareRequestHeaders(),
                prepareRequestBody(arg));
    }

    private MultivaluedMap<String, Object> prepareRequestHeaders() {
        MultivaluedMap<String, Object> headers = commonDataProvider.getStaticHeaders();
        headers.putSingle("Content-Type", "application/x-www-form-urlencoded");
        return headers;
    }

    private String prepareRequestBody(Arg arg) {

        return Form.builder()
                .encodeSpacesWithPercent()
                .put("is_passcode_enrolled", "true")
                .put("is_fingerprint_enrolled", "false")
                .put("challengeId", "-")
                .put("is_touchid_available", "true")
                .put("is_login_with_touch_id_enabled", "")
                .put("r", "0")
                .put("showTouchIDNotEnable", "true")
                .put("was_login_activation_already_asked", "")
                .put("oid", "-")
                .put("fpe", commonDataProvider.prepareFpe(arg.getDeviceId()))
                .put("push_active", "false")
                .put("value1", arg.getMappedPinValue(1))
                .put("aid", "-")
                .put("otml_context", "c1")
                .put("is_iphone_x", "false")
                .put("fingerprint", arg.getDeviceId())
                .put("value3", arg.getMappedPinValue(3))
                .put("device_os", "iOS 12.4")
                .put("geolocation_permission", "false")
                .put("show_push_activation_page", "true")
                .put("isBiometricButtonDisabled", "true")
                .put("value2", arg.getMappedPinValue(2))
                .build()
                .serialize();
    }

    @Override
    protected ExternalApiCallResult<Result> parseResponse(HttpResponse httpResponse) {
        return ExternalApiCallResult.of(
                new Result(extractJSessionId(httpResponse)),
                httpResponse.getStatus(),
                httpResponse.getLocation());
    }

    private String extractJSessionId(HttpResponse httpResponse) {
        return httpResponse.getCookies().stream()
                .filter(cookie -> "JSESSIONID".equalsIgnoreCase(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(
                        () -> new IllegalStateException("Required value JSESSIONID is missing"));
    }

    private Optional<String> getHeaderValue(HttpResponse httpResponse, String headerName) {
        return Optional.of(httpResponse)
                .map(HttpResponse::getHeaders)
                .map(x -> x.getFirst(headerName));
    }

    @Accessors(chain = true)
    @Data
    static class Arg {

        private String deviceId;
        private String pin;
        private Map<Integer, Integer> pinKeyboardMap;
        private List<Integer> pinPositions;

        String getMappedPinValue(int index) {
            return Optional.of(index)
                    .map(this::getPinDigitPosition)
                    .map(this::getPinDigit)
                    .map(this::mapPinDigit)
                    .map(String::valueOf)
                    .orElseThrow(IllegalStateException::new);
        }

        private Integer getPinDigitPosition(int position) {
            return Optional.of(getPinPositions())
                    .map(positions -> positions.get(position - 1))
                    .orElseThrow(IllegalStateException::new);
        }

        private Integer getPinDigit(int index) {
            return Optional.of(pin)
                    .map(p -> p.charAt(index - 1))
                    .map(Character::getNumericValue)
                    .orElseThrow(IllegalStateException::new);
        }

        private Integer mapPinDigit(Integer pinDigit) {
            return pinKeyboardMap.get(pinDigit);
        }
    }

    @AllArgsConstructor
    @Data
    static class Result {

        private String jSessionId;
    }
}
