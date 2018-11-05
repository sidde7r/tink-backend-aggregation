package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.IbanEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.ValueEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserDataResponse {
    @JsonProperty("datosSalidaValores")
    private ValuesList valuesList;

    @JsonProperty("datosSalidaCodIban")
    private IbanList ibanList;

    @JsonProperty("datosSalidaTarjetas")
    private CardsList cardsList;

    @JsonProperty("datosSalidaCuentas")
    private AccountsList accountsList;

    public static UserDataResponse empty() {
        return new UserDataResponse();
    }

    public List<ValueEntity> getValues() {
        return valuesList.values;
    }

    public ValuesList getValuesList() {
        return valuesList;
    }

    public List<IbanEntity> getIbans() {
        return ibanList.ibans;
    }

    public List<CardEntity> getCards() {
        return cardsList.cards;
    }

    public List<AccountEntity> getAccounts() {
        return accountsList.accounts;
    }

    private static class ValuesList {
        @JsonProperty("valores")
        private List<ValueEntity> values;
    }

    private static class IbanList {
        @JsonProperty("datosIban")
        private List<IbanEntity> ibans;
    }

    private static class CardsList {
        @JsonProperty("tarjetas")
        private List<CardEntity> cards;
    }

    private static class AccountsList {
        @JsonProperty("cuentas")
        private List<AccountEntity> accounts;
    }
}
