package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.MediaType;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskePersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.KeycardChallengeEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.security.Token;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(JUnitParamsRunner.class)
public class JyskeKeyCardAuthenticatorTest {

    @Test
    @Parameters(method = "errorResponses")
    public void should_throw_supplemental_info_error_when_nemid_keycard_code_is_invalid(
            ErrorResponse errorResponse, SupplementalInfoError supplementalInfoError) {
        // given
        TinkHttpClient tinkHttpClient = mockTinkHttpClient(errorResponse);
        PersistentStorage persistentStorage = createPersistentStorage();
        JyskeKeyCardAuthenticator jyskeKeyCardAuthenticatorTest =
                new JyskeKeyCardAuthenticator(
                        new JyskeApiClient(tinkHttpClient),
                        new JyskePersistentStorage(persistentStorage),
                        new Credentials());

        // when
        Throwable throwable =
                catchThrowable(() -> jyskeKeyCardAuthenticatorTest.authenticate("code"));

        // then
        assertThat(throwable).isInstanceOf(SupplementalInfoException.class);
        SupplementalInfoException supplementalInfoException = (SupplementalInfoException) throwable;
        assertThat(supplementalInfoException.getError()).isEqualTo(supplementalInfoError);
    }

    private Object[] errorResponses() {
        return new Object[] {
            new Object[] {
                new ErrorResponse(1, null, null, "BAD_REQUEST"), SupplementalInfoError.NO_VALID_CODE
            },
            new Object[] {new ErrorResponse(999, null, null, null), SupplementalInfoError.UNKNOWN}
        };
    }

    private PersistentStorage createPersistentStorage() {
        PersistentStorage persistentStorage = new PersistentStorage();
        persistentStorage.put(Storage.USER_ID, "userId");
        persistentStorage.put(Storage.PIN_CODE, "pinCode");
        persistentStorage.put(Storage.TOKEN, Token.generate());
        persistentStorage.put(Storage.KEYCARD_CHALLENGE_ENTITY, new KeycardChallengeEntity());
        return persistentStorage;
    }

    private TinkHttpClient mockTinkHttpClient(ErrorResponse errorResponse) {
        TinkHttpClient tinkHttpClient = mock(TinkHttpClient.class);
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        when(tinkHttpClient.request(any(URL.class))).thenReturn(requestBuilder);
        when(requestBuilder.accept(any(MediaType.class))).thenReturn(requestBuilder);
        when(requestBuilder.type(any(MediaType.class))).thenReturn(requestBuilder);
        when(requestBuilder.header(any(), any())).thenReturn(requestBuilder);

        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getBody(ErrorResponse.class)).thenReturn(errorResponse);
        HttpResponseException httpResponseException =
                new HttpResponseException(mock(HttpRequest.class), httpResponse);
        when(requestBuilder.post(any(), any())).thenThrow(httpResponseException);
        return tinkHttpClient;
    }
}
