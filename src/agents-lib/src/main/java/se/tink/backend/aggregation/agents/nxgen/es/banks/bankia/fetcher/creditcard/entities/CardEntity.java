package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.ContractEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.core.Amount;

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
        Amount balance = getBalance();

        String cardNumberUnmasked = contract.getIdentifierProductContract().substring(1);
        Preconditions.checkState(
                cardNumberUnmasked.matches(BankiaConstants.Regex.CARD_NUMBER_UNMASKED),
                "Card number provided by bank is not of expected format (16 digits unformatted)"
        );

        String firstFour = cardNumberUnmasked.substring(0, 4);
        String lastFour = cardNumberUnmasked.substring(cardNumberUnmasked.length() - 4);
        String accountNumber = String.format("%s **** **** %s", firstFour, lastFour);
        String uniqueIdentifier = String.format("%s%s", firstFour, lastFour);
        String name = String.format("%s *%s", contract.getAlias(), lastFour);

        return CreditCardAccount.builder(uniqueIdentifier)
                .setBalance(balance)
                .setAvailableCredit(availableBalance.toTinkAmount())
                .setName(name)
                .setAccountNumber(accountNumber)
                .setBankIdentifier(cardNumberUnmasked)
                .build();
    }

    private Amount getBalance() {
        if (creditLimit.toTinkAmount().isPositive()) {
            return availableBalance.toTinkAmount().subtract(creditLimit.toTinkAmount());
        } return availableBalance.toTinkAmount();
    }
}
