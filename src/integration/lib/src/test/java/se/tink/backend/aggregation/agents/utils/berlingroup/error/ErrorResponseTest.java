package se.tink.backend.aggregation.agents.utils.berlingroup.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Predicate;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class ErrorResponseTest {

    private static final String TEST_PSU_MESSAGE = "test_psu_message";

    @Test
    @Parameters(value = {TEST_PSU_MESSAGE, "asdf" + TEST_PSU_MESSAGE + "zxcv", "TEST_psu_MESSage"})
    public void psuMessageContainsPredicateShouldReturnTrue(String errorResponsePsuMessage) {
        // given
        Predicate<ErrorResponse> errorResponsePredicate =
                ErrorResponse.psuMessageContainsPredicate(TEST_PSU_MESSAGE);
        ErrorResponse mockErrorResponse = mock(ErrorResponse.class);
        when(mockErrorResponse.getPsuMessage()).thenReturn(errorResponsePsuMessage);

        // when
        boolean predicateResult = errorResponsePredicate.test(mockErrorResponse);

        // then
        assertThat(predicateResult).isTrue();
    }

    @Test
    @Parameters(value = {"", "totally_different", "test_psX_message", "test_ssss_psu_message"})
    public void psuMessageContainsPredicateShouldReturnFalse(String errorResponsePsuMessage) {
        // given
        Predicate<ErrorResponse> errorResponsePredicate =
                ErrorResponse.psuMessageContainsPredicate(TEST_PSU_MESSAGE);
        ErrorResponse mockErrorResponse = mock(ErrorResponse.class);
        when(mockErrorResponse.getPsuMessage()).thenReturn(errorResponsePsuMessage);

        // when
        boolean predicateResult = errorResponsePredicate.test(mockErrorResponse);

        // then
        assertThat(predicateResult).isFalse();
    }

    @Test
    public void psuMessageContainsPredicateShouldHandleNullProperly() {
        // given
        Predicate<ErrorResponse> errorResponsePredicate =
                ErrorResponse.psuMessageContainsPredicate(TEST_PSU_MESSAGE);
        ErrorResponse mockErrorResponse = mock(ErrorResponse.class);
        when(mockErrorResponse.getPsuMessage()).thenReturn(null);

        // when
        boolean predicateResult = errorResponsePredicate.test(mockErrorResponse);

        // then
        assertThat(predicateResult).isFalse();
    }
}
