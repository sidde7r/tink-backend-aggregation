package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class ResultInfoMessage {
    private static final ImmutableMap<String, String> CANCELLED_DESCRIPTIONS_BY_CODE =
            ImmutableMap.<String, String>builder()
                    .put("PCB046H", "Transfer validation: DueDate is before next business day")
                    .put("PCB03G0", "Transfer validation: The user does not have enough money")
                    .build();

    @JsonProperty("TableName")
    private String tableName;

    @JsonProperty("ErrorRowId")
    private Integer errorRowId;

    @JsonProperty("ErrorColumnName")
    private String errorColumnName;

    @JsonProperty("Level")
    private String level;

    @JsonProperty("ErrorCode")
    private String errorCode;

    @JsonProperty("ErrorText")
    private String errorText;

    @JsonIgnore
    private String getErrorCode() {
        return StringUtils.trimToNull(errorCode);
    }

    @JsonIgnore
    public String getErrorText() {
        return StringUtils.trimToNull(errorText);
    }

    @JsonIgnore
    public String getDescription() {
        String description = CANCELLED_DESCRIPTIONS_BY_CODE.get(getErrorCode());

        if (description != null) {
            return description;
        } else {
            return String.format(
                    "Unknown error: %s",
                    MoreObjects.toStringHelper(this)
                            .add("ErrorText", getErrorText())
                            .add("ErrorCode", getErrorCode())
                            .toString());
        }
    }

    @JsonIgnore
    public SignableOperationStatuses getSignableOperationStatus() {
        if (CANCELLED_DESCRIPTIONS_BY_CODE.containsKey(getErrorCode())) {
            return SignableOperationStatuses.CANCELLED;
        } else {
            return SignableOperationStatuses.FAILED;
        }
    }

    @JsonIgnore
    public boolean hasText() {
        return !Strings.isNullOrEmpty(getErrorText());
    }
}
