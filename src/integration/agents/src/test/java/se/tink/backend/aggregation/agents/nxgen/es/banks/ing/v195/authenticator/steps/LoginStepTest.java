package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.steps.LoginStep.getPasswordStringAsIntegerList;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.steps.LoginStep.getUsernameType;

import com.google.common.collect.ImmutableMap;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants.ScaMethod;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants.UsernameTypes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.CreateSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.PutRestSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class LoginStepTest {

    private static final List VALID_PINPAD = Arrays.asList(9, 8, 7, 6, 5, 4, 3, 2, 1, 0);
    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/ing/v195/resources/";

    @Test
    public void testGetPinPositionsForPassword() throws LoginException {

        List<Integer> pinPositions =
                LoginStep.getPinPositionsForPassword(
                        getPasswordStringAsIntegerList("123456"),
                        VALID_PINPAD,
                        Arrays.asList(1, 3, 5));

        assertEquals(3, pinPositions.size());

        assertArrayEquals(
                Arrays.asList(8, 6, 4).toArray(new Integer[0]),
                pinPositions.toArray(new Integer[0]));
    }

    @Test(expected = LoginException.class)
    public void testExceptionThrownIfAlphaNumericPassword() throws LoginException {
        getPasswordStringAsIntegerList("12345A");
    }

    @Test(expected = LoginException.class)
    public void testExceptionThrownIfPinpadNumbersMissing() throws LoginException {

        LoginStep.getPinPositionsForPassword(
                getPasswordStringAsIntegerList("123456"),
                Arrays.asList(9, 8, 7, 2, 1, 0),
                Arrays.asList(1, 3, 5));
    }

    @Test(expected = LoginException.class)
    public void testExceptionThrownIfInvalidPinPositionRequested() throws LoginException {
        LoginStep.getPinPositionsForPassword(
                getPasswordStringAsIntegerList("123456"), VALID_PINPAD, Arrays.asList(1, 3, 7));
    }

    @Test(expected = LoginException.class)
    public void testExceptionThrownIfZeroPinPositionRequested() throws LoginException {
        LoginStep.getPinPositionsForPassword(
                getPasswordStringAsIntegerList("123456"), VALID_PINPAD, Arrays.asList(0, 3, 6));
    }

    @Test
    public void testUsernameTypes() throws LoginException {
        assertEquals(UsernameTypes.NIF, getUsernameType("12345678Z"));
        assertEquals(UsernameTypes.NIF, getUsernameType("12345677J"));
        assertEquals(UsernameTypes.NIE, getUsernameType("Z2345678M"));
        assertEquals(UsernameTypes.NIE, getUsernameType("X2345677E"));
        assertEquals(UsernameTypes.PASSPORT, getUsernameType("XAB123456"));
        assertEquals(UsernameTypes.PASSPORT, getUsernameType("AB123456"));
        assertEquals(UsernameTypes.PASSPORT, getUsernameType("12345678A"));
    }

    @Test
    public void shouldRouteToMobileValidationStepIfMobileValidationNeeded() {
        // given
        IngApiClient apiClient = mock(IngApiClient.class);
        Credentials credentials = mock(Credentials.class);
        PersistentStorage persistentStorage = new PersistentStorage();
        persistentStorage.put(Storage.DEVICE_ID, "12345678");
        when(credentials.getField(Field.Key.USERNAME)).thenReturn("00814788J");
        when(credentials.getField(Key.PASSWORD)).thenReturn("12345678");
        //        LocalDate dateOfBirth = LocalDate.parse("02011985", IngUtils.BIRTHDAY_INPUT);
        when(credentials.getField(IngConstants.DATE_OF_BIRTH)).thenReturn("02011985");
        LoginStep loginStep =
                new LoginStep(
                        apiClient,
                        new SessionStorage(),
                        new PersistentStorage(),
                        mock(RandomValueGenerator.class),
                        ImmutableMap.of(
                                ScaMethod.SMS, OtpStep.STEP_ID, ScaMethod.PUSH, PushStep.STEP_ID),
                        true);
        HttpResponse response = mock(HttpResponse.class);
        when(response.getBody(ErrorResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(
                                                RESOURCES_PATH
                                                        + "create-session-response-mobile-validation-ex.json")
                                        .toFile(),
                                ErrorResponse.class));
        HttpResponseException exception = mock(HttpResponseException.class);
        when(exception.getResponse()).thenReturn(response);

        when(apiClient.postLoginRestSession(any()))
                .thenThrow(exception)
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(RESOURCES_PATH + "create-session-response-succesful.json")
                                        .toFile(),
                                CreateSessionResponse.class));

        when(apiClient.putLoginRestSession(any(List.class), any()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(RESOURCES_PATH + "put-session-response.json").toFile(),
                                PutRestSessionResponse.class));

        when(apiClient.putLoginRestSession(any(List.class), any()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(RESOURCES_PATH + "put-session-response.json").toFile(),
                                PutRestSessionResponse.class));

        // when
        AuthenticationStepResponse authenticationStepResponse =
                loginStep.execute(new AuthenticationRequest(credentials));

        // then
        assertThat(authenticationStepResponse.getNextStepId())
                .isEqualTo(Optional.of(MobileValidationStep.class.getName()));
    }
}
