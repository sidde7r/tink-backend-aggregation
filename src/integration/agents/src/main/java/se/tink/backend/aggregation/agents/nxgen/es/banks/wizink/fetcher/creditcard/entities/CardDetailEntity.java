package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.util.WizinkCardUtil;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Data
@JsonObject
public class CardDetailEntity {

    @JsonProperty("cardNumberDisplay")
    private String cardNumber;

    @JsonProperty("plasticid")
    private String plasticId;

    private String accountNumber;
    private BigDecimal availableBalance;
    private BigDecimal currentBalance;
    private BigDecimal usedBalance;
    private BigDecimal creditLine;
    private String virtualIban;
    private String nameInCard;
    private String cardLogo;

    @JsonIgnore
    public CreditCardAccount toTinkAccount(String xTokenUser) {
        String maskedCardNumber = WizinkCardUtil.getMaskedCardNumber(cardNumber, xTokenUser);
        return CreditCardAccount.nxBuilder()
                .withCardDetails(createCardDetails(maskedCardNumber))
                .withoutFlags()
                .withId(createIdModule(maskedCardNumber))
                .setApiIdentifier(maskedCardNumber)
                .addHolderName(nameInCard)
                .putInTemporaryStorage(StorageKeys.ENCODED_ACCOUNT_NUMBER, accountNumber)
                .build();
    }

    private CreditCardModule createCardDetails(String decodedMaskedCardNumber) {
        final ExactCurrencyAmount balance = ExactCurrencyAmount.of(getBalance(), "EUR");
        final ExactCurrencyAmount availableCredit = ExactCurrencyAmount.of(getCreditLine(), "EUR");
        return CreditCardModule.builder()
                .withCardNumber(decodedMaskedCardNumber)
                .withBalance(balance)
                .withAvailableCredit(availableCredit)
                .withCardAlias(prepareAccountName(decodedMaskedCardNumber))
                .build();
    }

    private BigDecimal getBalance() {
        return Optional.ofNullable(availableBalance).orElse(currentBalance);
    }

    private BigDecimal getCreditLine() {
        return Optional.ofNullable(creditLine).orElse(currentBalance.add(usedBalance));
    }

    private IdModule createIdModule(String decodedMaskedCardNumber) {
        return IdModule.builder()
                .withUniqueIdentifier(decodedMaskedCardNumber)
                .withAccountNumber(decodedMaskedCardNumber)
                .withAccountName(prepareAccountName(decodedMaskedCardNumber))
                .addIdentifier(
                        AccountIdentifier.create(
                                AccountIdentifierType.PAYMENT_CARD_NUMBER, decodedMaskedCardNumber))
                .build();
    }

    private String prepareAccountName(String decodedMaskedCardNumber) {
        return mapCardLogoToAccountType() + " " + decodedMaskedCardNumber;
    }

    private String mapCardLogoToAccountType() {
        if (cardLogo != null) {
            switch (cardLogo) {
                case "401":
                case "402":
                case "420":
                case "502":
                    return "WiZink Oro";
                case "403":
                    if (plasticId.equals("067")) {
                        return "WiZink Classic";
                    }
                    return "WiZink Twin";
                case "404":
                case "504":
                    return "WiZink Classic";
                case "500":
                    return "Mastercard porque TU vuelves de Cepsa";
                case "501":
                    return "Caser Clin Clin";
                case "503":
                    return "WiZink Oro Plus";
                case "505":
                    return "WiZink Classic Plus";
                case "507":
                    return "WiZink Plus";
                default:
                    return "WiZink";
            }
        }
        return "WiZink";
    }
}
