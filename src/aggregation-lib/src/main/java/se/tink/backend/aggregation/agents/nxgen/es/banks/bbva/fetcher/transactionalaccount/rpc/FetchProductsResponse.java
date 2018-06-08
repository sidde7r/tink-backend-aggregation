package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.BbvaResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchProductsResponse extends BbvaResponse {

    private List<AccountEntity> accounts;
    private List<AccountEntity> cards;
    private List<Object> internationalFundsPortfolios;
    private List<Object> workingCapitalLoansLimits;
    private List<Object> revolvingCredits;
    private List<Object> multiMortgages;
    private List<Object> singleInsurance;
    private List<Object> wealthDepositaryPortfolios;
    private List<Object> managedFundsPortfolios;

    public List<AccountEntity> getAccounts() {
        return accounts;
    }

    // getters to support logging
    public List<AccountEntity> getCards() {
        return cards;
    }

    public List<Object> getInternationalFundsPortfolios() {
        return internationalFundsPortfolios;
    }

    public List<Object> getWorkingCapitalLoansLimits() {
        return workingCapitalLoansLimits;
    }

    public List<Object> getRevolvingCredits() {
        return revolvingCredits;
    }

    public List<Object> getMultiMortgages() {
        return multiMortgages;
    }

    public List<Object> getSingleInsurance() {
        return singleInsurance;
    }

    public List<Object> getWealthDepositaryPortfolios() {
        return wealthDepositaryPortfolios;
    }

    public List<Object> getManagedFundsPortfolios() {
        return managedFundsPortfolios;
    }
}
