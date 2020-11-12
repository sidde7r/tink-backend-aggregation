package se.tink.backend.aggregation.nxgen.http.log.executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LoggingAdapterTest {

    private LoggingAdapter<String, String> loggingAdapter;

    @Mock private LoggingExecutor loggingExecutor;

    private final ArgumentCaptor<RequestLogEntry> requestCaptor =
            ArgumentCaptor.forClass(RequestLogEntry.class);
    private final ArgumentCaptor<ResponseLogEntry> responseCaptor =
            ArgumentCaptor.forClass(ResponseLogEntry.class);

    @Before
    public void init() {
        loggingAdapter = new TestLoggingAdapter(loggingExecutor);
    }

    @Test
    public void shouldConvertRequest() {
        loggingAdapter.logRequest("A request");

        verify(loggingExecutor).log(requestCaptor.capture());
        RequestLogEntry value = requestCaptor.getValue();
        assertThat(value).isNotNull();
        assertThat(value.getMethod()).isEqualTo("GET");
        assertThat(value.getUrl()).isEqualTo("http://localhost/x");
        assertThat(value.getBody()).isEqualTo("A request");
    }

    @Test
    public void shouldConvertResponse() {
        loggingAdapter.logResponse("A response");

        verify(loggingExecutor).log(responseCaptor.capture());
        ResponseLogEntry value = responseCaptor.getValue();
        assertThat(value).isNotNull();
        assertThat(value.getStatus()).isEqualTo(200);
        assertThat(value.getBody()).isEqualTo("A response");
    }

    private class TestLoggingAdapter extends LoggingAdapter<String, String> {

        public TestLoggingAdapter(LoggingExecutor loggingExecutor) {
            super(loggingExecutor);
        }

        @Override
        protected String mapMethod(String request) {
            return "GET";
        }

        @Override
        protected String mapUrl(String request) {
            return "http://localhost/x";
        }

        @Override
        protected Map<String, String> mapRequestHeaders(String request) {
            return new HashMap<>();
        }

        @Override
        protected int mapStatus(String response) {
            return 200;
        }

        @Override
        protected Map<String, String> mapResponseHeaders(String response) {
            return new HashMap<>();
        }

        @Override
        protected InputStream convertRequest(String request) throws IOException {
            return new ByteArrayInputStream(request.getBytes());
        }

        @Override
        protected InputStream convertResponse(String response) throws IOException {
            return new ByteArrayInputStream(response.getBytes());
        }
    }
}
