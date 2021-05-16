package se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.fetchers.oauth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class DnbOAuthHeaderFormatterTest {

    private DnbOAuthHeaderFormatter formatter;

    @Before
    public void setUp() {
        formatter = new DnbOAuthHeaderFormatter();
    }

    @Test
    public void putOnePair() {
        // given

        // when
        formatter.putPair("pair 1 key", "pair 1 value");

        // then
        String result = formatter.toString();
        assertThat(result).isEqualTo("OAuth " + "pair 1 key=\"pair 1 value\"");
    }

    @Test
    public void putMultiplePairs() {
        // given

        // when
        formatter.putPair("pair 1 key", "pair 1 value");
        formatter.putPair("pair 2 key", "pair 2 value");
        formatter.putPair("pair 3 key", "pair 3 value");

        // then
        String result = formatter.toString();
        assertThat(result)
                .isEqualTo(
                        "OAuth "
                                + "pair 1 key=\"pair 1 value\", "
                                + "pair 2 key=\"pair 2 value\", "
                                + "pair 3 key=\"pair 3 value\"");
    }
}
