package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.MetroServiceConstants.Services;

public class MetroServiceConstantsTest {

    @Test
    public void shouldBuildProperPathForBindingEndpoint() {
        // when
        URI uri =
                Services.AUTHENTICATION_SERVICE
                        .url()
                        .path("bind")
                        .queryParam("aid", "mobile_metro")
                        .queryParam("locale", "en-US")
                        .build();

        // then
        assertThat(uri)
                .hasToString(
                        "https://tlc.metrobankonline.co.uk:443/api/v2/auth/bind?aid=mobile_metro&locale=en-US");
    }
}
