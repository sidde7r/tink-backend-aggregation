package se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject()
public class CollectionEntity {

    @JsonProperty("current_page")
    private Long currentPage;

    @JsonProperty("per_page")
    private Long perPage;

    @JsonProperty("total_entries")
    private Long totalEntries;

    @JsonProperty("total_pages")
    private Long totalPages;

    public Long getCurrentPage() {
        return currentPage;
    }

    public Long getPerPage() {
        return perPage;
    }

    public Long getTotalEntries() {
        return totalEntries;
    }

    public Long getTotalPages() {
        return totalPages;
    }
}
