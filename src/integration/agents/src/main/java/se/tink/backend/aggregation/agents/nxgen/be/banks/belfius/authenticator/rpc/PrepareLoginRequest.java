package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants.Widget;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.WidgetEventInformation;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.WidgetEventsRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PrepareLoginRequest extends BelfiusRequest {

    public static Builder create(String panNumber) {
        return BelfiusRequest.builder()
                .setRequests(
                        WidgetEventsRequest.create(
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        Widget.PAN, panNumber),
                                WidgetEventInformation.newButtonClickedWidgetEvent(
                                        BelfiusConstants.Widget.LOGON_SOFT)),
                        WidgetEventsRequest.create(
                                WidgetEventInformation.newButtonClickedWidgetEvent(
                                        Widget.LOGON_SOFT_GET_CHALLENGE)));
    }
}
