package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vavr.collection.List;
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

    public List<ValueEntity> getValues() {
        return valuesList.values;
    }

    public ValuesList getValuesList() {
        return valuesList;
    }

    public List<CodIban> getIbans() {
        return ibanList.ibans;
    }

    public List<CardEntity> getCards() {
        if (cardsList == null) {
            return List.empty();
        }
        return cardsList.cards;
    }

    public List<AccountEntity> getAccounts() {
        // This mapping is done since IBAN is not included in the accountList, instead it is
        // included as a seperate list at the same "level" as the accountlist.
        accountsList.accounts.asJava().stream()
                .forEach(
                        t ->
                                t.setIbanEntity(
                                        ibanList.ibans.asJava().stream()
                                                .filter(
                                                        a ->
                                                                a.getCodIban()
                                                                        .getIban()
                                                                        .contains(
                                                                                t.getAccountInfoOldFormat()
                                                                                        .getContractNumber()))
                                                .findFirst()
                                                .get()
                                                .getCodIban()));

        return accountsList.accounts;
    }

    private static class ValuesList {
        @JsonProperty("valores")
        private List<ValueEntity> values;
    }

    private static class IbanList {
        @JsonProperty("datosIban")
        private List<CodIban> ibans;
    }

    private static class CardsList {
        @JsonProperty("tarjetas")
        private List<CardEntity> cards;
    }

    private static class AccountsList {
        @JsonProperty("cuentas")
        private List<AccountEntity> accounts;
    }

    private static class CodIban {
        @JsonProperty("codIban")
        private IbanEntity codIban;

        public IbanEntity getCodIban() {
            return codIban;
        }
    }
}
