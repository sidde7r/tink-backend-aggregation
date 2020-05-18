package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.transactions.InfoUdcEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsResponseInfoUdc {
    @JsonProperty("infoUdc")
    private InfoUdcEntity infoUdc;

    public InfoUdcEntity getInfoUdc() {
        return infoUdc;
    }
}
