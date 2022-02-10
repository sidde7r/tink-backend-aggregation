package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.nio.file.Paths;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.connectivity.ConnectivityException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants.SupplementalInformationKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants.TemporaryStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.entities.SmsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.ScaResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.OtpStep;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.i18n_aggregation.LocalizableKey;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BankiaSignatureStepTest {
    private SessionStorage sessionStorage;
    private LaCaixaApiClient apiClient;
    private BankiaSignatureStep bankiaSignatureStep;
    private AuthenticationRequest authenticationRequest;

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/lacaixa/resources";

    @Before
    public void init() {
        Catalog catalog = mock(Catalog.class);
        when(catalog.getString(any(LocalizableKey.class))).thenReturn("dummyString");
        sessionStorage = new SessionStorage();
        apiClient = mock(LaCaixaApiClient.class);
        bankiaSignatureStep = new BankiaSignatureStep(catalog, sessionStorage, apiClient);
        authenticationRequest = new AuthenticationRequest(mock(Credentials.class));
    }

    @Test
    public void should_return_SupplementalInformationRequester_if_no_supplemetal_info_provided() {
        // given
        authenticationRequest.withUserInputs(ImmutableMap.of());

        // when
        AuthenticationStepResponse stepResponse =
                bankiaSignatureStep.execute(authenticationRequest);

        // then
        assertThat(stepResponse.getSupplementInformationRequester().isPresent()).isTrue();
        Field fieldBankiaSignature =
                stepResponse.getSupplementInformationRequester().get().getFields().get().stream()
                        .filter(
                                field ->
                                        field.getName()
                                                .equalsIgnoreCase(
                                                        SupplementalInformationKeys
                                                                .BANKIA_SIGNATURE))
                        .findFirst()
                        .get();
        assertThat(fieldBankiaSignature.getName())
                .isEqualTo(SupplementalInformationKeys.BANKIA_SIGNATURE);
    }

    @Test
    public void should_return_OtpStep_if_supplemetal_info_provided_and_SMS_auth_flow() {
        // given
        Map<String, String> inputs =
                ImmutableMap.of(SupplementalInformationKeys.BANKIA_SIGNATURE, "dummyVal");
        authenticationRequest.withUserInputs(inputs);

        ScaResponse pinScaResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "pin2_bankia_scaResponse.json").toFile(),
                        ScaResponse.class);
        sessionStorage.put(TemporaryStorage.PIN_BANKIA, pinScaResponse.getPin2ScaBankia());

        ScaResponse smsScaResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "smsotp_scaResponse.json").toFile(),
                        ScaResponse.class);

        when(apiClient.authorizeSCA(any())).thenReturn(smsScaResponse);

        // when
        AuthenticationStepResponse stepResponse =
                bankiaSignatureStep.execute(authenticationRequest);

        // then
        assertThat(stepResponse.getNextStepId().get()).isEqualTo(OtpStep.class.getName());
        assertThat(sessionStorage.get(TemporaryStorage.SCA_SMS, SmsEntity.class).get())
                .isEqualTo(smsScaResponse.getSms());
    }

    @Test
    public void should_throw_exception_if_supplemetal_info_provided_and_not_SMS_auth_flow() {
        // given
        Map<String, String> inputs =
                ImmutableMap.of(SupplementalInformationKeys.BANKIA_SIGNATURE, "dummyVal");
        authenticationRequest.withUserInputs(inputs);

        ScaResponse pinScaResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "pin2_bankia_scaResponse.json").toFile(),
                        ScaResponse.class);
        sessionStorage.put(TemporaryStorage.PIN_BANKIA, pinScaResponse.getPin2ScaBankia());

        ScaResponse smsScaResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "pushotp_scaResponse.json").toFile(),
                        ScaResponse.class);

        when(apiClient.authorizeSCA(any())).thenReturn(smsScaResponse);

        // when
        Throwable exception =
                catchThrowable(() -> bankiaSignatureStep.execute(authenticationRequest));

        // then
        assertThat(exception).isInstanceOf(ConnectivityException.class);
    }
}
