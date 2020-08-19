package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.errorhandling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.File;
import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.AssertFormResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DefaultResponseErrorHandlingTest {

    private static final String INCORRECT_CARD_NUMBER_FILE =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/be/banks/axa/resources/incorrectCardNumber.json";

    private static final String INCORRECT_CHALLENGE_RESPONSE_FILE =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/be/banks/axa/resources/incorrectChallengeResponse.json";

    private static final String DEVICES_LIMIT_REACHED_FILE =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/be/banks/axa/resources/devicesLimitReached.json";

    private static final String NOT_A_CUSTOMER_FILE =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/be/banks/axa/resources/notACustomer.json";

    private static final String ASSERTION_NO_DETAILS_FILE =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/be/banks/axa/resources/assertionWithoutDetails.json";

    private static final String ASSERTION_UNKNOWN_CODE_FILE =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/be/banks/axa/resources/assertionUnknownCode.json";

    private static final String GENERIC_UNKNOWN_ERROR_FILE =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/be/banks/axa/resources/genericUnknownError.json";

    private static final String SUCCESS_FILE =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/be/banks/axa/resources/successMessage.json";

    private static final ResponseErrorHandler handler = ResponseErrorHandlingBuilder.DEFAULT_CHAIN;

    @Test
    public void shouldThrowLoginExceptionForIncorrectCardNumber() {
        // given
        AssertFormResponse response = deserializeFrom(INCORRECT_CARD_NUMBER_FILE);

        // when
        Throwable throwable = catchThrowable(() -> handler.handleError(response));

        // then
        assertThat(throwable)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }

    @Test
    public void shouldThrowLoginExceptionForIncorrectChallengeResponse() {
        // given
        AssertFormResponse response = deserializeFrom(INCORRECT_CHALLENGE_RESPONSE_FILE);

        // when
        Throwable throwable = catchThrowable(() -> handler.handleError(response));

        // then
        assertThat(throwable)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CHALLENGE_RESPONSE");
    }

    @Test
    public void shouldThrowLoginExceptionForDeviceLimitReached() {
        // given
        AssertFormResponse response = deserializeFrom(DEVICES_LIMIT_REACHED_FILE);

        // when
        Throwable throwable = catchThrowable(() -> handler.handleError(response));

        // then
        assertThat(throwable)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.REGISTER_DEVICE_ERROR");
    }

    @Test
    public void shouldThrowLoginExceptionForNoCustomer() {
        // given
        AssertFormResponse response = deserializeFrom(NOT_A_CUSTOMER_FILE);

        // when
        Throwable throwable = catchThrowable(() -> handler.handleError(response));

        // then
        assertThat(throwable)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.NOT_CUSTOMER");
    }

    @Test
    public void shouldThrowLoginExceptionForUnknownCode() {
        // given
        AssertFormResponse response = deserializeFrom(ASSERTION_UNKNOWN_CODE_FILE);

        // when
        Throwable throwable = catchThrowable(() -> handler.handleError(response));

        // then
        assertThat(throwable)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.DEFAULT_MESSAGE");
    }

    @Test
    public void shouldThrowLoginExceptionForNoDetails() {
        // given
        AssertFormResponse response = deserializeFrom(ASSERTION_NO_DETAILS_FILE);

        // when
        Throwable throwable = catchThrowable(() -> handler.handleError(response));

        // then
        assertThat(throwable)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.DEFAULT_MESSAGE");
    }

    @Test
    public void shouldThrowBankServiceExceptionForUnknownError() {
        // given
        AssertFormResponse response = deserializeFrom(GENERIC_UNKNOWN_ERROR_FILE);

        // when
        Throwable throwable = catchThrowable(() -> handler.handleError(response));

        // then
        assertThat(throwable)
                .isExactlyInstanceOf(BankServiceException.class)
                .hasMessage("Error response [code: 2, message: Unknown error occured]");
    }

    @Test
    public void shouldNotThrowForSuccessMessage() {
        // given
        AssertFormResponse response = deserializeFrom(SUCCESS_FILE);

        // when
        AbstractThrowableAssert<?, ? extends Throwable> code =
                assertThatCode(() -> handler.handleError(response));

        // then
        code.doesNotThrowAnyException();
    }

    private AssertFormResponse deserializeFrom(String filePath) {
        return SerializationUtils.deserializeFromString(
                new File(filePath), AssertFormResponse.class);
    }
}
