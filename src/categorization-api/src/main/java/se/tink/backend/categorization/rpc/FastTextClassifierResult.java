package se.tink.backend.categorization.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class FastTextClassifierResult implements Cloneable {
    @JsonProperty("labels")
    private List<FastTextLabel> labels;

    @JsonProperty("seenBefore")
    private Boolean seenBefore;

    @JsonProperty("labels")
    public List<FastTextLabel> getLabels() {
        return labels;
    }

    @JsonProperty("seenBefore")
    public Boolean seenBefore() {
        return seenBefore;
    }

    @JsonProperty("labels")
    public void setLabels(List<FastTextLabel> labels) {
        this.labels = labels;
    }

    @JsonProperty("seenBefore")
    public void setSeenBefore(Boolean seenBefore) {
        this.seenBefore = seenBefore;
    }
}
