package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@RunWith(JUnitParamsRunner.class)
public class IngBaseInstantSepaErrorFilterTest {

    private static final String RESOURCE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ingbase/resources";
    private static final String TEST_WRONG_ERROR_MESSAGE = "instant payment is possible";

    private IngBaseInstantSepaErrorFilter ingBaseInstantSepaErrorFilter;

    @Mock private HttpRequest httpRequest;

    @Mock private HttpResponse httpResponse;

    @Mock private Filter mockNextFilter;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ingBaseInstantSepaErrorFilter = new IngBaseInstantSepaErrorFilter();
        ingBaseInstantSepaErrorFilter.setNext(mockNextFilter);
        given(mockNextFilter.handle(httpRequest)).willReturn(httpResponse);
    }

    @Test
    @Parameters({"400", "401", "422", "500", "501", "502", "503"})
    public void shouldThrowOnInstantSepaNotPossibleError(int status) throws IOException {
        // given
        given(httpResponse.getStatus()).willReturn(status);
        given(httpResponse.getBody(String.class)).willReturn(sepaInstantPaymentIsNotPossible());

        // expect
        assertThatThrownBy(() -> ingBaseInstantSepaErrorFilter.handle(httpRequest))
                .isInstanceOf(PaymentValidationException.class)
                .hasMessage("Instant payment is not supported");
    }

    @Test
    @Parameters
    public void shouldFilterAndReturnHttpResponse(int status, String response) {
        // given
        given(httpResponse.getStatus()).willReturn(status);
        given(httpResponse.getBody(String.class)).willReturn(response);

        // when
        HttpResponse result = ingBaseInstantSepaErrorFilter.handle(httpRequest);

        // then
        assertThat(result).isEqualTo(httpResponse);
    }

    @SuppressWarnings("unused")
    private Object[] parametersForShouldFilterAndReturnHttpResponse() {
        return new Object[][] {
            {400, TEST_WRONG_ERROR_MESSAGE},
            {401, null},
            {422, ""},
            {500, "{}"},
            {501, TEST_WRONG_ERROR_MESSAGE},
            {503, TEST_WRONG_ERROR_MESSAGE},
            {503, TEST_WRONG_ERROR_MESSAGE},
        };
    }

    private String sepaInstantPaymentIsNotPossible() throws IOException {
        return FileUtils.readFileToString(
                Paths.get(RESOURCE_PATH, "sepa_instant_not_possible.json").toFile(),
                StandardCharsets.UTF_8);
    }
}
