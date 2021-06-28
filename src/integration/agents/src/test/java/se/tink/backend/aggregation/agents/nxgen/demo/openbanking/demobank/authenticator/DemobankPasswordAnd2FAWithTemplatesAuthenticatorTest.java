package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.embeddedotp.EmbeddedChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.embeddedotp.EmbeddedCompleteResponse;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.sdktemplates.TemplatesDataBuilder;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.appcode.AppCodeTemplate;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.appcode.dto.AppCodeData;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.decoupled.DecoupledWithChangeMethodTemplate;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.decoupled.dto.DecoupledWithChangeMethodData;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DemobankPasswordAnd2FAWithTemplatesAuthenticatorTest {

    private static final String OTP_CODE = "8338";
    private static final String CHALLENGE_RESPONSE =
            "{\"message\": \"provide otp code" + OTP_CODE + " to log in\"}";
    private static final String COMPLETED_CHALLENGE_RESPONSE =
            "{\"refreshToken\": \"refresh_token\", \"accessToken\": \"access_token\", \"expiresIn\": 3600, \"refreshExpiresIn\": 3600}";

    // Mocks
    private DemobankApiClient demobankApiClient;
    private SupplementalInformationController supplementalInformationController;
    private Credentials credentials;

    private InOrder inOrder;

    // Tested
    private DemobankPasswordAnd2FAWithTemplatesAuthenticator authenticator;

    @Before
    public void setup() {
        demobankApiClient = mock(DemobankApiClient.class);
        supplementalInformationController = mock(SupplementalInformationController.class);
        inOrder = inOrder(supplementalInformationController);
        credentials = mock(Credentials.class);

        authenticator =
                new DemobankPasswordAnd2FAWithTemplatesAuthenticator(
                        demobankApiClient, supplementalInformationController);

        // given
        when(credentials.getField(Field.Key.USERNAME)).thenReturn("USERNAME");
        when(credentials.getField(Field.Key.PASSWORD)).thenReturn("PASSWORD");
        EmbeddedChallengeResponse embeddedChallengeResponse =
                SerializationUtils.deserializeFromString(
                        CHALLENGE_RESPONSE, EmbeddedChallengeResponse.class);
        when(demobankApiClient.initEmbeddedOtp(
                        credentials.getField(Field.Key.USERNAME),
                        credentials.getField(Field.Key.PASSWORD)))
                .thenReturn(embeddedChallengeResponse);
    }

    @Test
    public void shouldHandleOneOfTheTemplatesMethod() {
        // given
        when(supplementalInformationController.askSupplementalInformationSync(any()))
                .thenReturn(ImmutableMap.of("2fa-option", "app_code"))
                .thenReturn(ImmutableMap.of("otpCode", OTP_CODE));

        EmbeddedCompleteResponse completeEmbeddedOtp =
                SerializationUtils.deserializeFromString(
                        COMPLETED_CHALLENGE_RESPONSE, EmbeddedCompleteResponse.class);
        when(demobankApiClient.completeEmbeddedOtp(
                        credentials.getField(Field.Key.USERNAME),
                        credentials.getField(Field.Key.PASSWORD),
                        OTP_CODE))
                .thenReturn(completeEmbeddedOtp);

        AppCodeData appCodeData = TemplatesDataBuilder.prepareAppCodeData(OTP_CODE);

        // when
        authenticator.authenticate(credentials);

        // then
        ArgumentCaptor<Field> argumentCaptor = ArgumentCaptor.forClass(Field.class);
        inOrder.verify(supplementalInformationController, times(2))
                .askSupplementalInformationSync(argumentCaptor.capture());

        Field choose2fa = argumentCaptor.getAllValues().get(0);
        assertThat(choose2fa.getSelectOptions().size()).isEqualTo(6);

        List<Field> templateScreen = argumentCaptor.getAllValues().subList(1, 6);
        assertThat(templateScreen).hasSameElementsAs(AppCodeTemplate.getTemplate(appCodeData));

        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void shouldHandleChangeMethod() {
        // given
        when(supplementalInformationController.askSupplementalInformationSync(any()))
                .thenReturn(ImmutableMap.of("2fa-option", "decoupled_change"))
                .thenReturn(ImmutableMap.of("CHANGE_METHOD", "true"))
                .thenReturn(ImmutableMap.of("2fa-option", "app_code"))
                .thenReturn(ImmutableMap.of("otpCode", OTP_CODE));

        EmbeddedCompleteResponse completeEmbeddedOtp =
                SerializationUtils.deserializeFromString(
                        COMPLETED_CHALLENGE_RESPONSE, EmbeddedCompleteResponse.class);
        when(demobankApiClient.completeEmbeddedOtp(
                        credentials.getField(Field.Key.USERNAME),
                        credentials.getField(Field.Key.PASSWORD),
                        "8338"))
                .thenReturn(completeEmbeddedOtp);

        DecoupledWithChangeMethodData decoupledWithChangeMethodData =
                TemplatesDataBuilder.prepareDecoupledWithChangeMethodData();
        AppCodeData appCodeData = TemplatesDataBuilder.prepareAppCodeData(OTP_CODE);

        // when
        authenticator.authenticate(credentials);

        // then
        ArgumentCaptor<Field> argumentCaptor = ArgumentCaptor.forClass(Field.class);
        inOrder.verify(supplementalInformationController, times(4))
                .askSupplementalInformationSync(argumentCaptor.capture());

        Field choose2fa = argumentCaptor.getAllValues().get(0);
        assertThat(choose2fa.getSelectOptions().size()).isEqualTo(6);

        List<Field> templateScreenWithChange = argumentCaptor.getAllValues().subList(1, 5);
        assertThat(templateScreenWithChange)
                .hasSameElementsAs(
                        DecoupledWithChangeMethodTemplate.getTemplate(
                                decoupledWithChangeMethodData));

        Field choose2fa2 = argumentCaptor.getAllValues().get(5);
        assertThat(choose2fa2.getSelectOptions().size()).isEqualTo(6);

        List<Field> templateScreenWithFinalChoice = argumentCaptor.getAllValues().subList(6, 11);
        assertThat(templateScreenWithFinalChoice)
                .hasSameElementsAs(AppCodeTemplate.getTemplate(appCodeData));

        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void shouldThrowErrorWhenUnknownTemplateIsReturn() {
        // given
        when(supplementalInformationController.askSupplementalInformationSync(any()))
                .thenReturn(ImmutableMap.of("2fa-option", "no_valid_option"));

        // when
        Throwable t = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(t).isInstanceOf(SupplementalInfoException.class);
    }
}
