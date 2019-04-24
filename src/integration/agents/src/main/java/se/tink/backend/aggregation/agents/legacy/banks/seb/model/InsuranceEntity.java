package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.assertj.core.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.banks.seb.SEBApiConstants;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class InsuranceEntity {
    @JsonIgnore private static final Logger log = LoggerFactory.getLogger(InsuranceEntity.class);

    @JsonProperty("FORS_NR")
    private String insuranceNumber;

    @JsonProperty("KUND_NAMN_FORSAKR")
    private String customerName;

    @JsonProperty("KOMPL_TARIFF_KOD")
    private String tariffCode;

    @JsonProperty("ANDEL_VARDE_BEL")
    private double marketValue;

    @JsonProperty("TYP")
    private String type;

    @JsonProperty("VERKS_GREN_KOD")
    private String
            accountType; // I'm really not sure what to call this, but the field value is "IPS" for
    // one object.

    @JsonProperty("DETAIL_URL")
    private String detailUrl;

    @JsonIgnore
    public Account toAccount() {
        Account account = new Account();

        account.setAccountNumber(insuranceNumber);
        account.setHolderName(customerName);
        account.setBalance(marketValue);
        account.setBankId(insuranceNumber);
        account.setName(StringUtils.firstLetterUppercaseFormatting(type.trim()));
        account.setType(AccountTypes.INVESTMENT);

        return account;
    }

    @JsonIgnore
    public Portfolio toPortfolio() {
        Portfolio portfolio = new Portfolio();

        portfolio.setTotalValue(marketValue);
        portfolio.setRawType(type.trim());
        portfolio.setType(getPortfolioType());
        portfolio.setUniqueIdentifier(insuranceNumber);

        return portfolio;
    }

    @JsonIgnore
    private Portfolio.Type getPortfolioType() {
        if (Strings.isNullOrEmpty(type)) {
            return Portfolio.Type.OTHER;
        }

        switch (type.toLowerCase()) {
            case SEBApiConstants.PortfolioType.ENDOWMENT_INSURANCE:
            case SEBApiConstants.PortfolioType.PENSION_SAVINGS_FUND:
            case SEBApiConstants.PortfolioType.SAFE_PENSION_INSURANCE:
            case SEBApiConstants.PortfolioType.PENSION_INSURANCE:
                return Portfolio.Type.KF;
            case SEBApiConstants.PortfolioType.OCCUPATIONAL_PENSION:
                return Portfolio.Type.PENSION;
            default:
                log.warn(String.format("Unknown insurance type: %s", type));
                return Portfolio.Type.OTHER;
        }
    }

    public String getInsuranceNumber() {
        return insuranceNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getTariffCode() {
        return tariffCode;
    }

    public String getType() {
        return type;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getDetailUrl() {
        return detailUrl;
    }
}
