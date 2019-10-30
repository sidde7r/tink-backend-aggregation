package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class WidgetEventsRequest extends RequestEntity {
    private static final Logger log = LoggerFactory.getLogger(WidgetEventsRequest.class);
    private String applicationId;
    private List<WidgetEventInformation> widgetEventInformations;

    public String getApplicationId() {
        return applicationId;
    }

    public List<WidgetEventInformation> getWidgetEventInformations() {
        return widgetEventInformations;
    }

    public static WidgetEventsRequest create(WidgetEventInformation... widgets) {
        WidgetEventsRequest widgetRequest = new WidgetEventsRequest();
        widgetRequest.applicationId = BelfiusConstants.Request.APPLICATION_ID;
        widgetRequest.widgetEventInformations = Lists.newArrayList(widgets);
        // TODO - Temporary log below for icecream hack test in staging environment.
        Gson gson = new Gson();
        log.info("widgetRequest: {}", gson.toJson(widgetRequest));
        return widgetRequest;
    }
}
