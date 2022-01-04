package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment.callback;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.Map;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.apiclient.CmcicRepository;

public class CmcicCallbackSuccessHandlerTest {

    @Test
    public void shouldAddCodeToRepository() {

        // given:
        CmcicRepository cmcicRepository = Mockito.mock(CmcicRepository.class);
        CmcicCallbackSuccessHandler successStrategy =
                new CmcicCallbackSuccessHandler(cmcicRepository);
        CmcicCallbackData cmcicCallbackData = Mockito.mock(CmcicCallbackData.class);
        Map<String, String> expectedCallbackData = Mockito.mock(Map.class);
        String codeValue = "codeValue";
        given(expectedCallbackData.get("code")).willReturn(codeValue);
        given(cmcicCallbackData.getExpectedCallbackData()).willReturn(expectedCallbackData);

        // when:
        successStrategy.handleCallback(cmcicCallbackData);

        // then:
        then(cmcicRepository).should().storeAuthorizationCode(codeValue);
    }
}
