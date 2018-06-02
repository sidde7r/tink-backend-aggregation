package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.WidgetEventInformation;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.WidgetEventsRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils.BelfiusSecurityUtils;

public class SignBeneficiaryRequest extends BelfiusRequest {
    public static BelfiusRequest.Builder create(String challenge) {
        return BelfiusRequest.builder()
                .setRetry(false)
                .setRequests(
                        WidgetEventsRequest.create(
                                WidgetEventInformation.newButtonClickedWidgetEvent(
                                        BelfiusConstants.Widget.UPGRADE_BENEFICIARIES),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.BENEFICIARY_SIGNATURE,
                                        challenge)))
                .setTransactionId(BelfiusSecurityUtils.generateBeneficiaryId());
    }
}
