package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc;

import io.vavr.collection.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.entities.CreditCardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities.StockAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.InternationalFundsPortfoliosEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.BbvaResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProductsResponse extends BbvaResponse {
    private List<AccountEntity> accounts;
    private List<CreditCardEntity> cards;
    private List<StockAccountEntity> stockAccounts;
    private List<InternationalFundsPortfoliosEntity> internationalFundsPortfolios;
    private List<Object> workingCapitalLoansLimits;
    private List<Object> revolvingCredits;
    private List<Object> multiMortgages;
    private List<Object> singleInsurance;
    private List<Object> wealthDepositaryPortfolios;
    private List<Object> managedFundsPortfolios;

    public List<AccountEntity> getAccounts() {
        return accounts;
    }

    public List<CreditCardEntity> getCards() {
        return cards;
    }

    public List<InternationalFundsPortfoliosEntity> getInternationalFundsPortfolios() {
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

    public List<StockAccountEntity> getStockAccounts() {
        return stockAccounts;
    }
}
