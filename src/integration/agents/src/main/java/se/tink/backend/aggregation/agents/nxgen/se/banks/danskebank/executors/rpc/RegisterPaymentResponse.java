package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.entity.ForcableErrorEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

@Getter
@Setter
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
        if (statusCode != 200) {
            throw registerTransferFailure();
        }
        return this;
    }

    @JsonIgnore
    private TransferExecutionException registerTransferFailure() {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(EndUserMessage.TRANSFER_REJECTED.getKey().get())
                .setEndUserMessage(EndUserMessage.TRANSFER_REJECTED)
                .build();
    }
}
