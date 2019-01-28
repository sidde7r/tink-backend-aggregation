package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.util.Date;
import org.apache.commons.lang.StringUtils;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.json.BaseMobileResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor.rpc.ValidateInternalTransferResponse;
import se.tink.libraries.enums.MessageType;
import se.tink.backend.core.signableoperation.SignableOperationStatuses;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.i18n.Catalog;

public class IngTransferHelper {

    private final Catalog catalog;

    public IngTransferHelper(Catalog catalog) {
        this.catalog = catalog;
    }

    /**
     * For transfers between own accounts and 3rd party accounts where recipient is manually entered
     * by the user, the amount in the transfer request is formatted with a + sign.
     * 1.0 as input returns "+000000000000001002"
     */
    public static String formatSignedTransferAmount(Double amount) {
        return formatTransferAmount(amount, "+");
    }

    /**
     * For transfers to saved beneficiaries the amount in the transfer request is formatted with a space.
     * 1.0 as input returns " 000000000000001002"
     */
    public static String formatUnsignedTransferAmount(Double amount) {
        return formatTransferAmount(amount, " ");
    }

    private static String formatTransferAmount(Double amount, String formattingCharacter) {
        int scale = 2;
        return String.format("%s%017d%d", formattingCharacter, Math.round(amount * Math.pow(10,scale)), scale);
    }

    public static void addDestinationMessageByMessageType(MultivaluedMapImpl map, MessageType messageType,
            String destinationMessage) {
        if (messageType == MessageType.STRUCTURED) {
            String structuredComm = destinationMessage.replaceAll("[^0-9]", "");
            map.add(IngConstants.Transfers.STRUCTURED_COMMUNICATION, structuredComm);
            addFreeTextMessage(map, "");
            return;
        }

        if (messageType == MessageType.FREE_TEXT) {
            addFreeTextMessage(map, destinationMessage);
            map.add(IngConstants.Transfers.STRUCTURED_COMMUNICATION, "");
            return;
        }

        throw new IllegalStateException(String.format("Message type %s is not supported", messageType.name()));
    }

    /**
     * Max free text message length is 140 chars, divided on the four different commLine fields, 35 chars
     * per field. All four fields are always present even if the message is shorter than 140 chars.
     */
    private static void addFreeTextMessage(MultivaluedMapImpl map, String destinationMessage) {
        for (int i = 0; i < IngConstants.Transfers.MAX_MESSAGE_ROWS; i++) {

            String subString = StringUtils.substring(
                    destinationMessage, IngConstants.Transfers.MAX_MESSAGE_LENGTH * i,
                    IngConstants.Transfers.MAX_MESSAGE_LENGTH * (i + 1));

            map.add(IngConstants.Transfers.COMM_LINE + (i + 1), subString);
        }
    }

    public static String formatTransferDueDate(Date dueDate) {
        if (dueDate == null) {
            return "";
        }

        return ThreadSafeDateFormat.FORMATTER_INTEGER_DATE.format(dueDate);
    }

    /**
     * For transfers to saved beneficiaries and third party accounts the validation response is in JSON.
     */
    void verifyTransferValidationJsonResponse(BaseMobileResponseEntity mobileResponseEntity) {
        if (returnCodeIsOk(mobileResponseEntity.getReturnCode())) {
            return;
        }

        mobileResponseEntity.getErrorCode()
                .ifPresent(this::throwKnownErrorException);

        failTransfer(IngConstants.EndUserMessage.TRANSFER_VALIDATION_FAILED.getKey().get());
    }

    /**
     * For transfers to internal accounts the validation response is in XML.
     */
    void verifyTransferValidationXmlResponse(ValidateInternalTransferResponse transferResponse) {
        if (returnCodeIsOk(transferResponse.getReturnCode())) {
            return;
        }

        transferResponse.getErrors().getErrorCode()
                .ifPresent(this::throwKnownErrorException);

        failTransfer(IngConstants.EndUserMessage.TRANSFER_VALIDATION_FAILED.getKey().get());
    }

    private static boolean returnCodeIsOk(String returnCode) {
        return IngConstants.ReturnCodes.OK.equalsIgnoreCase(returnCode);
    }

    private void throwKnownErrorException(String errorCode) {
        if (IngConstants.ErrorCodes.TRANSFER_AMOUNT_EXCEEDS_LIMIT_CODE.equalsIgnoreCase(errorCode)) {
            cancelTransfer(IngConstants.EndUserMessage.TRANSFER_AMOUNT_EXCEEDS_LIMIT.getKey().get());
        }

        if (IngConstants.ErrorCodes.TRANSFER_AMOUNT_EXCEEDS_BALANCE.equalsIgnoreCase(errorCode)) {
            cancelTransfer(TransferExecutionException.EndUserMessage.EXCESS_AMOUNT.getKey().get());
        }
    }

    void ensureTransferExecutionWasSuccess(String returnCode) {
        if (!IngConstants.ReturnCodes.OK.equalsIgnoreCase(returnCode)) {
            failTransfer(IngConstants.EndUserMessage.TRANSFER_EXECUTION_FAILED.getKey().get());
        }
    }

    void failTransfer(String message) {
        throw buildTranslatedTransferException(message, SignableOperationStatuses.FAILED);
    }

    void cancelTransfer(String message) throws TransferExecutionException {
        throw buildTranslatedTransferException(message, SignableOperationStatuses.CANCELLED);
    }

    TransferExecutionException buildTranslatedTransferException(String message, SignableOperationStatuses status) {
        String translatedMessage = catalog.getString(message);
        return TransferExecutionException.builder(status)
                .setEndUserMessage(translatedMessage)
                .setMessage(translatedMessage)
                .build();
    }
}
