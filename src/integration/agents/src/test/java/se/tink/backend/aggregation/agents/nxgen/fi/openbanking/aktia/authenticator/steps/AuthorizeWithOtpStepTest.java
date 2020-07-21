package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.CURRENT_OTP_CARD;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.NEXT_OTP_INDEX;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.OTP_CODE;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.createAuthenticationRequest;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.createOtpInfoDto;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.AktiaOtpDataStorage;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.data.ExchangeOtpCodeStatus;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.helpers.AktiaOtpCodeExchanger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;

public class AuthorizeWithOtpStepTest {

    private AuthorizeWithOtpStep authorizeWithOtpStep;

    private AktiaOtpCodeExchanger otpCodeExchangerMock;

    private AktiaOtpDataStorage otpDataStorageMock;

    private Field signCodeDescriptionFieldMock;

    @Before
    public void setUp() {
        final SupplementalInformationFormer supplementalInformationFormerMock =
                getSupplementalInformationFormerMock();

        otpCodeExchangerMock = mock(AktiaOtpCodeExchanger.class);
        when(otpCodeExchangerMock.exchangeCode(OTP_CODE))
                .thenReturn(ExchangeOtpCodeStatus.ACCEPTED);

        otpDataStorageMock = mock(AktiaOtpDataStorage.class);
        when(otpDataStorageMock.getInfo()).thenReturn(Optional.of(createOtpInfoDto()));

        authorizeWithOtpStep =
                new AuthorizeWithOtpStep(
                        supplementalInformationFormerMock,
                        otpCodeExchangerMock,
                        otpDataStorageMock);
    }

    @Test
    public void shouldReturnRequestForSupplementInformation()
            throws AuthenticationException, AuthorizationException {
        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();
        final ArgumentCaptor<String> signCodeDescriptionCaptor =
                ArgumentCaptor.forClass(String.class);

        doNothing()
                .when(signCodeDescriptionFieldMock)
                .setValue(signCodeDescriptionCaptor.capture());

        // when
        final AuthenticationStepResponse returnedResponse =
                authorizeWithOtpStep.execute(authenticationRequest);

        // then
        assertThat(returnedResponse.getNextStepId().isPresent()).isFalse();
        assertThat(returnedResponse.isAuthenticationFinished()).isFalse();
        assertThat(returnedResponse.getSupplementInformationRequester().isPresent()).isTrue();

        verify(otpCodeExchangerMock, never()).exchangeCode(OTP_CODE);
        verify(otpDataStorageMock, never()).storeStatus(ExchangeOtpCodeStatus.ACCEPTED);

        final String actualSignCodeDescription = signCodeDescriptionCaptor.getValue();
        final String expectedSignCodeDescription = createSignDescription();
        assertThat(actualSignCodeDescription).isEqualTo(expectedSignCodeDescription);
    }

    @Test
    public void shouldReturnExecuteNextStep()
            throws AuthenticationException, AuthorizationException {
        // given
        final Map<String, String> userInputs = createUserInputs();
        final AuthenticationRequest authenticationRequest =
                createAuthenticationRequest().withUserInputs(userInputs);

        // when
        final AuthenticationStepResponse returnedResponse =
                authorizeWithOtpStep.execute(authenticationRequest);

        // then
        assertThat(returnedResponse.getNextStepId().isPresent()).isFalse();
        assertThat(returnedResponse.isAuthenticationFinished()).isFalse();
        assertThat(returnedResponse.getSupplementInformationRequester().isPresent()).isFalse();

        verify(otpCodeExchangerMock).exchangeCode(OTP_CODE);
        verify(otpDataStorageMock).storeStatus(ExchangeOtpCodeStatus.ACCEPTED);
    }

    private SupplementalInformationFormer getSupplementalInformationFormerMock() {
        signCodeDescriptionFieldMock = mock(Field.class);
        when(signCodeDescriptionFieldMock.getName())
                .thenReturn(Field.Key.SIGN_CODE_DESCRIPTION.getFieldKey());

        final Field signCodeInputFieldMock = mock(Field.class);
        when(signCodeInputFieldMock.getName()).thenReturn(Field.Key.SIGN_CODE_INPUT.getFieldKey());

        final SupplementalInformationFormer supplementalInformationFormerMock =
                mock(SupplementalInformationFormer.class);

        when(supplementalInformationFormerMock.getField(Field.Key.SIGN_CODE_DESCRIPTION))
                .thenReturn(signCodeDescriptionFieldMock);
        when(supplementalInformationFormerMock.getField(Field.Key.SIGN_CODE_INPUT))
                .thenReturn(signCodeInputFieldMock);

        return supplementalInformationFormerMock;
    }

    private static Map<String, String> createUserInputs() {
        return ImmutableMap.of(
                Field.Key.SIGN_CODE_DESCRIPTION.getFieldKey(),
                createSignDescription(),
                Field.Key.SIGN_CODE_INPUT.getFieldKey(),
                OTP_CODE);
    }

    private static String createSignDescription() {
        return String.format("card %s with index %s", CURRENT_OTP_CARD, NEXT_OTP_INDEX);
    }
}
