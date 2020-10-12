package se.tink.backend.aggregation.agents.tools.opsgenie;

import javax.ws.rs.core.MediaType;
import org.apache.http.HttpHeaders;
import se.tink.backend.aggregation.agents.tools.opsgenie.rpc.CreateAlertRequest;
import se.tink.backend.aggregation.agents.tools.opsgenie.rpc.CreateAlertResponse;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public class OpsGenieClient {
    private static final TinkHttpClient httpClient =
            NextGenTinkHttpClient.builder(
                            LogMaskerImpl.builder().build(),
                            LogMaskerImpl.LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                    .build();
    private static final String CREATE_ALERT_URL = "https://api.opsgenie.com/v2/alerts";

    public static void createAlert(CreateAlertRequest createAlertRequest) {
        httpClient
                .request(CREATE_ALERT_URL)
                .header(HttpHeaders.AUTHORIZATION, "GenieKey {insert key here}")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(createAlertRequest)
                .post(CreateAlertResponse.class);
    }
}
