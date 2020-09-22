package se.tink.backend.aggregation.agents.tools.opsgenie;

import java.util.ArrayList;
import java.util.List;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.tools.opsgenie.rpc.CreateAlertRequest;

@Ignore
public class OpsGenieClientTest {

    // This test will create an actual OpsGenie alert in the oncall Slack channels
    @Test
    public void createAlertTest() {
        final CreateAlertRequest createAlertRequest = new CreateAlertRequest();
        createAlertRequest.setMessage("Testing creating alerts through opsgenie rest api");
        createAlertRequest.setDescription("Test description");
        createAlertRequest.setEntity("Test entity");
        createAlertRequest.setPriority("P4");

        final List<String> tags = new ArrayList<>();
        tags.add("*#aggregation-thundercats*");
        tags.add("aggregation");
        tags.add("test");

        createAlertRequest.setTags(tags);

        OpsGenieClient.createAlert(createAlertRequest);
    }
}
