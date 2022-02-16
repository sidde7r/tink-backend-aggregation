package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.toggle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.libraries.unleash.UnleashClient;
import se.tink.libraries.unleash.model.Toggle;

@RunWith(MockitoJUnitRunner.class)
public class UkOpenBankingFlowServiceTest {

    @Mock private UnleashClient unleashClient;

    @Mock private Toggle toggle;

    private UkOpenBankingFlowService toggleService;

    @Before
    public void setUp() throws Exception {
        this.toggleService = new UkOpenBankingFlowService(unleashClient, toggle);
    }

    @Test
    public void shouldReturnEidasProxyFlowWhenTheToggleWasEnable() {
        // given
        when(unleashClient.isToggleEnabled(toggle)).thenReturn(true);

        // when
        UkOpenBankingFlow ukOpenBankingFlow = toggleService.takeFlow();

        // then
        assertThat(ukOpenBankingFlow).isEqualTo(UkOpenBankingFlow.EIDAS_PROXY);
    }

    @Test
    public void shouldReturnSecretServiceFlowWhenTheToggleWasUnable() {
        // given
        when(unleashClient.isToggleEnabled(toggle)).thenReturn(false);

        // when
        UkOpenBankingFlow ukOpenBankingFlow = toggleService.takeFlow();

        // then
        assertThat(ukOpenBankingFlow).isEqualTo(UkOpenBankingFlow.SECRET_SERVICE);
    }
}
