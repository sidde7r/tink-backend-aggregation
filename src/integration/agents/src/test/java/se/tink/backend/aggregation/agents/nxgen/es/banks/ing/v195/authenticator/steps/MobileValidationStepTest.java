package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.BasicResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.TicketResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class MobileValidationStepTest {
    private MobileValidationStep mobileValidationStep;
    private IngApiClient apiClient;
    private SessionStorage sessionStorage;
    private static final String PERSON_ID_VAL = "dummyPerson";
    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/ing/v195/resources/";

    @Before
    public void init() {
        apiClient = mock(IngApiClient.class);
        sessionStorage = new SessionStorage();
        mobileValidationStep = new MobileValidationStep(apiClient, sessionStorage);
    }

    @Test
    public void shouldThrowLoginErrorIfPersonIdMissingInStorage() {
        // given

        // when
        Throwable throwable =
                catchThrowable(() -> mobileValidationStep.execute(new AuthenticationRequest(null)));

        // then
        assertThat(throwable)
                .isInstanceOf(LoginException.class)
                .hasMessageContaining("Missing neccessary personId in storage");
    }

    @Test
    public void shouldThrowLoginErrorIfRegisterInsecurePhoneFailed() {
        // given
        sessionStorage.put(Storage.PERSON_ID, PERSON_ID_VAL);
        when(apiClient.registerInsecureMobileError(any()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(RESOURCES_PATH + "basic_response_fail.json").toFile(),
                                BasicResponse.class));

        // when
        Throwable throwable =
                catchThrowable(() -> mobileValidationStep.execute(new AuthenticationRequest(null)));

        // then
        assertThat(throwable)
                .isInstanceOf(LoginException.class)
                .hasMessageContaining("Error when registering rooted mobile");
    }

    @Test
    public void shouldThrowLoginErrorIfDismissScaFailed() {
        // given
        sessionStorage.put(Storage.PERSON_ID, PERSON_ID_VAL);
        when(apiClient.registerInsecureMobileError(any()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(RESOURCES_PATH + "basic_response_succesful.json")
                                        .toFile(),
                                BasicResponse.class));
        when(apiClient.dismissSca(any()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(RESOURCES_PATH + "basic_response_fail.json").toFile(),
                                BasicResponse.class));

        // when
        Throwable throwable =
                catchThrowable(() -> mobileValidationStep.execute(new AuthenticationRequest(null)));

        // then
        assertThat(throwable)
                .isInstanceOf(LoginException.class)
                .hasMessageContaining("Error when registering rooted mobile");
    }

    @Test
    public void shouldReturnAuthenticationSucceededIfHappyPath() {
        // given
        sessionStorage.put(Storage.PERSON_ID, PERSON_ID_VAL);
        when(apiClient.registerInsecureMobileError(any()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(RESOURCES_PATH + "basic_response_succesful.json")
                                        .toFile(),
                                BasicResponse.class));
        when(apiClient.dismissSca(any()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(RESOURCES_PATH + "basic_response_succesful.json")
                                        .toFile(),
                                BasicResponse.class));
        when(apiClient.requestSsoTicket(any()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(RESOURCES_PATH + "ticket_response.json").toFile(),
                                TicketResponse.class));

        // when
        Throwable throwable =
                catchThrowable(() -> mobileValidationStep.execute(new AuthenticationRequest(null)));

        // then
        assertThat(throwable).isNull();
    }
}
