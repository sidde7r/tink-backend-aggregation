package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class JyskeKnownErrorsFilterTest {

    private JyskeKnownErrorsFilter jyskeKnownErrorsFilter;

    private final HttpRequest httpRequest = Mockito.mock(HttpRequest.class);
    private HttpResponse httpResponse;

    @Before
    public void setUp() {
        Filter mockFilter = mock(Filter.class);
        jyskeKnownErrorsFilter = new JyskeKnownErrorsFilter();
        jyskeKnownErrorsFilter.setNext(mockFilter);
        httpResponse = Mockito.mock(HttpResponse.class);
        when(httpResponse.getStatus()).thenReturn(400);
        when(httpResponse.hasBody()).thenReturn(true);
        when(mockFilter.handle(httpRequest)).thenReturn(httpResponse);
    }

    @Test
    @Parameters(method = "messageAndExceptions")
    public void shouldThrowProperExceptionForProperErrorResponse(
            String errorResponseJson, AgentError expectedError) {
        // given
        ErrorResponse errorResponse =
                SerializationUtils.deserializeFromString(errorResponseJson, ErrorResponse.class);
        when(httpResponse.getBody(ErrorResponse.class)).thenReturn(errorResponse);
        // when
        final Throwable thrown = catchThrowable(() -> jyskeKnownErrorsFilter.handle(httpRequest));

        // then
        assertThat(thrown)
                .isEqualToComparingFieldByField(expectedError.exception(errorResponse.toString()));
    }

    @Test
    public void shouldReturnResponseForUnknownErrorMessage() {
        // given
        ErrorResponse errorResponse =
                SerializationUtils.deserializeFromString(
                        "{\"errorCode\": 300, \"status\": \"INTERNAL_SERVER_ERROR\", \"errorMessage\": \"UNKNOWN_MESSAGE_123\"}",
                        ErrorResponse.class);
        when(httpResponse.getBody(ErrorResponse.class)).thenReturn(errorResponse);

        // when
        HttpResponse httpResponse = jyskeKnownErrorsFilter.handle(httpRequest);

        // then;
        assertThat(httpResponse).isEqualTo(httpResponse);
    }

    @SuppressWarnings("unused")
    private Object[] messageAndExceptions() {
        return new Object[] {
            new Object[] {
                "{\"errorCode\": 109, \"status\": \"BAD_REQUEST\", \"errorMessage\": \"Du skal tilmelde dig mobilbanken i Jyske Netbank, inden du kan logge på.\"}",
                LoginError.NO_ACCESS_TO_MOBILE_BANKING
            },
            new Object[] {
                "{\"errorCode\": 300, \"status\": \"BAD_REQUEST\", \"errorMessage\": \"Ingen aktive nøgleapps\"}",
                LoginError.NO_ACCESS_TO_MOBILE_BANKING
            },
            new Object[] {
                "{\"errorCode\": 7, \"status\": \"BAD_REQUEST\", \"errorMessage\": \"Dit bruger-id er spærret hos NemID. Kontakt support.\"}",
                AuthorizationError.ACCOUNT_BLOCKED
            },
            new Object[] {
                "{\"errorCode\": 109, \"status\": \"BAD_REQUEST\", \"errorMessage\": \"Forkert bruger-id eller mobilkode\"}",
                LoginError.INCORRECT_CREDENTIALS
            },
            new Object[] {
                "{\"errorCode\": 300, \"status\": \"INTERNAL_SERVER_ERROR\", \"errorMessage\": \"Kunne ikke hente kontobevægelser - Prøv igen senere.\"}",
                BankServiceError.BANK_SIDE_FAILURE
            },
            new Object[] {
                "{\"errorCode\": 110, \"status\": \"BAD_REQUEST\", \"errorMessage\": \"Din adgang til mobilbanken er spærret. Åbn den igen i Jyske Netbank.\"}",
                AuthorizationError.ACCOUNT_BLOCKED
            }
        };
    }
}
