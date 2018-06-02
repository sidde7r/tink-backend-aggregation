package se.tink.backend.categorization.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class FastTextLabel implements Cloneable {
    private String label;
    private Double percentage;

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
}
