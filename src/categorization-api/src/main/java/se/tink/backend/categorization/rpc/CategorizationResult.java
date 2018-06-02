package se.tink.backend.categorization.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Collections;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class CategorizationResult {
    private List<CategorizationLabel> labels = Collections.emptyList();

    public List<CategorizationLabel> getLabels() {
        return labels;
    }
}
