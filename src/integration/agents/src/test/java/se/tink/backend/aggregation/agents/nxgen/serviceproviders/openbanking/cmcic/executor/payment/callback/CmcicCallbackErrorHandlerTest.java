package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment.callback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.payment.DebtorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;

@RunWith(Parameterized.class)
public class CmcicCallbackErrorHandlerTest {

    private final Map<String, String> callbackData;
    private final Class expectedExceptionType;

    @Parameters(name = "{index} Should throw exception {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[][] {
                    {
                        ImmutableMap.of(
                                "state",
                                "stateValue",
                                "error",
                                "access_denied",
                                "error_description",
                                "operation canceled by the client"),
                        PaymentCancelledException.class
                    },
                    {
                        ImmutableMap.of(
                                "state",
                                "stateValue",
                                "error",
                                "access_denied",
                                "error_description",
                                "Canceled by user"),
                        PaymentCancelledException.class
                    },
                    {
                        ImmutableMap.of(
                                "state",
                                "stateValue",
                                "error",
                                "access_denied",
                                "error_description",
                                "The PSU cancelled the operation"),
                        PaymentCancelledException.class
                    },
                    {
                        ImmutableMap.of(
                                "state",
                                "stateValue",
                                "error",
                                "invalid_request",
                                "error_description",
                                "Compte à créditer identique au compte à débiter"),
                        DebtorValidationException.class
                    },
                    {
                        ImmutableMap.of(
                                "state",
                                "stateValue",
                                "error",
                                "invalid_request",
                                "error_description",
                                "Vous n'êtes pas autorisé à saisir un bénéficiaire européen"),
                        PaymentRejectedException.class
                    },
                    {
                        ImmutableMap.of(
                                "state",
                                "stateValue",
                                "error",
                                "invalid_request",
                                "error_description",
                                "L'intitulé bénéf. ne peut contenir uniquement le symbole '"),
                        PaymentValidationException.class
                    },
                    {
                        ImmutableMap.of(
                                "state",
                                "stateValue",
                                "error",
                                "unknownError",
                                "error_description",
                                "operation canceled by the client"),
                        PaymentRejectedException.class
                    },
                });
    }

    public CmcicCallbackErrorHandlerTest(
            Map<String, String> callbackData, Class expectedExceptionType) {
        this.callbackData = callbackData;
        this.expectedExceptionType = expectedExceptionType;
    }

    @Test
    public void shouldThrowExpectedException() {
        // given:
        CmcicCallbackErrorHandler cmcicCallbackErrorHandler = CmcicCallbackErrorHandler.create();

        CmcicCallbackData cmcicCallbackData = Mockito.mock(CmcicCallbackData.class);
        given(cmcicCallbackData.getExpectedCallbackData()).willReturn(callbackData);

        // when:
        Throwable throwable =
                catchThrowable(() -> cmcicCallbackErrorHandler.handleCallback(cmcicCallbackData));

        // then:
        assertThat(throwable).isNotNull();
        assertThat(throwable).isInstanceOf(expectedExceptionType);
    }
}
