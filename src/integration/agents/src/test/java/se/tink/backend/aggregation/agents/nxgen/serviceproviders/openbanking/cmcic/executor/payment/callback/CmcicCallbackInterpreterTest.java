package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment.callback;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.Map;
import org.junit.Test;
import org.mockito.Mockito;

public class CmcicCallbackInterpreterTest {

    @Test
    public void shouldHandleCallbackData() {

        // given:
        CmcicCallbackHandlingStrategy defaultStrategy =
                Mockito.mock(CmcicCallbackHandlingStrategy.class);
        CmcicCallbackHandlingStrategy handlingStrategy =
                Mockito.mock(CmcicCallbackHandlingStrategy.class);
        Map<CmcicCallbackStatus, CmcicCallbackHandlingStrategy> mapping = Mockito.mock(Map.class);
        CmcicCallbackDataFactory factory = Mockito.mock(CmcicCallbackDataFactory.class);
        CmcicCallbackInterpreter cmcicCallbackInterpreter =
                new CmcicCallbackInterpreter(defaultStrategy, mapping, factory);
        Map<String, String> callbackData = Mockito.mock(Map.class);
        CmcicCallbackData cmcicCallbackData = Mockito.mock(CmcicCallbackData.class);
        CmcicCallbackStatus status = CmcicCallbackStatus.SUCCESS;
        given(cmcicCallbackData.getStatus()).willReturn(status);
        given(factory.fromCallbackData(callbackData)).willReturn(cmcicCallbackData);
        given(mapping.getOrDefault(status, defaultStrategy)).willReturn(handlingStrategy);

        // when:
        cmcicCallbackInterpreter.handleCallbackData(callbackData);

        // then:
        then(factory).should().fromCallbackData(callbackData);
        then(handlingStrategy).should().handleCallback(cmcicCallbackData);
    }
}
