package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.creditcard.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment.entities.InvestmentAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.entities.LoanAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ContractsResponse {
    @JsonProperty("listaCuentas")
    private List<AccountEntity> accountsList;

    @JsonProperty("listaTarjetas")
    private List<CardEntity> listCards;

    @JsonProperty("listaCuentasValores")
    private List<InvestmentAccountEntity> investmentsList;

    @JsonProperty("listaPrestamos")
    private List<LoanAccountEntity> loanList;

    public List<AccountEntity> getAccounts() {
        return Optional.ofNullable(accountsList).orElse(Collections.emptyList());
    }

    public List<CardEntity> getCards() {
        return Optional.ofNullable(listCards).orElse(Collections.emptyList());
    }

    public List<InvestmentAccountEntity> getInvestments() {
        return Optional.ofNullable(investmentsList).orElse(Collections.emptyList());
    }

    public List<LoanAccountEntity> getLoans() {
        return Optional.ofNullable(loanList).orElse(Collections.emptyList());
    }
}
