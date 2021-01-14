package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.InitializeLoginResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AccountBlockedError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AuthenticationErrorHandlerTest {

    @Test
    public void shouldIdentifyAccountBlocked() {

        // given
        String response =
                "{\"value\":null,\"businessMessageBulk\":{\"messages\":[{\"ind\":\"E\",\"code\":\"EBW0501\",\"text\":\"\"}],\"pewCode\":\"PEW0501\",\"globalIndicator\":\"E\",\"text\":\"\"}}";

        InitializeLoginResponse initializeLoginResponse =
                SerializationUtils.deserializeFromString(response, InitializeLoginResponse.class);

        // when
        AgentBankApiError error =
                AuthenticationErrorHandler.getError(initializeLoginResponse, true);

        // then
        AccountBlockedError expected = new AccountBlockedError();
        assertThat(error.getDetails().getErrorMessage())
                .isEqualTo(expected.getDetails().getErrorMessage());
        assertThat(error.getDetails().getErrorCode())
                .isEqualTo(expected.getDetails().getErrorCode());
    }

    @Test
    public void shouldThrowWhenNoError() {

        // given
        String response =
                "{\"value\":{\"cardInfo\":{\"authenticationFactorId\":\"xxxx\",\"cardFrameId\":\"xxxx\"},\"userInfo\":{\"isMinor\":false},\"channelAgreements\":[{\"agreementId\":\"xxxx\",\"webStatus\":\"ACT\",\"mobileStatus\":\"ACT\"}],\"authenticationMeans\":[{\"authenticationMeanId\":\"08\",\"name\":\"UCR\",\"dacLevel\":\"5\"},{\"authenticationMeanId\":\"21\",\"name\":\"SDBL\",\"dacLevel\":\"3\"},{\"authenticationMeanId\":\"30\",\"name\":\"EAPI\",\"dacLevel\":\"4\"},{\"authenticationMeanId\":\"31\",\"name\":\"EAFI\",\"dacLevel\":\"4\"},{\"authenticationMeanId\":\"32\",\"name\":\"EAFR\",\"dacLevel\":\"4\"}],\"ucr\":{\"signature\":{\"challenges\":[\"63751091\"]}},\"itsme\":null,\"easyPin\":null},\"businessMessageBulk\":{\"messages\":[],\"pewCode\":null,\"globalIndicator\":null,\"text\":\"\"}}";

        InitializeLoginResponse initializeLoginResponse =
                SerializationUtils.deserializeFromString(response, InitializeLoginResponse.class);

        // when
        ThrowingCallable callable =
                () -> AuthenticationErrorHandler.getError(initializeLoginResponse, true);

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No error, check error using isError method first");
    }
}
