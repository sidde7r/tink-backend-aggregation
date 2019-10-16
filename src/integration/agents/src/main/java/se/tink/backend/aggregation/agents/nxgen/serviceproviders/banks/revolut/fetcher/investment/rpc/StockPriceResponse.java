package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.rpc;

import java.util.ArrayList;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity.StockPriceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class StockPriceResponse extends ArrayList<StockPriceEntity> {}
