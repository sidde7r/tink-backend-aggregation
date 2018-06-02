package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.WidgetEventInformation;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.WidgetEventsRequest;

public class MenuAccess extends BelfiusRequest {
    public static BelfiusRequest.Builder create(String sessionId) {
        return BelfiusRequest.builder()
                .setRetry(false)
                .setRequests(
                        WidgetEventsRequest.create(
                                WidgetEventInformation.newButtonClickedWidgetEvent(
                                        BelfiusConstants.Widget.MENU_ACCESS)))
                .setSessionId(sessionId);
    }
}
