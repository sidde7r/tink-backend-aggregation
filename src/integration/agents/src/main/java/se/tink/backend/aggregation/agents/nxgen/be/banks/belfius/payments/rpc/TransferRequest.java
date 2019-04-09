package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.rpc;

import java.util.Date;
import java.util.Optional;
import org.apache.commons.lang.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.WidgetEventInformation;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.WidgetEventsRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils.BelfiusSecurityUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils.BelfiusStringUtils;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.SepaEurIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.transfer.rpc.Transfer;

public class TransferRequest extends BelfiusRequest {

    /**
     * Formats a Date to a String acceptable for Belfius Creates a string on the format dd\/mm\/yyyy
     * from a date
     *
     * @param date
     * @return a formatted date string
     */
    public static String getDateString(Date date) {
        String day = (date.getDate() > 9) ? "" + date.getDate() : "0" + date.getDate();
        String month =
                ((date.getMonth() + 1) > 9)
                        ? "" + (date.getMonth() + 1)
                        : "0" + (date.getMonth() + 1);
        char special = (char) 92;
        return day + special + "/" + month + special + "/" + (date.getYear() + 1900);
    }

    private static String getFormattedAmount(Amount amount) {
        return BelfiusStringUtils.getFormattedAmount(amount);
    }

    private static String getName(Transfer transfer) {
        Optional<String> name = transfer.getDestination().getName();
        return name.isPresent()
                ? name.get()
                : ((SepaEurIdentifier) transfer.getDestination()).getIban();
    }

    public static BelfiusRequest.Builder create(
            boolean toOwnAccount,
            Transfer transfer,
            String clientHash,
            boolean isStructuredMessage) {
        return BelfiusRequest.builder()
                .setRetry(false)
                .setRequests(
                        WidgetEventsRequest.create(
                                WidgetEventInformation.newButtonClickedWidgetEvent(
                                        BelfiusConstants.Widget.TRANSFER_INIT)),
                        WidgetEventsRequest.create(
                                WidgetEventInformation.newButtonClickedWidgetEvent(
                                        BelfiusConstants.Widget.TRANSFER_PAY),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.ACCOUNT_INPUT,
                                        "I"
                                                + ((SepaEurIdentifier) (transfer.getSource()))
                                                        .getIban()),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.ACCOUNT_OWNER,
                                        "I"
                                                + ((SepaEurIdentifier) transfer.getDestination())
                                                        .getIban()),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.TO_OWN_ACCOUNT,
                                        toOwnAccount ? "Y" : "N"),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.RECIPIENT_NAME, getName(transfer)),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.RECIPIENT_COUNTRY,
                                        StringUtils.substring(
                                                ((IbanIdentifier) transfer.getDestination())
                                                        .getIban(),
                                                0,
                                                2)),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.RECIPIENT_MESSAGE,
                                        transfer.getDestinationMessage()),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.USE_STRUCTURED_MESSAGE,
                                        isStructuredMessage ? "Y" : "N"),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.TRANSFER_AMOUNT,
                                        getFormattedAmount(transfer.getAmount())
                                                + " "
                                                + transfer.getAmount().getCurrency()),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.TRANSFER_DATE,
                                        getDateString(transfer.getDueDate())),
                                WidgetEventInformation.newInputValueChangedWidgetEvent(
                                        BelfiusConstants.Widget.SHA1_CLIENT, clientHash)))
                .setTransactionId(BelfiusSecurityUtils.generateTransferId());
    }
}
