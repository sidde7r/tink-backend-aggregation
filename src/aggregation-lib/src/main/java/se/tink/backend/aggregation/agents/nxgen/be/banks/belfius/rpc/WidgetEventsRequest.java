package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc;

import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class WidgetEventsRequest extends RequestEntity {

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
        return widgetRequest;
    }
}
