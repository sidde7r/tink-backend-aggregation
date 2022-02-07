package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import agents_platform_agents_framework.org.springframework.http.RequestEntity;
import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import java.util.Collections;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import se.tink.backend.aggregation.agents.agentplatform.AgentPlatformHttpClient;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.auth.PaymentAccessToken;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.secrets.StarlingSecrets;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;

public class StarlingPaymentAuthenticatorTest {

    AgentConfiguration<StarlingSecrets> agentConfiguration = mock(AgentConfiguration.class);
    AgentPlatformHttpClient agentHttpClient = mock(AgentPlatformHttpClient.class);
    StarlingSecrets starlingSecrets = mock(StarlingSecrets.class);

    @Before
    public void setup() {
        when(agentConfiguration.getProviderSpecificConfiguration()).thenReturn(starlingSecrets);
        when(agentConfiguration.getRedirectUrl())
                .thenReturn("https://127.0.0.1:7357/api/v1/thirdparty/callback");
        when(starlingSecrets.getAisClientId()).thenReturn("a1b2c3d4");
        when(starlingSecrets.getAisClientSecret()).thenReturn("e5f6H7G8");
    }

    @Test
    public void testBuildAuthorizeUrl() {
        StarlingPaymentAuthenticator starlingPaymentAuthenticator =
                new StarlingPaymentAuthenticator(agentConfiguration, agentHttpClient);
        Assertions.assertThat(starlingPaymentAuthenticator.buildAuthorizeUrl("state").toString())
                .isEqualTo(
                        "https://oauth.starlingbank.com?client_id=a1b2c3d4&redirect_uri=https://127.0.0.1:7357/api/v1/thirdparty/callback&response_type=code&scope=account-list:read+account-identifier:read+pay-local-once:create+pay-local:read&state=state");
    }

    @Test
    public void testExchangeAuthorizationCode() {
        StarlingPaymentAuthenticator starlingPaymentAuthenticator =
                new StarlingPaymentAuthenticator(agentConfiguration, agentHttpClient);
        ResponseEntity<PaymentAccessToken> responseEntity = mock(ResponseEntity.class);
        when(agentHttpClient.exchange(
                        any(RequestEntity.class), eq(PaymentAccessToken.class), eq(null)))
                .thenReturn(responseEntity);
        when(responseEntity.getBody()).thenReturn(new PaymentAccessToken());
        ArgumentCaptor<RequestEntity> argumentCaptor = ArgumentCaptor.forClass(RequestEntity.class);

        starlingPaymentAuthenticator.exchangeAuthorizationCode("code");
        verify(agentHttpClient)
                .exchange(argumentCaptor.capture(), eq(PaymentAccessToken.class), eq(null));
        Assertions.assertThat(argumentCaptor.getValue().getMethod()).hasToString("POST");
        Assertions.assertThat(argumentCaptor.getValue().getBody())
                .hasToString(
                        "code=code&grant_type=authorization_code&redirect_uri=https://127.0.0.1:7357/api/v1/thirdparty/callback&client_secret=e5f6H7G8&client_id=a1b2c3d4");
        Assertions.assertThat(argumentCaptor.getValue().getHeaders().get("Content-Type"))
                .isEqualTo(Collections.singletonList("application/x-www-form-urlencoded"));
    }
}
