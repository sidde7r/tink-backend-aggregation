package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity.OrdersEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity.StocksItemEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class StockInfoResponse {

    @JsonProperty("forbidden-countries")
    private List<String> forbiddenCountries;

    @JsonProperty("partner")
    private String partner;

    @JsonProperty("links")
    private LinksEntity linksEntity;

    @JsonProperty("orders")
    private OrdersEntity ordersEntity;

    @JsonProperty("stocks")
    private List<StocksItemEntity> stocks;

    public List<StocksItemEntity> getStocks() {
        return stocks;
    }
}
