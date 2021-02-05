package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator;

import static java.time.Instant.ofEpochMilli;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;
import java.time.Clock;
import java.time.ZoneId;
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

    private String getNemIdTestIframe() {
        return "<script type=\"text/x-nemid\" id=\"nemid_parameters\">"
                + "{\"CLIENTFLOW\":\"OCESLOGIN2\","
                + "\"CLIENTMODE\":\"LIMITED\","
                + "\"DIGEST_SIGNATURE\":\"digestSignature\","
                + "\"ENABLE_AWAITING_APP_APPROVAL_EVENT\":\"TRUE\","
                + "\"PARAMS_DIGEST\":\"paramsDigest\","
                + "\"SIGN_PROPERTIES\":\"challenge=1234567890123\","
                + "\"SP_CERT\":\"CoolCertificate\","
                + "\"TIMESTAMP\":\"MjAyMS0wMS0xOSAxNDo0NDowMCswMDAw\"}"
                + "</script><iframe id=\"nemid_iframe\" allowTransparency=\"true\" name=\"nemid_iframe\" scrolling=\"no\" "
                + "style=\"z-index: 100; position: relative; width: 275px; height: 350px; border: 0\" "
                + "src=\"https://applet.danid.dk/launcher/lmt/0\"></iframe>";
    }
}
