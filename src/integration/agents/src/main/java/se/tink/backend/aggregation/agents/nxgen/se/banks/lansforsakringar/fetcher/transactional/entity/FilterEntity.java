package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FilterEntity {
    private IncludeFilterEntity includeFilter;

    private FilterEntity(IncludeFilterEntity includeFilter) {
        this.includeFilter = includeFilter;
    }

    @JsonIgnore
    public static FilterEntity of(String transactionStatus) {
      IncludeFilterEntity filterEntity = new IncludeFilterEntity(transactionStatus);
      return new FilterEntity(filterEntity);
    }
}
