package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc;

import static se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEConstants.ResponseMessage.EXCESS_AMOUNT;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEConstants.ResponseMessage.EXECUTION_DAY_INVALID;
import static se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.DanskeBankSEConstants.ResponseMessage.MESSAGE_MISSING;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.entity.ForcableErrorEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

@Slf4j
@Getter
@JsonObject
public class RegisterPaymentResponse {
    @JsonProperty("AutoStartToken")
    private String autoStartToken;

    @JsonProperty("ForcableError")
    private List<ForcableErrorEntity> forcableError;

    @JsonProperty("InitSignPackage")
    private String initSignPackage;

    @JsonProperty("IsConfirmation")
    private boolean isConfirmation;

    @JsonProperty("OrderRef")
    private String orderRef;

    @JsonProperty("ResponseCode")
    private int responseCode;

    @JsonProperty("SignDataEnc")
    private String signDataEnc;

    @JsonProperty("SignatureId")
    private String signatureId;

    @JsonProperty("SignatureText")
    private String signatureText;

    @JsonProperty("ResponseMessage")
    private String responseMessage;

    @JsonProperty("StatusCode")
    private int statusCode;

    @JsonProperty("UserID")
    private String userId;

    @JsonProperty("ValidationResponse")
    private String validationResponse;

    @JsonIgnore
    public RegisterPaymentResponse validate() {
        boolean errorMessageExist = !Strings.isNullOrEmpty(responseMessage);
        if (statusCode != HttpStatus.SC_OK || errorMessageExist) {
            throw throwTransferException(errorMessageExist);
        }
        return this;
    }

    @JsonIgnore
    private TransferExecutionException throwTransferException(boolean errorMessageExist) {
        if (errorMessageExist) {
            switch (responseMessage) {
                case MESSAGE_MISSING:
                    return createCancelledException(EndUserMessage.INVALID_DESTINATION_MESSAGE);
                case EXCESS_AMOUNT:
                    return createCancelledException(EndUserMessage.EXCESS_AMOUNT);
                case EXECUTION_DAY_INVALID:
                    return createCancelledException(
                            EndUserMessage.INVALID_DUEDATE_TOO_SOON_OR_NOT_BUSINESSDAY);
                default:
                    throw createDefaultFailedException();
            }
        }
        throw createDefaultFailedException();
    }

    private TransferExecutionException createDefaultFailedException() {
        log.info("packageID is null, message from bank: {}", responseMessage);
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(EndUserMessage.TRANSFER_REJECTED.getKey().get())
                .setEndUserMessage(EndUserMessage.TRANSFER_REJECTED)
                .build();
    }

    private TransferExecutionException createCancelledException(EndUserMessage endUserMessage) {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(endUserMessage.getKey().get())
                .setEndUserMessage(endUserMessage)
                .build();
    }
}
