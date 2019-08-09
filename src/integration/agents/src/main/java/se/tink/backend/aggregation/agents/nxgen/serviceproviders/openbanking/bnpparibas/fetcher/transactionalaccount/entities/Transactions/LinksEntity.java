package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities.Transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.entities.BalancesEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities.HrefEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    @JsonProperty("next")
    private HrefEntity nextEntity;

    @JsonProperty("balances")
    private BalancesEntity balancesEntity;

    @JsonProperty("last")
    private HrefEntity lastEntity;

    @JsonProperty("self")
    private HrefEntity selfEntity;

    @JsonProperty("parent-list")
    private HrefEntity parentListEntity;

    public HrefEntity getNextEntity() {
        return nextEntity;
    }

    public BalancesEntity getBalancesEntity() {
        return balancesEntity;
    }

    public HrefEntity getLastEntity() {
        return lastEntity;
    }

    public HrefEntity getSelfEntity() {
        return selfEntity;
    }

    public HrefEntity getParentListEntity() {
        return parentListEntity;
    }
}
