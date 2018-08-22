package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.rpc;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.accounts.SmcEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountDetailsResponse {
    private SmcEntity smc;

    public SmcEntity getSmc() {
        return smc;
    }
}
