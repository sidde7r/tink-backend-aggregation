package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountListResponse {
    private List<Object> laan;
    private List<Object> disponerer;
    private List<Object> minFamilie;
    private List<AccountEntity> eier;

    public void setLaan(List<Object> laan) {
        this.laan = laan;
    }

    public List<Object> getLaan() {
        return laan;
    }

    public void setDisponerer(List<Object> disponerer) {
        this.disponerer = disponerer;
    }

    public List<Object> getDisponerer() {
        return disponerer;
    }

    public void setMinFamilie(List<Object> minFamilie) {
        this.minFamilie = minFamilie;
    }

    public List<Object> getMinFamilie() {
        return minFamilie;
    }

    public void setAccountEntities(List<AccountEntity> eier) {
        this.eier = eier;
    }

    public List<AccountEntity> getAccountEntities() {
        return eier;
    }
}
