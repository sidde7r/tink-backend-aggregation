package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionListSearchCriteriaEntity {
    @JsonProperty("can_use_start_date")
    private boolean canUseStartDate;

    @JsonProperty("can_use_end_date")
    private boolean canUseEndDate;

    @JsonProperty("can_use_lowest_amount")
    private boolean canUseLowestAmount;

    @JsonProperty("can_use_highest_amount")
    private boolean canUseHighestAmount;

    @JsonProperty("can_use_free_text")
    private boolean canUseFreeText;

    @JsonProperty("sort_by_list")
    private List<String> sortByList;
}
