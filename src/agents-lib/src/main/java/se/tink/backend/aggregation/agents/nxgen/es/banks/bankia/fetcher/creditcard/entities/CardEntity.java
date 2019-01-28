package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.ContractEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.libraries.amount.Amount;

@JsonObject
public class CardEntity {
    @JsonProperty("contrato")
    private ContractEntity contract;
    @JsonProperty("saldoInformado")
    private boolean informedBalance;
    @JsonProperty("saldoDisponible")
    private AmountEntity availableBalance;
    @JsonProperty("limiteCredito")
    private AmountEntity creditLimit;

    public CreditCardAccount toTinkAccount() {

        String cardNumber = contract.getIdentifierProductContract().substring(1);
        String cardAlias = contract.getAlias();

        return CreditCardAccount.builderFromFullNumber(cardNumber, cardAlias)
                .setBalance(getBalance())
                .setAvailableCredit(availableBalance.toTinkAmount())
                .setBankIdentifier(cardNumber)
                .build();
    }

    private Amount getBalance() {
        if (creditLimit.toTinkAmount().isPositive()) {
            return availableBalance.toTinkAmount().subtract(creditLimit.toTinkAmount());
        } return availableBalance.toTinkAmount();
    }

    public ContractEntity getContract() {
        return contract;
    }

    public boolean isInformedBalance() {
        return informedBalance;
    }

    public AmountEntity getAvailableBalance() {
        return availableBalance;
    }

    public AmountEntity getCreditLimit() {
        return creditLimit;
    }
}
