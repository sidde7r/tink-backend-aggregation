package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.rpc;

import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.WidgetEventInformation;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.WidgetEventsRequest;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.transfer.rpc.Transfer;

public class AddBeneficiaryRequest extends BelfiusRequest {

    public static BelfiusRequest.Builder create(
            String sessionId, Transfer transfer, boolean isStructuredMessage, String name) {
        return BelfiusRequest.builder()
                .setRetry(false)
                .setRequests(
                        WidgetEventsRequest.create(
                                WidgetEventInformation.newButtonClickedWidgetEvent(
                                        BelfiusConstants.Widget.BENEFICIARY_MANAGEMENT)),
                        WidgetEventsRequest.create(
                                WidgetEventInformation.newButtonClickedWidgetEvent(
                                        BelfiusConstants.Widget.PREPARE_UPGRADE),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.LIST_NUMBER, ""),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.SEQUENCE_NUMBER, ""),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.INP_ALIAS, ""),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.INP_BIC, ""),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.INP_EXT_ACC,
                                        "I"
                                                + ((IbanIdentifier) (transfer.getDestination()))
                                                        .getIban()),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.INP_AMOUNT, ""),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.INP_NAME, name),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.INP_STREET, ""),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.INP_HOUSE_NUMBER, ""),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.INP_ZIP, ""),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.INP_CITY, ""),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.INP_COUNTRY_CODE,
                                        StringUtils.substring(
                                                ((IbanIdentifier) (transfer.getDestination()))
                                                        .getIban(),
                                                0,
                                                2)),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.INP_STRUCTURED_COMMUNICATION,
                                        isStructuredMessage ? "Y" : "N"),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.INP_COMMUNICATION, ""),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.INP_CONTACT, "")))
                .setSessionId(sessionId);
    }
}
