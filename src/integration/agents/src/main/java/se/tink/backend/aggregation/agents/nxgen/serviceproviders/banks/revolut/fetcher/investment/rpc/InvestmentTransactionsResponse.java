package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity.ItemsItemEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InvestmentTransactionsResponse {

    @JsonProperty("currentOffset")
    private int currentOffset;

    @JsonProperty("totalCount")
    private int totalCount;

    @JsonProperty("items")
    private List<ItemsItemEntity> items;
}
