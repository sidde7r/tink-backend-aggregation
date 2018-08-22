package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.creditcard.entities.CardEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ContractsResponse {
    @JsonProperty("listaCuentas")
    private List<AccountEntity> accountsList;
    @JsonProperty("listaTarjetas")
    private List<CardEntity> listCards;

    public List<AccountEntity> getAccounts() {
        return accountsList;
    }

    public List<CardEntity> getCards() {
        return listCards;
    }
}
