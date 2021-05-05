package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;

public class SdcAuthenticationControllerTest {

    private static final String DUMMY_STATE = "dummyState";
    private static final String DUMMY_ERROR = "dummyState";
    private static final String DUMMY_DESCRIPTION = "dummyState";

    private SdcAuthenticationController controller;
    private SupplementalInformationHelper supplementalInformationHelper;
    private StrongAuthenticationState state;

    @Before
    public void init() {
        supplementalInformationHelper = mock(SupplementalInformationHelper.class);
        SdcAuthenticator authenticator = mock(SdcAuthenticator.class);
        state = mock(StrongAuthenticationState.class);
        controller =
                new SdcAuthenticationController(
                        supplementalInformationHelper, authenticator, state);
    }

    @Test
    public void authenticateShouldThrowExceptionIfErrorInCallbackReceived() {
        // given
        Map<String, String> callbackData =
                ImmutableMap.of("error", DUMMY_ERROR, "error_description", DUMMY_DESCRIPTION);
        when(state.getSupplementalKey()).thenReturn(DUMMY_STATE);
        when(supplementalInformationHelper.waitForSupplementalInformation(
                        anyString(), anyLong(), any()))
                .thenReturn(Optional.of(callbackData));

        // when
        Throwable exception = catchThrowable(() -> controller.collect("dummy"));

        // then
        assertThat(exception)
                .isInstanceOf(LoginException.class)
                .hasMessageContaining(DUMMY_ERROR)
                .hasMessageContaining(DUMMY_DESCRIPTION);
    }
}
