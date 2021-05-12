package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.investment.entities;

import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
@Slf4j
public class MiscEntity {
    private AccountNumberEntity accountNumber;
    private AmountEntity value;
    private String id;
    private String name;
    private String ownerName;
    private boolean owner;
    private Double amount;
    private AmountEntity marketValue;
    private AmountEntity returnSinceBought;
    private AmountEntity returnThisYear;
    private Double returnSinceBoughtPercentage;
    private Double returnThisYearPercentage;
    private AmountEntity quote;
    private long quoteTimestamp;
    private Double deltaQuotePoint;
    private Double deltaQuotePercent;
    private String type;
    private boolean hasTrades;
    private boolean powerOfAttorney;
    private boolean tradesAllowed;

    public boolean hasInvestmentAccount() {
        if (Objects.nonNull(marketValue)) {
            log.info("Credential has investment account");
            return true;
        }
        return false;
    }

    public InvestmentAccount toTinkInvestmentAccount() {
        return InvestmentAccount.nxBuilder()
                .withoutPortfolios()
                .withCashBalance(getExactCurrencyAmount())
                .withId(getIdModule())
                .build();
    }

    private ExactCurrencyAmount getExactCurrencyAmount() {
        return ExactCurrencyAmount.of(value.getAmount(), value.getCurrencyCode());
    }

    private IdModule getIdModule() {
        return IdModule.builder()
                .withUniqueIdentifier(accountNumber.getRegNo() + accountNumber.getAccountNo())
                .withAccountNumber(accountNumber.getAccountNo())
                .withAccountName(name)
                .addIdentifier(
                        AccountIdentifier.create(
                                AccountIdentifierType.OTHER, accountNumber.getAccountNo(), name))
                .build();
    }
}
