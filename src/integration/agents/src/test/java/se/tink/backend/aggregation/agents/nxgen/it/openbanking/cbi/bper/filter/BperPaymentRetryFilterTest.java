package se.tink.backend.aggregation.agents.nxgen.it.openbanking.cbi.bper.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bper.filter.BperPaymentRetryFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.errorhandle.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class BperPaymentRetryFilterTest {

    private BperPaymentRetryFilter bperPaymentRetryFilter;

    @Before
    public void init() {
        bperPaymentRetryFilter = new BperPaymentRetryFilter(1, 1000);
    }

    @Test
    @Parameters(method = "responses_with_error_codes")
    public void should_retry_is_true_for_specific_responses(String file, Integer httpStatus) {
        // given
        HttpResponse httpResponse = mockHttpErrorResponseForbidden(file, httpStatus);

        // when
        boolean result = bperPaymentRetryFilter.shouldRetry(httpResponse);

        // then
        assertThat(result).isTrue();
    }

    @SuppressWarnings("unused")
    private Object[] responses_with_error_codes() {
        return new Object[] {
            new Object[] {"bper_payment_unknown.json", HttpStatus.SC_FORBIDDEN},
            new Object[] {"bper_authentication_required.json", HttpStatus.SC_BAD_REQUEST},
            new Object[] {"bper_invalid_response.json", HttpStatus.SC_BAD_GATEWAY},
        };
    }

    private HttpResponse mockHttpErrorResponseForbidden(String fileName, Integer httpStatus) {
        HttpResponse httpResponse = mock(HttpResponse.class);
        String TEST_DATA_PATH =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/openbanking/cbi/bper/resources";
        when(httpResponse.getBody(ErrorResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, fileName).toFile(), ErrorResponse.class));
        when(httpResponse.getStatus()).thenReturn(httpStatus);
        return httpResponse;
    }
}
