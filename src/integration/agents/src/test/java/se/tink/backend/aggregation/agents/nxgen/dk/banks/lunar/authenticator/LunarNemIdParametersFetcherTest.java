package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator;

import static java.time.Instant.ofEpochMilli;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.ZoneId;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.rpc.NemIdParamsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdParameters;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class LunarNemIdParametersFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/dk/banks/lunar/resources";

    @Test
    public void shouldGetNemIdParamsResponse() {
        // given
        NemIdParamsResponse nemIdParamsResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "nem_id_parameters.json").toFile(),
                        NemIdParamsResponse.class);

        // and
        Clock clock = Clock.fixed(ofEpochMilli(0), ZoneId.systemDefault());
        LunarNemIdParametersFetcher nemIdParametersFetcher = new LunarNemIdParametersFetcher(clock);
        nemIdParametersFetcher.setNemIdParameters(
                SerializationUtils.serializeToString(nemIdParamsResponse));

        // when
        NemIdParameters result = nemIdParametersFetcher.getNemIdParameters();

        // then
        assertThat(result)
                .isEqualToComparingFieldByField(new NemIdParameters(getNemIdTestIframe()));
    }

    @SneakyThrows
    private String getNemIdTestIframe() {
        return FileUtils.readFileToString(
                Paths.get(TEST_DATA_PATH, "nem_id_test_iframe").toFile(), StandardCharsets.UTF_8);
    }
}
