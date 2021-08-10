package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vavr.collection.List;
import java.util.Collection;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.IbanEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.ValueEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.transactionalaccount.entities.AccountHoldersEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.transactionalaccount.rpc.AccountHolderResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

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
        return accountsList.accounts;
    }

    public Collection<TransactionalAccount> toTinkAccounts(List<AccountHoldersEntity> holders) {
        return accountsList
                .accounts
                .filter(AccountEntity::isTransactionalAccount)
                .map(account -> toTransactionalAccount(account, holders))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .asJava();
    }

    private Optional<TransactionalAccount> toTransactionalAccount(
            AccountEntity account, List<AccountHoldersEntity> holders) {
        return account.toTinkAccount(mapIbanToAccountNumber(account), getHolders(account, holders));
    }

    private List<AccountHolderResponse> getHolders(
            AccountEntity account, List<AccountHoldersEntity> holders) {
        return holders.filter(
                        accountHoldersEntity ->
                                accountHoldersEntity
                                        .getContractNumber()
                                        .equals(
                                                account.getAccountInfoOldFormat()
                                                        .getContractNumber()))
                .map(AccountHoldersEntity::getHolders)
                .getOrElse(List.empty());
    }

    private String mapIbanToAccountNumber(AccountEntity t) {
        return ibanList.ibans.asJava().stream()
                .filter(
                        ibanEntity ->
                                ibanEntity
                                        .getIbanEntity()
                                        .getIban()
                                        .contains(t.getAccountInfoOldFormat().getContractNumber()))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "No iban entity found to get account number"))
                .getIbanEntity()
                .getComposedIban();
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
        private IbanEntity ibanEntity;

        public IbanEntity getIbanEntity() {
            return ibanEntity;
        }
    }
}
