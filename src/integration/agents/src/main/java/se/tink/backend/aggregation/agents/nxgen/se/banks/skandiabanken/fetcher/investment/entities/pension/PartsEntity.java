package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.pension;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.rpc.PensionFundsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PartsEntity {

    @JsonProperty("Agreement")
    private String agreement;

    @JsonProperty("CanChangeFunds")
    private boolean canChangeFunds;

    @JsonProperty("CanDeposit")
    private boolean canDeposit;

    @JsonProperty("CanSeeHolding")
    private boolean canSeeHolding;

    @JsonProperty("CanSeePolicy")
    private boolean canSeePolicy;

    @JsonProperty("CanSeeTransactions")
    private boolean canSeeTransactions;

    @JsonProperty("Category")
    private int category;

    @JsonProperty("Category1")
    private String category1;

    @JsonProperty("Category2")
    private String category2;

    @JsonProperty("DateOfNetAssetValue")
    private String dateOfNetAssetValue;

    @JsonProperty("DateOfUpdate")
    private String dateOfUpdate;

    @JsonProperty("Dated")
    private String dated;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("EncrypedNationalIdentificationNumberOfFirstInsuredPerson")
    private String encryptedNationalIdentificationNumberOfFirstInsuredPerson;

    @JsonProperty("EncryptedNationalIdentificationNumberOfOwner")
    private String encryptedNationalIdentificationNumberOfOwner;

    @JsonProperty("EntitledToChangeFund")
    private String entitledToChangeFund;

    @JsonProperty("FundManagement")
    private String fundManagement;

    @JsonProperty("Holder")
    private HolderEntity holder;

    @JsonProperty("HoldingsInNumericalFund")
    private String holdingsInNumericalFund;

    @JsonProperty("HoldingsInsurance")
    private String holdingsInsurance;

    @JsonProperty("InsuranceCombination")
    private Object insuranceCombination;

    @JsonProperty("InsuranceInformation")
    private String insuranceInformation;

    @JsonProperty("InsuranceNumber")
    private String insuranceNumber;

    @JsonProperty("InsuranceType")
    private String insuranceType;

    @JsonProperty("InsuranceTypeName")
    private Object insuranceTypeName;

    @JsonProperty("IsAieInsurance")
    private boolean isAieInsurance;

    @JsonProperty("IsServicePension")
    private boolean isServicePension;

    @JsonProperty("Management")
    private String management;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("NameOfEntitledToChangeFund")
    private String nameOfEntitledToChangeFund;

    @JsonProperty("NameOfFirstInsuredPerson")
    private String nameOfFirstInsuredPerson;

    @JsonProperty("NationalIdentificationNumberOfFirstInsuredPerson")
    private String nationalIdentificationNumberOfFirstInsuredPerson;

    @JsonProperty("NationalIdentificationNumberOfOwner")
    private String nationalIdentificationNumberOfOwner;

    @JsonProperty("NewDepositInsurance")
    private String newDepositInsurance;

    @JsonProperty("Number")
    private String number;

    @JsonProperty("Origin")
    private int origin;

    @JsonProperty("OriginTypeName")
    private String originTypeName;

    @JsonProperty("PaymentsInProgress")
    private String paymentsInProgress;

    @JsonProperty("Reference")
    private String reference;

    @JsonProperty("ReportedDead")
    private String reportedDead;

    @JsonProperty("ResponsibleAdvisor")
    private String responsibleAdvisor;

    @JsonProperty("ResponsibleAdvisorCompany")
    private Object responsibleAdvisorCompany;

    @JsonProperty("ResponsibleAdvisorPhone")
    private Object responsibleAdvisorPhone;

    @JsonProperty("Roles")
    private Object roles;

    @JsonProperty("Scope")
    private String scope;

    @JsonProperty("Secrecy")
    private String secrecy;

    @JsonProperty("SkandiaLivValue")
    private double skandiaLivValue;

    @JsonProperty("SortingCategory1")
    private String sortingCategory1;

    @JsonProperty("Spec")
    private Object spec;

    @JsonProperty("StatusInsurance")
    private String statusInsurance;

    @JsonProperty("StatusInsuranceText")
    private String statusInsuranceText;

    @JsonProperty("StatusOfOngoingTrade")
    private String statusOfOngoingTrade;

    @JsonProperty("StatusOfOngoingTradeText")
    private Object statusOfOngoingTradeText;

    @JsonProperty("TJP_F")
    private String tjpF;

    @JsonProperty("TPSManagement")
    private String tpsManagement;

    @JsonProperty("TPSValue")
    private double tpsValue;

    @JsonProperty("TPSValueWithoutDecimals")
    private int tpsValueWithoutDecimals;

    @JsonProperty("TPS_F")
    private String tpsF;

    @JsonProperty("TaxClass")
    private String taxClass;

    @JsonProperty("TaxCode")
    private String taxCode;

    @JsonProperty("TotalCurrentValueInsurance")
    private String totalCurrentValueInsurance;

    @JsonProperty("TradeInsurance")
    private String tradeInsurance;

    @JsonProperty("TransactionInsurance")
    private String transactionInsurance;

    @JsonProperty("Type")
    private int type;

    @JsonProperty("TypeName")
    private String typeName;

    @JsonProperty("UnitLinkNumber")
    private String unitLinkNumber;

    @JsonProperty("Validity")
    private String validity;

    @JsonProperty("Value")
    private double value;

    @JsonProperty("ValueWithoutDecimals")
    private int valueWithoutDecimals;

    @JsonIgnore private PensionFundsResponse pensionFunds;

    @JsonIgnore
    public String getNumber() {
        return number;
    }

    public String getTypeName() {
        return typeName;
    }

    public double getValue() {
        return value;
    }

    public HolderEntity getHolder() {
        return holder;
    }

    public String getAgreement() {
        return agreement;
    }

    public boolean isCanSeeHolding() {
        return canSeeHolding;
    }

    public String getEncryptedNationalIdentificationNumberOfFirstInsuredPerson() {
        return encryptedNationalIdentificationNumberOfFirstInsuredPerson;
    }

    public void setPensionFunds(PensionFundsResponse pensionFunds) {
        this.pensionFunds = pensionFunds;
    }

    public PensionFundsResponse getPensionFunds() {
        return pensionFunds;
    }
}
