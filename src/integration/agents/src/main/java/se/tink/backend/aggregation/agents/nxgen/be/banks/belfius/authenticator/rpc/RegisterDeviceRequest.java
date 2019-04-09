package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.WidgetEventInformation;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.WidgetEventsRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils.BelfiusSecurityUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterDeviceRequest extends BelfiusRequest {

    public static Builder create(String signature) {
        return BelfiusRequest.builder()
                .setRetry(false)
                .setTransactionId(BelfiusSecurityUtils.generateTransactionId())
                .setRequests(
                        WidgetEventsRequest.create(
                                WidgetEventInformation.newButtonClickedWidgetEvent(
                                        BelfiusConstants.Widget.DEVICE_REGISTRATION_REGISTER),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.DEVICE_REGISTRATION_SIGNATURE,
                                        signature)));
    }
}
