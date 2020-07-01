package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entities.account;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountLinksEntity {

    private String viewBalances;
    private String viewTransactions;
}
