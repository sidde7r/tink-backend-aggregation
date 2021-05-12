package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.investment.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ListingsEntity {
    private SecuritiesEntity securities;
    private CustodyAccountsEntity custodyAccounts;
}
