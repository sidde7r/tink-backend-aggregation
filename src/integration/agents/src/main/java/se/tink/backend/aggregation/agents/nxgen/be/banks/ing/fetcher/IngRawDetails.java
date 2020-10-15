package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher;

import java.util.Collections;
import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class IngRawDetails {

    private List<String> details;
    private List<String> extraDetails;

    public IngRawDetails(List<String> details, List<String> extraDetails) {
        // be kind no nulls
        this.details = details != null ? details : Collections.emptyList();
        this.extraDetails = extraDetails != null ? extraDetails : Collections.emptyList();
    }
}
