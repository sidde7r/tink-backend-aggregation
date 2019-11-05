package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.investments.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.MontepioConstants.PropertyKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class InvestmentAccountEntity {

    @JsonProperty("Balance")
    private double balance;

    @JsonProperty("Currency")
    private String currency;

    @JsonProperty("Id")
    private String handle;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Number")
    private String number;

    public InvestmentAccount toTinkAccount() {
        ExactCurrencyAmount ecBalance = ExactCurrencyAmount.of(balance, currency);
        IdModule idModule =
                IdModule.builder()
                        .withUniqueIdentifier(number)
                        .withAccountNumber(number)
                        .withAccountName(name)
                        .addIdentifier(
                                AccountIdentifier.create(
                                        AccountIdentifier.Type.COUNTRY_SPECIFIC, number))
                        .build();
        return InvestmentAccount.nxBuilder()
                .withoutPortfolios()
                .withCashBalance(ecBalance)
                .withId(idModule)
                .putInTemporaryStorage(PropertyKeys.HANDLE, handle)
                .build();
    }
}
