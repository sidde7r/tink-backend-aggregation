package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities;

import io.vavr.collection.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PositionEntity {
    private List<AccountEntity> accounts;
    private List<CardEntity> cards;
    private List<ProductEntity> otherProducts;
    private List<FinancialMarketAccountEntity> financialMarketAccounts;
    private List<SavingInvestmentEntity> savingInvestment;
    private String currency;
    private Integer globalBalance;
    private Integer whatIhave;
    private Integer whatIowe;

    public List<AccountEntity> getAccounts() {
        return accounts;
    }

    public List<CardEntity> getCards() {
        return cards;
    }
}
