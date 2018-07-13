package se.tink.backend.aggregation.agents.banks.sbab.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnswerEntity {

    @JsonProperty("svarsalternativId")
    private int id;

    @JsonProperty("svarstext")
    private String value;

    @JsonProperty("fritextTillaten")
    private boolean customValueAllowed;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isCustomValueAllowed() {
        return customValueAllowed;
    }

    public void setCustomValueAllowed(boolean customValueAllowed) {
        this.customValueAllowed = customValueAllowed;
    }

    @Override
    public boolean equals(Object compareObject) {
        if (this == compareObject) {
            return true;
        }

        if (compareObject == null || getClass() != compareObject.getClass()) {
            return false;
        }

        AnswerEntity other = (AnswerEntity) compareObject;

        return Objects.equal(getId(), other.getId()) &&
                Objects.equal(isCustomValueAllowed(), other.isCustomValueAllowed()) &&
                Objects.equal(getValue(), other.getValue());
    }
}
