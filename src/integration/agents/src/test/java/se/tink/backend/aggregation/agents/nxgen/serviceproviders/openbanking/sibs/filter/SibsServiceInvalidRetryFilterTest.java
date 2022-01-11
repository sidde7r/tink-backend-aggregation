package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@RunWith(JUnitParamsRunner.class)
public class SibsServiceInvalidRetryFilterTest {

    private static final String TEST_ERROR_RESPONSE = "{\"code\": \"TEST_ERROR\"}";

    private final SibsServiceInvalidRetryFilter filter = new SibsServiceInvalidRetryFilter(1, 1);
    private final HttpResponse response = mock(HttpResponse.class);

    @Test
    @SneakyThrows
    public void shouldRetryOnServiceInvalidErrorResponse() {
        // given
        given(response.getStatus()).willReturn(405);
        given(response.getBody(String.class)).willReturn(readServiceInvalidResponse());

        // expect
        assertThat(filter.shouldRetry(response)).isTrue();
    }

    @Test
    @Parameters
    public void shouldNotRetry(int status, String responseBody) {
        // given
        given(response.getStatus()).willReturn(status);
        given(response.getBody(String.class)).willReturn(responseBody);

        // expect
        assertThat(filter.shouldRetry(response)).isFalse();
    }

    @SneakyThrows
    @SuppressWarnings("unused")
    private Object[] parametersForShouldNotRetry() {
        return new Object[][] {
            {405, null},
            {405, ""},
            {405, TEST_ERROR_RESPONSE},
            {401, null},
            {403, ""},
            {404, TEST_ERROR_RESPONSE},
            {429, readServiceInvalidResponse()},
        };
    }

    private String readServiceInvalidResponse() throws IOException {
        return FileUtils.readFileToString(
                Paths.get(
                                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sibs/resources/service_invalid_response.json")
                        .toFile(),
                StandardCharsets.UTF_8);
    }
}
