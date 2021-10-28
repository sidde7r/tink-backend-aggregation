package se.tink.backend.aggregation.nxgen.http.log.executor.raw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.OutputStream;
import java.io.PrintStream;
import org.junit.Test;

public class RawHttpTrafficLoggerTest {

    @Test
    public void should_be_enabled_by_default() {
        // given
        RawHttpTrafficLogger rawLogger =
                new RawHttpTrafficLogger(mock(OutputStream.class), mock(PrintStream.class));

        // when
        boolean enabled = rawLogger.isEnabled();

        // then
        assertThat(enabled).isTrue();
    }
}
