package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.creditcards.rpc;

import java.util.ArrayList;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.creditcards.entities.CreditCardDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardDataResponse extends ArrayList<CreditCardDataEntity> {}
