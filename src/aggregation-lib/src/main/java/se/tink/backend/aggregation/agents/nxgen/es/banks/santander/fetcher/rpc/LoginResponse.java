package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.creditcards.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.InfoEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.UserData;
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

    public InfoEntity getInfo() {
        return info;
    }

    public UserData getUserData() {
        return userData;
    }

    public List<AccountEntity> getAccountList() {
        return accountList == null ? Collections.emptyList() : accountList;
    }

    public List<CardEntity> getCards() {
        return cards == null ? Collections.emptyList() : cards;
    }
}
