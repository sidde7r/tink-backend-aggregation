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
                    .put("PCB0464", "Transfer validation: The user lacks permission for this action")
                    .put("PCB049I", "Transfer validation: Transfer can be made next business day as earliest, change due date")
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
        return ErrorText != null ? ErrorText.trim() : null;
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
