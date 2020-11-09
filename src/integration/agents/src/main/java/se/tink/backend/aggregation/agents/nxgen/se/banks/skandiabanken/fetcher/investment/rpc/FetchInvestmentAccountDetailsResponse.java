package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.HoldingsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.SecuritiesAccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.TypeEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class FetchInvestmentAccountDetailsResponse {
    @JsonProperty("AvailableForWithdrawal")
    private BigDecimal availableForWithdrawal;

    @JsonProperty("CreditLimit")
    private BigDecimal creditLimit;

    @JsonProperty("RemainingPawnCapacity")
    private BigDecimal remainingPawnCapacity;

    @JsonProperty("TotalPawnValue")
    private BigDecimal totalPawnValue;

    @JsonProperty("EncryptedSecuritiesAccountNumber")
    private String encryptedSecuritiesAccountNumber = "";

    @JsonProperty("EncryptedSecuritiesAccountDisplayNumber")
    private String encryptedSecuritiesAccountDisplayNumber = "";

    @JsonProperty("EncryptedBankAccountNumber")
    private String encryptedBankAccountNumber = "";

    @JsonProperty("SecuritiesAccountNumber")
    private String securitiesAccountNumber = "";

    @JsonProperty("SecuritiesAccountDisplayNumber")
    private String securitiesAccountDisplayNumber = "";

    @JsonProperty("AccountResponsible")
    private String accountResponsible = "";

    @JsonProperty("AvailableForTrading")
    private BigDecimal availableForTrading;

    @JsonProperty("BankAccountNumber")
    private String bankAccountNumber = "";

    @JsonProperty("CommissionModel")
    private int commissionModel;

    @JsonProperty("Currency")
    private String currency = "";

    @JsonProperty("InternalAccountNumber")
    private int internalAccountNumber;

    @JsonProperty("IsDiscretionaryManaged")
    private boolean isDiscretionaryManaged;

    @JsonProperty("MarketValue")
    private BigDecimal marketValue;

    @JsonProperty("Name")
    private String name = "";

    @JsonProperty("RegistrationDate")
    private String registrationDate = "";

    @JsonProperty("SettledAmount")
    private BigDecimal settledAmount;

    @JsonProperty("TotalValue")
    private BigDecimal totalValue;

    @JsonProperty("Status")
    private int status;

    @JsonProperty("Type")
    private TypeEntity type;

    @JsonProperty("ServiceChargeGroupId")
    private int serviceChargeGroupId;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setType(TypeEntity type) {
        this.type = type;
    }

    @JsonIgnore
    private double getAvailableForWithdrawal() {
        return availableForWithdrawal.doubleValue();
    }

    @JsonIgnore
    private double getTotalValue() {
        return totalValue.doubleValue();
    }

    @JsonIgnore
    public InvestmentAccount toTinkInvestmentAccount(
            SecuritiesAccountsEntity accountsEntity,
            FetchInvestmentHoldingsResponse holdingsResponse) {
        return InvestmentAccount.builder(accountsEntity.getNumber())
                .setCashBalance(ExactCurrencyAmount.zero(currency))
                .setBankIdentifier(accountsEntity.getEncryptedNumber())
                .setAccountNumber(accountsEntity.getNumber())
                .setName(accountsEntity.getDisplayName())
                .setPortfolios(Collections.singletonList(getTinkPortfolio(holdingsResponse)))
                .build();
    }

    @JsonIgnore
    private Portfolio getTinkPortfolio(FetchInvestmentHoldingsResponse holdingsResponse) {
        final Portfolio portfolio = new Portfolio();
        portfolio.setUniqueIdentifier(securitiesAccountNumber);
        portfolio.setRawType(type.getAccountTypeName());
        portfolio.setType(
                SkandiaBankenConstants.PORTFOLIO_TYPE_MAP
                        .translate(type.getAccountTypeName())
                        .orElse(Portfolio.Type.OTHER));
        portfolio.setCashValue(getAvailableForWithdrawal());
        portfolio.setTotalValue(getTotalValue());
        portfolio.setInstruments(getInstruments(holdingsResponse));

        return portfolio;
    }

    @JsonIgnore
    private List<Instrument> getInstruments(FetchInvestmentHoldingsResponse holdingsResponse) {
        return holdingsResponse.stream()
                .map(HoldingsEntity::toTinkInstrument)
                .collect(Collectors.toList());
    }
}
