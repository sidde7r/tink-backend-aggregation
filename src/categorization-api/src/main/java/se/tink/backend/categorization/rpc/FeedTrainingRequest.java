package se.tink.backend.categorization.rpc;

import java.util.List;

public class FeedTrainingRequest {
    private List<String> labels;
    private String description;

    public FeedTrainingRequest(List<String> labels, String description) {
        this.labels = labels;
        this.description = description;
    }

    public List<String> getLabels() {
        return labels;
    }

    public String getDescription() {
        return description;
    }
}
