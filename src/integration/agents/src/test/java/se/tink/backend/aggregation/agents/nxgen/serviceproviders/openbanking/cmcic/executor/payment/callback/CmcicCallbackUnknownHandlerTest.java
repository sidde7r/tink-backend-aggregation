package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment.callback;

import static org.mockito.BDDMockito.given;

import java.util.HashMap;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;

public class CmcicCallbackUnknownHandlerTest {

    @Test(expected = PaymentException.class)
    public void shouldThrowException() {

        // given:
        CmcicCallbackUnknownHandler strategy = new CmcicCallbackUnknownHandler();
        CmcicCallbackData cmcicCallbackData = Mockito.mock(CmcicCallbackData.class);
        given(cmcicCallbackData.getStatus()).willReturn(CmcicCallbackStatus.SUCCESS);
        given(cmcicCallbackData.getUnexpectedCallbackData()).willReturn(new HashMap<>());

        // when:
        strategy.handleCallback(cmcicCallbackData);

        // then:
        // PaymentException is thrown

    }
}
