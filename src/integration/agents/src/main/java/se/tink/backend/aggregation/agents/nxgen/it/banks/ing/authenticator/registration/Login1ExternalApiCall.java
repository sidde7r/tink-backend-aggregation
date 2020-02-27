package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.core.MultivaluedMap;
import lombok.Data;
import lombok.experimental.Accessors;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.ConfigurationProvider;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.Login1ExternalApiCall.Arg;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.Login1ExternalApiCall.Result;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationSteppable;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.scaffold.ExternalApiCallResult;
import se.tink.backend.aggregation.nxgen.scaffold.SimpleExternalApiCall;

public class Login1ExternalApiCall extends SimpleExternalApiCall<Arg, Result>
        implements AuthenticationSteppable {

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("dd");
    private static final DateTimeFormatter MONT_FORMATTER = DateTimeFormatter.ofPattern("MM");
    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy");

    private final ConfigurationProvider configurationProvider;
    private final CommonDataProvider commonDataProvider;
    private final PinKeyboardMapper pinKeyboardMapper;
    private final OtmlParser otmlParser;

    public Login1ExternalApiCall(
            TinkHttpClient httpClient,
            ConfigurationProvider configurationProvider,
            CommonDataProvider commonDataProvider,
            PinKeyboardMapper pinKeyboardMapper,
            OtmlParser otmlParser) {
        super(httpClient);
        this.configurationProvider = configurationProvider;
        this.commonDataProvider = commonDataProvider;
        this.pinKeyboardMapper = pinKeyboardMapper;
        this.otmlParser = otmlParser;
    }

    @Override
    public Optional<String> getStepIdentifier() {
        return Optional.of(getClass().getSimpleName());
    }

    protected HttpRequest prepareRequest(Arg arg) {
        return new HttpRequestImpl(
                HttpMethod.POST,
                new URL(configurationProvider.getBaseUrl() + "/MobileFlow/login1.htm"),
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
                .put("is", "BAB50C27-3DE2-4FC4-9086-9D7B85A08B2C")
                .put("visibility", "visible")
                .put("omtl_context", "c1")
                .put("day", arg.getBirthDate().format(DAY_FORMATTER))
                .put("aid", "-")
                .put("month", arg.getBirthDate().format(MONT_FORMATTER))
                .put("year", arg.getBirthDate().format(YEAR_FORMATTER))
                .put("oid", "-")
                .put("fpe", commonDataProvider.prepareFpe(arg.getDeviceId()))
                .put("personId", arg.getPersonId())
                .build()
                .serialize();
    }

    protected ExternalApiCallResult<Result> parseResponse(HttpResponse httpResponse) {
        OtmlResponse otmlResponse = httpResponse.getBody(OtmlResponse.class);
        return ExternalApiCallResult.of(prepareResults(otmlResponse), httpResponse.getStatus());
    }

    private Result prepareResults(OtmlResponse otmlResponse) {
        return new Result()
                .setPinKeyboardMap(getPinKeyboardMap(otmlResponse.getDatasources()))
                .setPinNumberPositions(otmlParser.getPinPositions(otmlResponse.getDatasources()));
    }

    private Map<Integer, Integer> getPinKeyboardMap(String document) {
        return pinKeyboardMapper.toPinKeyboardMap(otmlParser.getPinKeyboardImages(document));
    }

    @Accessors(chain = true)
    @Data
    static class Arg {

        private String is;

        private String personId;
        private LocalDate birthDate;
        private String deviceId;
    }

    @Accessors(chain = true)
    @Data
    static class Result {

        private Map<Integer, Integer> pinKeyboardMap;

        private List<Integer> pinNumberPositions;
    }
}
