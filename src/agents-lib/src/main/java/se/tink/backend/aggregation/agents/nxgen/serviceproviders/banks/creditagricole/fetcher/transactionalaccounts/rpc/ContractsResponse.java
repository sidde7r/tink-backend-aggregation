package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.fetcher.transactionalaccounts.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.fetcher.transactionalaccounts.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.fetcher.transactionalaccounts.entities.IncomeEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.fetcher.transactionalaccounts.entities.LifeInsuranceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.fetcher.transactionalaccounts.entities.RetirementEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.rpc.DefaultResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ContractsResponse extends DefaultResponse {
    private String lastOperationRetrievalDate;
    private String lastSynchronizationDateTime;
    private boolean echecSynthesePredica;
    @JsonProperty("account")
    private List<AccountEntity> accounts;
    private List<LifeInsuranceEntity> lifeInsurance;
    private List<IncomeEntity> income;
    private List<RetirementEntity> retirement;

    public String getLastOperationRetrievalDate() {
        return lastOperationRetrievalDate;
    }

    public String getLastSynchronizationDateTime() {
        return lastSynchronizationDateTime;
    }

    public boolean isEchecSynthesePredica() {
        return echecSynthesePredica;
    }

    public List<AccountEntity> getAccounts() {
        return accounts;
    }

    public List<LifeInsuranceEntity> getLifeInsurance() {
        return lifeInsurance;
    }

    public List<IncomeEntity> getIncome() {
        return income;
    }

    public List<RetirementEntity> getRetirement() {
        return retirement;
    }
}
