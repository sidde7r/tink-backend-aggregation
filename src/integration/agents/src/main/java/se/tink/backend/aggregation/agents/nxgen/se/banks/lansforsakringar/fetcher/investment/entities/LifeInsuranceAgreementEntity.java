package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import java.util.List;
import java.util.stream.Collectors;
import org.codehaus.jackson.annotate.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarConstants.Accounts;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class LifeInsuranceAgreementEntity {
    private double agreedPremium;
    private double agreementInvestmentValue;
    private double agreedPremiumExemption;
    private int agreedPremiumAllCovers;
    private double agreedPremiumAllSavings;
    private String agreementNumber;
    private String premiumModelTerm;
    private String startDate;
    private String concept;
    private AgeEntity pensionAge;
    private ProductEntity product;
    private String status;
    private String paymentDate;
    private IdentityEntity insured;
    private IdentityEntity owner;
    private double reportedMonthlySalary;
    private List<LifeInsurancesEntity> lifeInsurances;

    public double getAgreedPremium() {
        return agreedPremium;
    }

    public double getAgreementInvestmentValue() {
        return agreementInvestmentValue;
    }

    public double getAgreedPremiumExemption() {
        return agreedPremiumExemption;
    }

    public int getAgreedPremiumAllCovers() {
        return agreedPremiumAllCovers;
    }

    public double getAgreedPremiumAllSavings() {
        return agreedPremiumAllSavings;
    }

    public String getAgreementNumber() {
        return agreementNumber;
    }

    public String getPremiumModelTerm() {
        return premiumModelTerm;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getConcept() {
        return concept;
    }

    public AgeEntity getPensionAge() {
        return pensionAge;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public String getStatus() {
        return status;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public IdentityEntity getInsured() {
        return insured;
    }

    public IdentityEntity getOwner() {
        return owner;
    }

    public double getReportedMonthlySalary() {
        return reportedMonthlySalary;
    }

    public List<LifeInsurancesEntity> getLifeInsurances() {
        return lifeInsurances;
    }

    @JsonIgnore
    public InvestmentAccount toTinkInvestmentAccount() {
        return InvestmentAccount.builder(agreementNumber.trim())
                .setPortfolios(
                        lifeInsurances.stream()
                                .map(LifeInsurancesEntity::toTinkPortfolio)
                                .collect(Collectors.toList()))
                .setName(product.getName())
                .setAccountNumber(agreementNumber)
                .setExactBalance(
                        ExactCurrencyAmount.of(agreementInvestmentValue, Accounts.CURRENCY))
                .build();
    }
}
