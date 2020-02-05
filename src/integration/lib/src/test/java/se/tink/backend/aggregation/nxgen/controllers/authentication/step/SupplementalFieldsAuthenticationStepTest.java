package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepResponse;

public class SupplementalFieldsAuthenticationStepTest {

    private AuthenticationRequest authenticationRequest;
    private Credentials credentials;

    @Before
    public void init() {
        credentials = Mockito.mock(Credentials.class);
        authenticationRequest = new AuthenticationRequest(credentials);
    }

    @Test
    public void shouldRequestForSupplementFields()
            throws AuthenticationException, AuthorizationException {
        // given
        Field field = Mockito.mock(Field.class);
        SupplementalFieldsAuthenticationStep objectUnderTest =
                new SupplementalFieldsAuthenticationStep("stepId", (values) -> {}, field);
        // when
        AuthenticationStepResponse result = objectUnderTest.execute(authenticationRequest);
        // then
        Assert.assertTrue(result.getSupplementInformationRequester().get().getFields().isPresent());
        Iterator<Field> fields =
                result.getSupplementInformationRequester().get().getFields().get().iterator();
        Assert.assertTrue(fields.hasNext());
        Assert.assertEquals(field, fields.next());
        Assert.assertFalse(fields.hasNext());
    }

    @Test
    public void shouldCallbackWithFieldValue()
            throws AuthenticationException, AuthorizationException {
        // given
        Field field = Mockito.mock(Field.class);
        final String fieldKey = "testFieldKey";
        final String fieldValue = "testFieldValue";
        Map<String, String> values = new HashMap<>();
        values.put(fieldKey, fieldValue);
        authenticationRequest = authenticationRequest.withUserInputs(values);
        CallbackProcessorMultiData callbackProcessor =
                Mockito.mock(CallbackProcessorMultiData.class);
        SupplementalFieldsAuthenticationStep objectUnderTest =
                new SupplementalFieldsAuthenticationStep("stepId", callbackProcessor, field);
        // when
        AuthenticationStepResponse result = objectUnderTest.execute(authenticationRequest);
        // then
        Mockito.verify(callbackProcessor).process(values);
    }

    @Test
    public void shouldCallbackWithFieldValueAndCredentials()
            throws AuthenticationException, AuthorizationException {
        // given
        Field field = Mockito.mock(Field.class);
        final String fieldKey = "testFieldKey";
        final String fieldValue = "testFieldValue";
        Map<String, String> values = new HashMap<>();
        values.put(fieldKey, fieldValue);
        authenticationRequest = authenticationRequest.withUserInputs(values);
        CallbackProcessorMultiDataAndCredentials callbackProcessor =
                Mockito.mock(CallbackProcessorMultiDataAndCredentials.class);
        SupplementalFieldsAuthenticationStep objectUnderTest =
                new SupplementalFieldsAuthenticationStep("stepId", callbackProcessor, field);
        // when
        AuthenticationStepResponse result = objectUnderTest.execute(authenticationRequest);
        // then
        Mockito.verify(callbackProcessor).process(values, credentials);
    }
}
