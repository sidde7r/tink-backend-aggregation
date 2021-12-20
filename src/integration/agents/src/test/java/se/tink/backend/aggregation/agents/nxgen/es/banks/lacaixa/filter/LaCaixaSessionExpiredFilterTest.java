package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.filter;

import static org.mockito.Mockito.when;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.rpc.LaCaixaErrorResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(MockitoJUnitRunner.class)
public class LaCaixaSessionExpiredFilterTest {

    @Mock private PersistentStorage storage;

    @Mock private Filter nextFilter;

    @Mock private HttpResponse httpResponse;

    @Mock private HttpRequest httpRequest;

    @Mock private LaCaixaErrorResponse errorResponse;

    @InjectMocks private LaCaixaSessionExpiredFilter objectUnderTest;

    @Before
    public void init() {
        objectUnderTest.setNext(nextFilter);
        when(nextFilter.handle(httpRequest)).thenReturn(httpResponse);
        when(httpResponse.getBody(LaCaixaErrorResponse.class)).thenReturn(errorResponse);
    }

    @Test
    public void shouldThrowSessionExpiredWhenCanNotPerformOperation() {
        // given
        when(httpResponse.getStatus()).thenReturn(409);
        when(errorResponse.getCode()).thenReturn("2968");

        // when
        Throwable throwable = Assertions.catchThrowable(() -> objectUnderTest.handle(httpRequest));

        // then
        Assertions.assertThat(throwable).isInstanceOf(SessionException.class);
        SessionException sessionException = (SessionException) throwable;
        Assertions.assertThat(sessionException.getError()).isEqualTo(SessionError.SESSION_EXPIRED);
    }

    @Test
    public void shouldThrowSessionExpiredWhenAccessToCaixaBankNowNotPossible() {
        // given
        when(httpResponse.getStatus()).thenReturn(409);
        when(errorResponse.getCode()).thenReturn("-1");
        when(errorResponse.getMessage())
                .thenReturn(
                        "Por\\nmotivos de seguridad, no ha sido posible el acceso a CaixaBankNow. Por\\nfavor, vuelve a intentarlo o ponte en contacto con nosotros a trav?s del\\nservicio de atenci?n al cliente.");

        // when
        Throwable throwable = Assertions.catchThrowable(() -> objectUnderTest.handle(httpRequest));

        // then
        Assertions.assertThat(throwable).isInstanceOf(SessionException.class);
        SessionException sessionException = (SessionException) throwable;
        Assertions.assertThat(sessionException.getError()).isEqualTo(SessionError.SESSION_EXPIRED);
    }

    @Test
    public void shouldThrowSessionExpiredWhenCaixabankSignSignatureMechanismHasBeenBlocked() {
        // given
        when(httpResponse.getStatus()).thenReturn(409);
        when(errorResponse.getCode()).thenReturn("3735");

        // when
        Throwable throwable = Assertions.catchThrowable(() -> objectUnderTest.handle(httpRequest));

        // then
        Assertions.assertThat(throwable).isInstanceOf(SessionException.class);
        SessionException sessionException = (SessionException) throwable;
        Assertions.assertThat(sessionException.getError()).isEqualTo(SessionError.SESSION_EXPIRED);
    }

    @Test
    public void shouldReturnHttpResponseForOther409Statuses() {
        // given
        when(httpResponse.getStatus()).thenReturn(409);
        when(errorResponse.getCode()).thenReturn("notExistedCode");

        // when
        HttpResponse response = objectUnderTest.handle(httpRequest);

        // then
        Assertions.assertThat(response).isEqualTo(httpResponse);
    }

    @Test
    public void shouldReturnHttpResponseFor200Status() {
        // given
        when(httpResponse.getStatus()).thenReturn(200);

        // when
        HttpResponse response = objectUnderTest.handle(httpRequest);

        // then
        Assertions.assertThat(response).isEqualTo(httpResponse);
    }
}
