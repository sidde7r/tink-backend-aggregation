package se.tink.backend.aggregation.agents.banks.seb.mortgage.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import se.tink.libraries.social.security.time.SwedishTimeRule;

public class GetRateResponseTest {
    @Rule public SwedishTimeRule timeRule = new SwedishTimeRule();

    @Test
    public void deserialize() throws IOException {
        String serialized = "{\"indicative_rate\":3.88,\"date_valid\":\"2016-10-31T10:48:51\"}";
        ObjectMapper objectMapper = new ObjectMapper();

        GetRateResponse response = objectMapper.readValue(serialized, GetRateResponse.class);

        assertThat(response.getIndicativeRate()).isEqualTo(3.88);
        assertThat(response.getDateValid())
                .hasYear(2016)
                .hasMonth(10)
                .hasDayOfMonth(31)
                .hasHourOfDay(10)
                .hasMinute(48)
                .hasSecond(51)
                .hasMillisecond(0);
    }
}
