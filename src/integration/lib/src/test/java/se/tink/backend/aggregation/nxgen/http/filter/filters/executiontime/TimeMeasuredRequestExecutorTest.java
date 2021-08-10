package se.tink.backend.aggregation.nxgen.http.filter.filters.executiontime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.executiontime.TimeMeasuredRequestExecutor.ExecutionDetails;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RunWith(MockitoJUnitRunner.class)
public class TimeMeasuredRequestExecutorTest {

    @Mock private HttpRequest httpRequest;

    private Filter filter;

    private TimeMeasuredRequestExecutor executor;

    @Before
    public void setUp() throws Exception {
        this.filter =
                new Filter() {
                    @Override
                    public HttpResponse handle(HttpRequest httpRequest)
                            throws HttpClientException, HttpResponseException {
                        return mock(HttpResponse.class);
                    }
                };
        filter.setNext(mock(Filter.class));
        this.executor = TimeMeasuredRequestExecutor.withRequest(httpRequest);
    }

    @Test
    public void shouldBeAbleToLogInappropriateExecutionTime() {
        // given
        executor = executor.withThreshold(0);

        // when
        ExecutionDetails result = executor.execute(filter);

        // then
        assertThat(result.shouldBeLogged()).isTrue();
    }

    @Test
    public void shouldNotBeAbleToLogWhenRequestWasExecutedInProperTime() {

        // when
        ExecutionDetails result = executor.execute(filter);

        // then
        assertThat(result.shouldBeLogged()).isFalse();
    }
}
