package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.entities.ConstructOtherButtonsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.entities.FundsListEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.entities.ShowFundsBalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

@JsonObject
public class FundsListResponse {
    @JsonProperty("tieneFondos")
    private boolean hasFunds;
    @JsonProperty("listaFondos")
    private List<FundsListEntity> fundAccounts;
    @JsonProperty("verFondosSaldo")
    private ShowFundsBalanceEntity showFundsBalance;
    @JsonProperty("masDatos")
    private boolean moreData;
    private boolean timeOut;
    @JsonProperty("construyeOtrosBotones")
    private ConstructOtherButtonsEntity constructOtherButtons;

    @JsonIgnore
    public List<InvestmentAccount> getTinkInvestments(LaCaixaApiClient apiClient, HolderName holderName, EngagementResponse engagements) {
        return Optional.ofNullable(fundAccounts).orElse(Collections.emptyList()).stream()
                .map(fundAccount -> fundAccount.toTinkInvestment(apiClient, holderName, engagements))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public boolean isMoreData() {
        return moreData;
    }
}
