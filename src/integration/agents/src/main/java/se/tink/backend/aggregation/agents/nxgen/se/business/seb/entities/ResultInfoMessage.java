package se.tink.backend.aggregation.agents.nxgen.se.business.seb.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class ResultInfoMessage {
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
    public boolean hasText() {
        return !Strings.isNullOrEmpty(getErrorText());
    }
}
