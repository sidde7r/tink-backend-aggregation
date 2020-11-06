package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BankdataPoolAccountEntity {
    private IdEntity id;
    private String name;
    private String ownerName;
    private int ownerRefNo;
    private boolean owner;
    private String ownership;
    private String group;
    private String type;
    private String currency;
    private String customerCurrency;
    private double unsettledValue;
    private double balance;
    private double depositYTD;
    private double settledReturn;
    private double unsettledReturn;
    private double unsettledFee;
    private long entryDate;
    private long exitDate;
    private long unsettledValueDate;
    private InvestmentProfileEntity investmentProfile;
    private List<DistributionEntity> distribution;

    public boolean isMarketCurrency() {
        return "dkk".equalsIgnoreCase(currency);
    }

    public boolean isKnownType() {
        return BankdataConstants.POOLACCOUNT_TYPES.containsKey(type.toLowerCase());
    }

    private double getBalance() {
        return unsettledValue;
    }

    private double getProfit() {
        return settledReturn + unsettledReturn;
    }

    private String getFormattedAccountNumber() {
        return String.format("%s-%s", id.getRegNo(), id.getAccountNo());
    }

    private String getAccountNumber() {
        return String.format("%s%s", id.getRegNo(), id.getAccountNo());
    }

    private Portfolio.Type toTinkPortfolioType() {
        return BankdataConstants.POOLACCOUNT_TYPES.getOrDefault(
                type.toLowerCase(), Portfolio.Type.OTHER);
    }

    public InvestmentAccount toTinkInvestment() {
        Portfolio portfolio = new Portfolio();
        portfolio.setUniqueIdentifier(getAccountNumber());
        portfolio.setRawType(type);
        portfolio.setType(toTinkPortfolioType());
        portfolio.setTotalProfit(getProfit());
        portfolio.setTotalValue(getBalance());

        return InvestmentAccount.builder(getAccountNumber())
                .setCashBalance(ExactCurrencyAmount.zero(currency))
                .setAccountNumber(getFormattedAccountNumber())
                .setBankIdentifier(getAccountNumber())
                .setName(name)
                .setPortfolios(Collections.singletonList(portfolio))
                // As the capabilities are meant to signify "instant/direct" result we
                // - don't have an option to make and receive a transfer
                .canExecuteExternalTransfer(AccountCapabilities.Answer.NO)
                .canReceiveExternalTransfer(AccountCapabilities.Answer.NO)
                // lack of information to determine the following
                .canWithdrawCash(AccountCapabilities.Answer.UNKNOWN)
                .canPlaceFunds(AccountCapabilities.Answer.UNKNOWN)
                .build();
    }
}
