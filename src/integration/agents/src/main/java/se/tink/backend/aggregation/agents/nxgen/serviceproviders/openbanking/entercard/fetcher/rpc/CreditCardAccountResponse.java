package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.fetcher.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.fetcher.entities.MetadataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardAccountResponse {

    private MetadataEntity metadataEntity;

    private List<AccountEntity> account;

    public List<AccountEntity> getAccount() {
        return account;
    }
}
