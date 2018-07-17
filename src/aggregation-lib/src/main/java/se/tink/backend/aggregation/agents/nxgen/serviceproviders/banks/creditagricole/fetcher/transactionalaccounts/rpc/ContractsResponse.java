package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.fetcher.transactionalaccounts.rpc;

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
    private List<AccountEntity> account;
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

    public List<AccountEntity> getAccount() {
        return account;
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
