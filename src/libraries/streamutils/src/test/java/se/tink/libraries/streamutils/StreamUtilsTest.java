package se.tink.libraries.streamutils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.Test;

public class StreamUtilsTest {
    @Test
    public void testStreamWithMultiplEntriesReturnNull() {
        assertThat(Stream.of("1", "2", "3").collect(StreamUtils.toSingleton())).isNull();
    }

    @Test
    public void testStreamWithOneEntryReturnsThatEntry() {
        String s = Stream.of("1", "2", "3").filter("2"::equals).collect(StreamUtils.toSingleton());
        assertThat(s).isNotNull();
        assertThat(s).isEqualTo("2");
    }

    @Test
    public void testStreamWithNoEntries() {
        assertThat(
                        Stream.of("1", "2", "3")
                                .filter(anObject -> false)
                                .collect(StreamUtils.toSingleton()))
                .isNull();
    }
}
