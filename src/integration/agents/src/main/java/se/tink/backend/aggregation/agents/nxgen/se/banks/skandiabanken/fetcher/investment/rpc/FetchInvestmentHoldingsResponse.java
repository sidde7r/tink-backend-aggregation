package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.rpc;

import java.util.ArrayList;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.HoldingsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchInvestmentHoldingsResponse extends ArrayList<HoldingsEntity> {}
