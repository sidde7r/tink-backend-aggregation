package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class InsuranceAccountEntity {
    @JsonIgnore
    private static final Logger log = LoggerFactory.getLogger(InsuranceAccountEntity.class);

    @JsonProperty("BEGDAT_DATUM")
    private int startDate;

    @JsonProperty("DETAIL_URL")
    private String detailUrl;

    @JsonProperty("FORSTYP")
    private String insuranceType;

    @JsonProperty("FORSTYP_PNAMN")
    private String insureanceTypeName;

    @JsonProperty("FORS_AGARE")
    private String insuranceOwner; // Name of company that owns the insurance

    @JsonProperty("FORS_KNRTPLAN")
    private String insuranceKnrtPlan;

    @JsonProperty("FORS_MKODDAT")
    private String insuranceMkodData;

    @JsonProperty("FORS_MKODVARDE")
    private String insuranceMkodValue;

    @JsonProperty("FORS_NR")
    private String insuranceNumber;

    @JsonProperty("FORS_SKKAT")
    private String insuranceSkkat;

    @JsonProperty("FORS_STATUSK")
    private String insuranceStatusK;

    @JsonProperty("FORS_TJPKOD")
    private String inSuranceTjpKod;

    @JsonProperty("FULLMAKT_FL")
    private String mandateFl;

    @JsonProperty("PREMIE_BELOPP")
    private double premium;

    @JsonProperty("ROW_ID")
    private int rowId;

    @JsonProperty("SEB_KUND_NR")
    private String sebCustomerId;

    @JsonProperty("VAERDE_DATUM")
    private int valueDate;

    @JsonProperty("VARDE_BELOPP")
    private double marketValue;

    @JsonIgnore
    public Account toAccount() {
        Account account = new Account();

        account.setBankId(insuranceNumber);
        account.setAccountNumber(insuranceNumber);
        account.setBalance(marketValue);
        account.setName(getInsuranceName());
        account.setType(AccountTypes.INVESTMENT);

        return account;
    }

    private String getInsuranceName() {
        if (Strings.isNullOrEmpty(insuranceOwner)) {
            return null;
        }

        return StringUtils.firstLetterUppercaseFormatting(insuranceOwner.trim());
    }

    @JsonIgnore
    public Portfolio toPortfolio() {
        Portfolio portfolio = new Portfolio();

        portfolio.setUniqueIdentifier(insuranceNumber);
        portfolio.setTotalValue(marketValue);
        // cashValue not present, default to 0
        portfolio.setCashValue(0d);
        portfolio.setRawType(insuranceType.trim());
        portfolio.setType(getPortfolioType());

        return portfolio;
    }

    @JsonIgnore
    private Portfolio.Type getPortfolioType() {
        if (Strings.isNullOrEmpty(insuranceType)) {
            return Portfolio.Type.OTHER;
        }

        switch (insuranceType.trim().toLowerCase()) {
            case "tjänstepension":
                return Portfolio.Type.PENSION;
            default:
                log.warn(String.format("Unknown insurance account type: %s", insuranceType));
                return Portfolio.Type.OTHER;
        }
    }

    public int getStartDate() {
        return startDate;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public String getInsuranceType() {
        return insuranceType;
    }

    public String getInsureanceTypeName() {
        return insureanceTypeName;
    }

    public String getInsuranceOwner() {
        return insuranceOwner;
    }

    public String getInsuranceKnrtPlan() {
        return insuranceKnrtPlan;
    }

    public String getInsuranceMkodData() {
        return insuranceMkodData;
    }

    public String getInsuranceMkodValue() {
        return insuranceMkodValue;
    }

    public String getInsuranceNumber() {
        return insuranceNumber;
    }

    public String getInsuranceSkkat() {
        return insuranceSkkat;
    }

    public String getInsuranceStatusK() {
        return insuranceStatusK;
    }

    public String getInSuranceTjpKod() {
        return inSuranceTjpKod;
    }

    public String getMandateFl() {
        return mandateFl;
    }

    public double getPremium() {
        return premium;
    }

    public int getRowId() {
        return rowId;
    }

    public String getSebCustomerId() {
        return sebCustomerId;
    }

    public int getValueDate() {
        return valueDate;
    }

    public double getMarketValue() {
        return marketValue;
    }
}
