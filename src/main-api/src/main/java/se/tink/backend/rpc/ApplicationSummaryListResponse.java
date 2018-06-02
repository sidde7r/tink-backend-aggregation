package se.tink.backend.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import se.tink.backend.core.ApplicationSummary;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class ApplicationSummaryListResponse {
    @Tag(1)
    @ApiModelProperty(name = "summaries", value = "List of `ApplicationSummary` entries.")
    private List<ApplicationSummary> summaries;

    public List<ApplicationSummary> getSummaries() {
        return summaries;
    }

    public void setSummaries(List<ApplicationSummary> summaries) {
        this.summaries = summaries;
    }
}
