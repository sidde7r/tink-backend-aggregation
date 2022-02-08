package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.validators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.UkObErrorResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

class HSBCFailedEligibilityCheckCodeValidatorTest {
    private ObjectMapper objectMapper;
    private HttpResponse httpResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        httpResponse = mock(HttpResponse.class);
    }

    @Test
    void shouldReturnTrueWhenHttpResponseHas400StatusAndHasFailedEligibilityCheckErrorCode()
            throws JsonProcessingException {
        // given
        String jsonResponse =
                "{\n"
                        + "  \"Code\" : \"400\",\n"
                        + "  \"Id\" : \"cd22e7fb-4f0b-4f24-b2b3-f109eed8f812\",\n"
                        + "  \"Message\" : \"Bad Request\",\n"
                        + "  \"Errors\" : [ {\n"
                        + "    \"ErrorCode\" : \"UK.HSBC.FailedEligibilityCheck\",\n"
                        + "    \"Message\" : \"Failed Eligibility check\"\n"
                        + "  } ]\n"
                        + "}";
        UkObErrorResponse errorResponse =
                objectMapper.readValue(jsonResponse, UkObErrorResponse.class);
        when(httpResponse.getStatus()).thenReturn(400);
        when(httpResponse.getBody(UkObErrorResponse.class)).thenReturn(errorResponse);

        // when
        boolean isValid = HSBCFailedEligibilityCheckCodeValidator.validate(httpResponse);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldReturnFalseWhenHttpResponseHasDifferentThan400() {
        // given
        when(httpResponse.getStatus()).thenReturn(404);

        // when
        boolean isValid = HSBCFailedEligibilityCheckCodeValidator.validate(httpResponse);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldReturnFalseWhenHttpResponseHas400AndEmptyErrorCodeList()
            throws JsonProcessingException {
        // given
        String jsonResponse =
                "{\n"
                        + "  \"Code\" : \"400\",\n"
                        + "  \"Id\" : \"cd22e7fb-4f0b-4f24-b2b3-f109eed8f812\",\n"
                        + "  \"Message\" : \"Bad Request\",\n"
                        + "  \"Errors\" : [ ]\n"
                        + "}";
        UkObErrorResponse errorResponse =
                objectMapper.readValue(jsonResponse, UkObErrorResponse.class);
        when(httpResponse.getStatus()).thenReturn(400);
        when(httpResponse.getBody(UkObErrorResponse.class)).thenReturn(errorResponse);

        // when
        boolean isValid = HSBCFailedEligibilityCheckCodeValidator.validate(httpResponse);

        // then
        assertThat(isValid).isFalse();
    }
}
