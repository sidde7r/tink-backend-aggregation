package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

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
}

