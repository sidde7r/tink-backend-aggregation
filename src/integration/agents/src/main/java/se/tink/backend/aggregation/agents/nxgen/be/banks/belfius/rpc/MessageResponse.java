package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.serializer.MessageResponseDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonDeserialize(using = MessageResponseDeserializer.class)
public class MessageResponse extends ResponseEntity {

    private final String messageContent;
    private final String messageDetail;
    private final String messageType;
    private final String messageTarget;

    public MessageResponse(
            String messageContent, String messageDetail, String messageType, String messageTarget) {
        this.messageContent = messageContent;
        this.messageDetail = messageDetail;
        this.messageType = messageType;
        this.messageTarget = messageTarget;
    }

    public static boolean isError(BelfiusResponse response) {
        MessageResponse messageResponse =
                response.filter(MessageResponse.class).findFirst().orElse(null);
        return messageResponse != null
                && (messageResponse.getMessageType().equalsIgnoreCase("error")
                        || messageResponse.getMessageType().equalsIgnoreCase("fatal"));
    }

    public static void validate(BelfiusResponse response) throws IllegalStateException {
        MessageResponse erroResponse =
                response.filter(MessageResponse.class).findFirst().orElse(null);
        if (erroResponse != null) {
            if (erroResponse.isWrongCredentialsMessage() // Handled in LoginResponse::validate
                    || erroResponse.isMobileBankingDisabled()) { // Handled in
                // PrepareLoginResponse::validate
                return;
            }
            throw new IllegalStateException(erroResponse.getMessageDetail());
        }
    }

    public static String getErrorMessage(BelfiusResponse response) {
        if (response == null) {
            return "";
        }

        MessageResponse messageResponse =
                response.filter(MessageResponse.class).findFirst().orElse(null);
        if (messageResponse == null) {
            return "";
        }
        String errorMessage = messageResponse.getMessageContent();
        String messageDetail = messageResponse.getMessageDetail();
        if (!Strings.isNullOrEmpty(messageDetail)) {
            String[] split = messageDetail.split("\\R");
            errorMessage = errorMessage.concat(" " + split[0]);
        }
        return errorMessage;
    }

    private boolean isWrongCredentialsMessage() {
        return BelfiusConstants.ErrorCodes.ERROR_MESSAGE_TYPE.equalsIgnoreCase(messageType)
                && messageDetail.contains(BelfiusConstants.ErrorCodes.WRONG_CREDENTIALS_CODE);
    }

    private boolean isMobileBankingDisabled() {
        return BelfiusConstants.ErrorCodes.ERROR_MESSAGE_TYPE.equalsIgnoreCase(messageType)
                && messageDetail.contains(
                        BelfiusConstants.ErrorCodes.MISSING_MOBILEBANKING_SUBSCRIPTION);
    }

    private boolean isSessionExpiredMessage() {
        return BelfiusConstants.ErrorCodes.FATAL_MESSAGE_TYPE.equalsIgnoreCase(messageType)
                && (messageContent.contains(BelfiusConstants.ErrorCodes.SESSION_EXPIRED)
                        || messageContent.contains(BelfiusConstants.ErrorCodes.UNKNOWN_SESSION));
    }

    public static boolean containsError(MessageResponse messageResponse) {
        String messageDetail = messageResponse.getMessageDetail();
        String messageType = messageResponse.getMessageType();
        String messageTarget = messageResponse.getMessageTarget();
        return ((messageDetail != null && messageDetail.contains("ERROR"))
                || (messageType != null && messageType.equalsIgnoreCase("error"))
                || (messageTarget != null && messageTarget.equalsIgnoreCase("internal")));
    }

    public static boolean isErrorMessageIdentifier(BelfiusResponse response) {
        MessageResponse messageResponse =
                response.filter(MessageResponse.class).findFirst().orElse(null);
        if (messageResponse == null) {
            return false;
        }
        String messageDetail = messageResponse.getMessageDetail();
        String messageType = messageResponse.getMessageType();
        String messageTarget = messageResponse.getMessageTarget();
        return (messageDetail != null && messageDetail.contains("ERROR"))
                || (messageType != null && messageType.equalsIgnoreCase("warning"))
                || (messageTarget != null && messageTarget.equalsIgnoreCase("internal"));
    }

    public static boolean findError(BelfiusResponse response, String errorCode) {
        if (response == null) {
            return false;
        }

        MessageResponse messageResponse =
                response.filter(MessageResponse.class).findFirst().orElse(null);
        if (messageResponse == null) {
            return false;
        }

        String messageContent = messageResponse.getMessageContent();
        String messageDetail = messageResponse.getMessageDetail();
        return containsError(messageResponse)
                && ((messageContent != null && (messageContent.contains(errorCode))
                        || (messageDetail != null && (messageDetail.contains(errorCode)))));
    }

    public static boolean requireSignOfWeeklyLimit(BelfiusResponse response) {
        return findError(response, BelfiusConstants.ErrorCodes.WEEKLY_LIMIT)
                || findError(response, BelfiusConstants.ErrorCodes.BENEFICIARY_WEEKLY_LIMIT);
    }

    public static boolean requireSignOfDailyLimit(BelfiusResponse response) {
        return findError(response, BelfiusConstants.ErrorCodes.DAILY_LIMIT);
    }

    public static boolean requireSignOfBeneficiaryLimit(BelfiusResponse response) {
        return findError(response, BelfiusConstants.ErrorCodes.BENEFICIARY_LIMIT);
    }

    public static boolean transferSignFailed(BelfiusResponse response) {
        return findError(response, BelfiusConstants.ErrorCodes.ERROR_SIGN_CODE);
    }

    public static boolean transferSignTempError(BelfiusResponse response) {
        return findError(response, BelfiusConstants.ErrorCodes.SIGN_TEMP_ERROR_CODE);
    }

    public static boolean weeklyCardLimitCode(BelfiusResponse response) {
        return findError(response, BelfiusConstants.ErrorCodes.WEEKLY_READER_LIMIT_CODE);
    }

    public static boolean invalidBeneficiarySign(BelfiusResponse response) {
        return findError(response, BelfiusConstants.ErrorCodes.INVBALID_SIGN_BENEFICIARY_CODE);
    }

    public String getMessageContent() {
        return messageContent;
    }

    public String getMessageTarget() {
        return messageTarget;
    }

    public String getMessageDetail() {
        return this.messageDetail;
    }

    public String getMessageType() {
        return this.messageType;
    }
}
