package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants.StepIdentifiers;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants.TemporaryStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.entities.PinScaEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.entities.SmsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.ScaResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.step.BankiaSignatureStep;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AutomaticAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.OtpStep;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class LaCaixaManualAuthenticatorTest {
    private LaCaixaApiClient apiClient;
    private Credentials credentials;
    private LaCaixaManualAuthenticator laCaixaManualAuthenticator;
    private SessionStorage sessionStorage;

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/lacaixa/resources";

    @Before
    public void init() {
        apiClient = mock(LaCaixaApiClient.class);
        credentials = mock(Credentials.class);
        sessionStorage = new SessionStorage();
        laCaixaManualAuthenticator =
                new LaCaixaManualAuthenticator(
                        apiClient,
                        mock(PersistentStorage.class),
                        mock(LogMasker.class),
                        mock(SupplementalInformationFormer.class),
                        mock(Catalog.class),
                        credentials,
                        mock(SupplementalInformationHelper.class),
                        sessionStorage);
    }

    @Test
    public void should_route_to_BankiaSignatureStep_if_Pin2Bankia_authentication_flow() {
        // given
        List<AuthenticationStep> steps = laCaixaManualAuthenticator.getAuthenticationSteps();
        AutomaticAuthenticationStep initiateEnrollmentStep =
                (AutomaticAuthenticationStep)
                        steps.stream()
                                .filter(
                                        step ->
                                                step.getIdentifier()
                                                        .equalsIgnoreCase(
                                                                StepIdentifiers
                                                                        .INITIALIZE_ENROLMENT))
                                .findFirst()
                                .get();
        AuthenticationRequest request = new AuthenticationRequest(credentials);
        ScaResponse scaResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "pin2_bankia_scaResponse.json").toFile(),
                        ScaResponse.class);
        when(apiClient.initiateEnrolment()).thenReturn(scaResponse);

        // when
        AuthenticationStepResponse response = initiateEnrollmentStep.execute(request);

        // then
        assertThat(response.getNextStepId().get()).isEqualTo(BankiaSignatureStep.class.getName());

        assertThat(sessionStorage.get(TemporaryStorage.PIN_BANKIA, PinScaEntity.class).get())
                .isEqualTo(scaResponse.getPin2ScaBankia());
    }

    @Test
    public void should_route_to_smsStep_if_smsotp_authentication_flow() {
        // given
        List<AuthenticationStep> steps = laCaixaManualAuthenticator.getAuthenticationSteps();
        AutomaticAuthenticationStep initiateEnrollmentStep =
                (AutomaticAuthenticationStep)
                        steps.stream()
                                .filter(
                                        step ->
                                                step.getIdentifier()
                                                        .equalsIgnoreCase(
                                                                StepIdentifiers
                                                                        .INITIALIZE_ENROLMENT))
                                .findFirst()
                                .get();
        AuthenticationRequest request = new AuthenticationRequest(credentials);

        ScaResponse scaResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "smsotp_scaResponse.json").toFile(),
                        ScaResponse.class);

        when(apiClient.initiateEnrolment()).thenReturn(scaResponse);

        // when
        AuthenticationStepResponse response = initiateEnrollmentStep.execute(request);

        // then
        assertThat(response.getNextStepId().get()).isEqualTo(OtpStep.class.getName());
        assertThat(sessionStorage.get(TemporaryStorage.SCA_SMS, SmsEntity.class).get())
                .isEqualTo(scaResponse.getSms());
    }

    @Test
    public void should_route_to_pushStep_if_pushotp_authentication_flow() {
        // given
        List<AuthenticationStep> steps = laCaixaManualAuthenticator.getAuthenticationSteps();
        AutomaticAuthenticationStep initiateEnrollmentStep =
                (AutomaticAuthenticationStep)
                        steps.stream()
                                .filter(
                                        step ->
                                                step.getIdentifier()
                                                        .equalsIgnoreCase(
                                                                StepIdentifiers
                                                                        .INITIALIZE_ENROLMENT))
                                .findFirst()
                                .get();
        AuthenticationRequest request = new AuthenticationRequest(credentials);
        when(apiClient.initiateEnrolment())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "pushotp_scaResponse.json").toFile(),
                                ScaResponse.class));

        // when
        AuthenticationStepResponse response = initiateEnrollmentStep.execute(request);

        // then
        assertThat(response.getNextStepId().get()).isEqualTo(StepIdentifiers.APP_SIGN);
    }
}
