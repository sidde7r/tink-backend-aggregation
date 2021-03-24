package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.creditcard.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.MontepioConstants;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.MontepioConstants.PropertyKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class CreditCardEntity {

    @JsonProperty("Balance")
    private double balance;

    @JsonProperty("Currency")
    private String currency;

    @JsonProperty("DisplayNumber")
    private String displayNumber;

    @JsonProperty("Id")
    private String handle;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Number")
    private String number;

    public CreditCardAccount toTinkAccount(Double creditLimit) {
        String correctedCurrency =
                Strings.isNullOrEmpty(currency) ? MontepioConstants.DEFAULT_CURRENCY : currency;
        ExactCurrencyAmount ecBalance = ExactCurrencyAmount.of(balance, correctedCurrency);
        ExactCurrencyAmount availableCredit =
                ExactCurrencyAmount.of(creditLimit, correctedCurrency);
        CreditCardModule cardDetails =
                CreditCardModule.builder()
                        .withCardNumber(displayNumber)
                        .withBalance(ecBalance)
                        .withAvailableCredit(availableCredit)
                        .withCardAlias(name)
                        .build();
        IdModule idModule =
                IdModule.builder()
                        .withUniqueIdentifier(number)
                        .withAccountNumber(displayNumber)
                        .withAccountName(name)
                        .addIdentifier(
                                AccountIdentifier.create(
                                        AccountIdentifierType.COUNTRY_SPECIFIC, number))
                        .build();
        return CreditCardAccount.nxBuilder()
                .withCardDetails(cardDetails)
                .withoutFlags()
                .withId(idModule)
                .putInTemporaryStorage(PropertyKeys.HANDLE, handle)
                .build();
    }

    public String getHandle() {
        return handle;
    }
}
