package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.FinancialService;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsUserState;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.libraries.credentials.service.ManualAuthenticateRequest;
import se.tink.libraries.credentials.service.RefreshScope;

@RunWith(MockitoJUnitRunner.class)
public class AccountSegmentSpecificationAuthenticationStepTest {

    private static final String ACCOUNT_SEGMENT_FIELD_NAME = "accountSegment";

    private AccountSegmentSpecificationAuthenticationStep objectUnderTest;

    @Mock private SupplementalInformationFormer supplementalInformationFormer;

    @Mock private Field accountSegmentField;

    @Mock private SibsUserState sibsUserState;

    private AuthenticationRequest authenticationRequest;

    @Mock private ManualAuthenticateRequest manualAuthenticateRequest;

    @Mock private RefreshScope refreshScope;

    @Before
    public void init() {
        authenticationRequest = new AuthenticationRequest(Mockito.mock(Credentials.class));
        Mockito.when(manualAuthenticateRequest.getRefreshScope()).thenReturn(refreshScope);
        Mockito.when(refreshScope.getFinancialServiceSegmentsIn())
                .thenReturn(Collections.emptySet());
        Mockito.when(accountSegmentField.getName()).thenReturn(ACCOUNT_SEGMENT_FIELD_NAME);
        Mockito.when(supplementalInformationFormer.getField(ACCOUNT_SEGMENT_FIELD_NAME))
                .thenReturn(accountSegmentField);
        objectUnderTest =
                new AccountSegmentSpecificationAuthenticationStep(
                        sibsUserState, supplementalInformationFormer, manualAuthenticateRequest);
    }

    @Test
    public void shouldReturnExecuteNextStepResponseImmediatelyWhenConsentIdExists() {
        // given
        Mockito.when(sibsUserState.hasConsentId()).thenReturn(true);
        Mockito.when(sibsUserState.isAccountSegmentNotSpecified()).thenReturn(true);

        // when
        AuthenticationStepResponse result = objectUnderTest.execute(authenticationRequest);

        // then
        Assertions.assertThat(result.isAuthenticationFinished()).isFalse();
        Assertions.assertThat(result.getSupplementInformationRequester()).isEmpty();
        Mockito.verify(sibsUserState, Mockito.never()).specifyAccountSegment(Mockito.any());
    }

    @Test
    public void shouldRequestForSegmentSupplementInformation() {
        // given
        Mockito.when(sibsUserState.hasConsentId()).thenReturn(false);
        Mockito.when(sibsUserState.isAccountSegmentNotSpecified()).thenReturn(true);

        // when
        AuthenticationStepResponse result = objectUnderTest.execute(authenticationRequest);

        // then
        Assertions.assertThat(result.isAuthenticationFinished()).isFalse();
        Assertions.assertThat(result.getSupplementInformationRequester()).isPresent();
        Assertions.assertThat(
                        result.getSupplementInformationRequester()
                                .get()
                                .getFields()
                                .get()
                                .iterator()
                                .next())
                .isEqualTo(accountSegmentField);
    }

    @Test
    public void shouldStoreAccountSegmentAndReturnExecuteNextStepResponse() {
        // given
        Mockito.when(sibsUserState.hasConsentId()).thenReturn(false);
        Mockito.when(sibsUserState.isAccountSegmentNotSpecified()).thenReturn(true);
        Map<String, String> userInputs = new HashMap<>();
        userInputs.put(ACCOUNT_SEGMENT_FIELD_NAME, SibsAccountSegment.BUSINESS.name());
        authenticationRequest.withUserInputs(userInputs);

        // when
        AuthenticationStepResponse result = objectUnderTest.execute(authenticationRequest);

        // then
        Assertions.assertThat(result.isAuthenticationFinished()).isFalse();
        Assertions.assertThat(result.getSupplementInformationRequester().isPresent()).isFalse();
        Mockito.verify(sibsUserState).specifyAccountSegment(SibsAccountSegment.BUSINESS);
    }

    @Test
    public void shouldFetchBusinessAccountSegmentFromCredentialsRequest() {
        // given
        Mockito.when(refreshScope.getFinancialServiceSegmentsIn())
                .thenReturn(
                        Sets.newSet(
                                FinancialService.FinancialServiceSegment.BUSINESS,
                                FinancialService.FinancialServiceSegment.UNDETERMINED));
        Mockito.when(sibsUserState.isAccountSegmentNotSpecified()).thenReturn(true);

        // when
        AuthenticationStepResponse result = objectUnderTest.execute(authenticationRequest);

        // then
        Assertions.assertThat(result.isAuthenticationFinished()).isFalse();
        Assertions.assertThat(result.getSupplementInformationRequester()).isEmpty();
        Assertions.assertThat(result.getSupplementInformationRequester()).isEmpty();
        Mockito.verify(sibsUserState).specifyAccountSegment(SibsAccountSegment.BUSINESS);
    }

    @Test
    public void shouldFetchPersonalAccountSegmentFromCredentialsRequest() {
        // given
        Mockito.when(refreshScope.getFinancialServiceSegmentsIn())
                .thenReturn(
                        Sets.newSet(
                                FinancialService.FinancialServiceSegment.PERSONAL,
                                FinancialService.FinancialServiceSegment.UNDETERMINED));
        Mockito.when(sibsUserState.isAccountSegmentNotSpecified()).thenReturn(true);

        // when
        AuthenticationStepResponse result = objectUnderTest.execute(authenticationRequest);

        // then
        Assertions.assertThat(result.isAuthenticationFinished()).isFalse();
        Assertions.assertThat(result.getSupplementInformationRequester()).isEmpty();
        Assertions.assertThat(result.getSupplementInformationRequester()).isEmpty();
        Mockito.verify(sibsUserState).specifyAccountSegment(SibsAccountSegment.PERSONAL);
    }

    @Test
    public void
            shouldRequestForSegmentSupplementInformationWhenCredentialsRequestContainsBothBusinessAndPersonalSegments() {
        // given
        Mockito.when(refreshScope.getFinancialServiceSegmentsIn())
                .thenReturn(
                        Sets.newSet(
                                FinancialService.FinancialServiceSegment.PERSONAL,
                                FinancialService.FinancialServiceSegment.BUSINESS));
        Mockito.when(sibsUserState.hasConsentId()).thenReturn(false);
        Mockito.when(sibsUserState.isAccountSegmentNotSpecified()).thenReturn(true);

        // when
        AuthenticationStepResponse result = objectUnderTest.execute(authenticationRequest);

        // then
        Assertions.assertThat(result.isAuthenticationFinished()).isFalse();
        Assertions.assertThat(result.getSupplementInformationRequester()).isPresent();
        Assertions.assertThat(
                        result.getSupplementInformationRequester()
                                .get()
                                .getFields()
                                .get()
                                .iterator()
                                .next())
                .isEqualTo(accountSegmentField);
        Mockito.verify(sibsUserState, Mockito.never()).specifyAccountSegment(Mockito.any());
    }

    @Test
    public void
            shouldRequestForSegmentSupplementInformationWhenCredentialsRequestContainsUndeterminedFinancialService() {
        // given
        Mockito.when(refreshScope.getFinancialServiceSegmentsIn())
                .thenReturn(Sets.newSet(FinancialService.FinancialServiceSegment.UNDETERMINED));
        Mockito.when(sibsUserState.hasConsentId()).thenReturn(false);
        Mockito.when(sibsUserState.isAccountSegmentNotSpecified()).thenReturn(true);

        // when
        AuthenticationStepResponse result = objectUnderTest.execute(authenticationRequest);

        // then
        Assertions.assertThat(result.isAuthenticationFinished()).isFalse();
        Assertions.assertThat(result.getSupplementInformationRequester()).isPresent();
        Assertions.assertThat(
                        result.getSupplementInformationRequester()
                                .get()
                                .getFields()
                                .get()
                                .iterator()
                                .next())
                .isEqualTo(accountSegmentField);
        Mockito.verify(sibsUserState, Mockito.never()).specifyAccountSegment(Mockito.any());
    }
}
