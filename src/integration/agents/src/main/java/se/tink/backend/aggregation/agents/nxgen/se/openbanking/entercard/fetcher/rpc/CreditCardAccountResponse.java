package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.fetcher.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.fetcher.entities.MetadataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardAccountResponse {

    public MetadataEntity metadataEntity;

    private List<AccountEntity> accounts;

    public List<AccountEntity> getAccounts() {
        return accounts;
    }
}
