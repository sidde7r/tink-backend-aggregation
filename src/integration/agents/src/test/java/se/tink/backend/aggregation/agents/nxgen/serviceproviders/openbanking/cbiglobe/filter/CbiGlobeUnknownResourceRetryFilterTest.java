package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.errorhandle.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CbiGlobeUnknownResourceRetryFilterTest {

    private CbiGlobeUnknownResourceRetryFilter cbiGlobeUnknownResourceRetryFilter;

    @Before
    public void init() {
        cbiGlobeUnknownResourceRetryFilter = new CbiGlobeUnknownResourceRetryFilter(1, 1);
    }

    @Test
    public void if_response_is_not_403_then_should_retry_is_false() {
        // given
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatus()).thenReturn(HttpStatus.SC_BAD_REQUEST);

        // when
        boolean result = cbiGlobeUnknownResourceRetryFilter.shouldRetry(httpResponse);

        // then
        assertThat(result).isFalse();
    }

    @Test
    public void if_response_is_403_resource_unknown_then_should_retry_is_true() {
        // given
        HttpResponse httpResponse = mockHttpErrorResponseForbidden("tpp_resource_unknown.json");

        // when
        boolean result = cbiGlobeUnknownResourceRetryFilter.shouldRetry(httpResponse);

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void if_response_is_403_but_not_resource_unknown_then_should_retry_is_false() {
        // given
        HttpResponse httpResponse = mockHttpErrorResponseForbidden("tpp_invalid_credentials.json");

        // when
        boolean result = cbiGlobeUnknownResourceRetryFilter.shouldRetry(httpResponse);

        // then
        assertThat(result).isFalse();
    }

    private HttpResponse mockHttpErrorResponseForbidden(String fileName) {
        HttpResponse httpResponse = mock(HttpResponse.class);
        String TEST_DATA_PATH =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/cbiglobe/resources";
        when(httpResponse.getBody(ErrorResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, fileName).toFile(), ErrorResponse.class));
        when(httpResponse.getStatus()).thenReturn(HttpStatus.SC_FORBIDDEN);
        return httpResponse;
    }
}
