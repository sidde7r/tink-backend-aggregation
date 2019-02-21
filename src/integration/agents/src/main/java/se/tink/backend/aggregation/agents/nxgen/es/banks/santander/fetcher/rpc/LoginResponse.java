package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.creditcards.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.InfoEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.UserData;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities.PortfolioEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities.FundEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement(name = "methodResult")
public class LoginResponse {
    private InfoEntity info;
    @JsonProperty("datosUsuario")
    private UserData userData;
    @JsonProperty("cuentas")
    private List<AccountEntity> accountList;
    @JsonProperty("tarjetas")
    private List<CardEntity> cards;
    @JsonProperty("fondos")
    private List<FundEntity> funds;
    @JsonProperty("valores")
    private List<PortfolioEntity> portfolios;
    @JsonProperty("prestamos")
    private List<LoanEntity> loans;

    public InfoEntity getInfo() {
        return info;
    }

    public UserData getUserData() {
        return userData;
    }

    public List<AccountEntity> getAccountList() {
        return Optional.ofNullable(accountList).orElse(Collections.emptyList());
    }

    public List<CardEntity> getCards() {
        return Optional.ofNullable(cards).orElse(Collections.emptyList());
    }

    public List<FundEntity> getFunds() {
        return Optional.ofNullable(funds).orElse(Collections.emptyList());
    }

    public List<LoanEntity> getLoans() {
        return loans;
    }

    public List<PortfolioEntity> getPortfolios() {
        return portfolios;
    }
}
