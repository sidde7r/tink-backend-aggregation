package se.tink.backend.categorization.rpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;

public class CategorizationLabel {
    private String label;
    private Double percentage;

    @JsonCreator
    public CategorizationLabel(@JsonProperty("label") String label, @JsonProperty("percentage") Double percentage) {
        this.label = label;
        this.percentage = percentage;
    }


    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CategorizationLabel that = (CategorizationLabel) o;
        return Objects.equals(label, that.label) &&
                Objects.equals(percentage, that.percentage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, percentage);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("label", label)
                .add("percentage", percentage)
                .toString();
    }
}
