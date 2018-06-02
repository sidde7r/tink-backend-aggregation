package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ReprocessTransactionsConfiguration {
    @JsonProperty
    private List<String> categoryCodes;
    @JsonProperty
    private String description;
    @JsonProperty
    private boolean includeModifiedByUser;

    public List<String> getCategoryCode() {
        return categoryCodes;
    }

    public String getDescription() {
        return description;
    }

    public boolean includeModifiedByUser() {
        return includeModifiedByUser;
    }

}
