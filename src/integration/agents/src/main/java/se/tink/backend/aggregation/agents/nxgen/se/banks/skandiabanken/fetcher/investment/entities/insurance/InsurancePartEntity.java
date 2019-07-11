package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.insurance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.HolderEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.SecuritiesAccountsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InsurancePartEntity {
    @JsonProperty("Holder")
    private HolderEntity holder;

    @JsonProperty("AllowedTradeNationalIdentificationNumber")
    private String allowedTradeNationalIdentificationNumber;

    @JsonProperty("IsServicePension")
    private boolean isServicePension;

    @JsonProperty("DepotInsuranceFoundInAbasec")
    private boolean depotInsuranceFoundInAbasec;

    @JsonProperty("DepotInsuranceResponsibilityCode")
    private String depotInsuranceResponsibilityCode;

    @JsonProperty("Status")
    private int status;

    @JsonProperty("DepotReference")
    private DepotReferenceEntity depotReference;

    @JsonProperty("PaymentsYTD")
    private double paymentsYTD;

    @JsonProperty("DepositedYTD")
    private double depositedYTD;

    @JsonProperty("Performance")
    private double performance;

    @JsonProperty("AvailableForTrading")
    private double availableForTrading;

    @JsonProperty("Category")
    private int category;

    @JsonProperty("InsuranceTypeName")
    private String insuranceTypeName;

    @JsonProperty("Agreement")
    private String agreement;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("TaxClass")
    private String taxClass;

    @JsonProperty("Value")
    private BigDecimal value;

    @JsonProperty("ValueWithoutDecimals")
    private int valueWithoutDecimals;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Number")
    private String number;

    @JsonProperty("Type")
    private int type;

    @JsonProperty("TypeName")
    private String typeName;

    @JsonProperty("Category1")
    private String category1;

    @JsonProperty("Category2")
    private String category2;

    @JsonProperty("IsAieInsurance")
    private boolean isAieInsurance;

    @JsonProperty("Origin")
    private int origin;

    @JsonProperty("OriginTypeName")
    private String originTypeName;

    @JsonProperty("Management")
    private String management;

    @JsonProperty("SecuritiesAccount")
    private SecuritiesAccountsEntity securitiesAccount;

    @JsonIgnore
    public HolderEntity getHolder() {
        if (isSecuritiesAccount() && securitiesAccount.getHolder() != null) {
            return securitiesAccount.getHolder();
        }
        return holder;
    }

    @JsonIgnore
    public BigDecimal getValue() {
        if (isSecuritiesAccount() && securitiesAccount.getTotalValue() != null) {
            return securitiesAccount.getTotalValue();
        }
        return value;
    }

    @JsonIgnore
    public boolean isSecuritiesAccount() {
        return securitiesAccount != null;
    }

    @JsonIgnore
    public SecuritiesAccountsEntity getSecuritiesAccount() {
        return securitiesAccount;
    }
}
