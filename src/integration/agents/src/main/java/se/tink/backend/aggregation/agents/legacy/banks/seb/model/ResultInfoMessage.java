package se.tink.backend.aggregation.agents.banks.seb.model;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

@JsonObject
public class ResultInfoMessage {
    private static final ImmutableMap<String, String> CANCELLED_DESCRIPTIONS_BY_CODE =
            ImmutableMap.<String, String>builder()
                    .put("PCB046H", "Transfer validation: DueDate is before next business day")
                    .put("PCB03G0", "Transfer validation: The user does not have enough money")
                    .put("PCB03K1", "Transfer validation: The destination account is not correct")
                    .put(
                            "PCB03E3",
                            "Transfer validation: Exact same payment already exist. Please sign it in your bank's app before creating a new payment")
                    .put(
                            "PCB0464",
                            "Transfer validation: The user lacks permission for this action")
                    .put(
                            "PCB049I",
                            "Transfer validation: Transfer can be made next business day as earliest, change due date")
                    .put(
                            "PCB03L1",
                            "Transfer validation: Supplied destination account doesn't exist in any Swedish bank")
                    .put(
                            "PCB0792",
                            "Transfer validation: Transfer limit for business account has been reached")
                    .put(
                            "PCB0354",
                            "Transfer validation: Destination account doesn't exist at bank") // Seems to be a specific error for SEB destination accounts
                    .put(
                            "2000",
                            "Transfer validation: To protect your money from fraudulent attempts, we have blocked this transfer. We can help you transfer the money by calling us at 0771-365 365.")
                    .build();

    public String TableName;
    public Integer ErrorRowId;
    public String ErrorColumnName;
    public String Level;
    public String ErrorCode;
    public String ErrorText;

    public String getErrorCode() {
        return ErrorCode != null ? ErrorCode.trim() : null;
    }

    public String getErrorText() {
        String text = ErrorText != null ? ErrorText.trim() : null;
        return "2000".equalsIgnoreCase(getErrorCode()) ? "Banken har blockerat överföringen" : text;
    }

    private Optional<String> getOptionalDescription() {
        return Optional.ofNullable(CANCELLED_DESCRIPTIONS_BY_CODE.get(getErrorCode()));
    }

    public String getDescription() {
        Optional<String> description = getOptionalDescription();

        if (description.isPresent()) {
            return description.get();
        } else {
            return String.format(
                    "Unknown error: %s",
                    MoreObjects.toStringHelper(this)
                            .add("ErrorText", getErrorText())
                            .add("ErrorCode", getErrorCode())
                            .toString());
        }
    }

    public SignableOperationStatuses getSignableOperationStatus() {
        if (CANCELLED_DESCRIPTIONS_BY_CODE.containsKey(getErrorCode())) {
            return SignableOperationStatuses.CANCELLED;
        } else {
            return SignableOperationStatuses.FAILED;
        }
    }
}
