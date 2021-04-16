package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.insurance.InsuranceReferenceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SecuritiesAccountsEntity {
    @JsonProperty("TotalValue")
    private BigDecimal totalValue;

    @JsonProperty("TotalValueWithoutDecimals")
    private int totalValueWithoutDecimals;

    @JsonProperty("AvailableForTrading")
    private BigDecimal availableForTrading;

    @JsonProperty("AvailableForTradingWithoutDecimals")
    private int availableForTradingWithoutDecimals;

    @JsonProperty("TotalDeposits")
    private BigDecimal totalDeposits;

    @JsonProperty("Currency")
    private String currency = "";

    @JsonProperty("Status")
    private int status;

    @JsonProperty("Holder")
    private HolderEntity holder;

    @JsonProperty("MifidInformation")
    private MifidInformationEntity mifidInformation;

    @JsonProperty("OwnedBySelf")
    private boolean ownedBySelf;

    @JsonProperty("Type")
    private int type;

    @JsonProperty("SortGroupNumber")
    private int sortGroupNumber;

    @JsonProperty("IsNamed")
    private boolean isNamed;

    @JsonProperty("AccountTypeName")
    private String accountTypeName = "";

    @JsonProperty("AccountRepresentative")
    private AccountRepresentativeEntity accountRepresentative;

    @JsonProperty("AccountResponsible")
    private String accountResponsible = "";

    @JsonProperty("RegDate")
    private String regDate = "";

    @JsonProperty("SettledAmount")
    private BigDecimal settledAmount;

    @JsonProperty("YearDeposits")
    private BigDecimal yearDeposits;

    @JsonProperty("IsWithdrawalAllowed")
    private boolean isWithdrawalAllowed;

    @JsonProperty("FSAClassification")
    private int fSAClassification;

    @JsonProperty("EncryptedNumber")
    private String encryptedNumber = "";

    @JsonProperty("HasDepositedValues")
    private boolean hasDepositedValues;

    @JsonProperty("HasWithdrawalValue")
    private boolean hasWithdrawalValue;

    @JsonProperty("HasCredit")
    private boolean hasCredit;

    @JsonProperty("DepositedYTD")
    private BigDecimal depositedYTD;

    @JsonProperty("DepositedTotal")
    private BigDecimal depositedTotal;

    @JsonProperty("ReturnPercentage")
    private BigDecimal returnPercentage;

    @JsonProperty("ReturnMonetary")
    private BigDecimal returnMonetary;

    @JsonProperty("PaymentsYTD")
    private BigDecimal paymentsYTD;

    @JsonProperty("PaymentAmount")
    private BigDecimal paymentAmount;

    @JsonProperty("IsInPaymentMode")
    private boolean isInPaymentMode;

    @JsonProperty("InsuranceReference")
    private InsuranceReferenceEntity insuranceReference;

    @JsonProperty("DisplayTypeName")
    private String displayTypeName = "";

    @JsonProperty("DisplayName")
    private String displayName = "";

    @JsonProperty("Name")
    private String name = "";

    @JsonProperty("Number")
    private String number = "";

    @JsonProperty("TypeName")
    private String typeName = "";

    @JsonIgnore
    public String getNumber() {
        return number;
    }

    @JsonIgnore
    public String getDisplayName() {
        return displayName;
    }

    @JsonIgnore
    public String getEncryptedNumber() {
        return encryptedNumber;
    }

    @JsonIgnore
    public BigDecimal getTotalValue() {
        return totalValue;
    }

    @JsonIgnore
    public HolderEntity getHolder() {
        return holder;
    }

    @JsonIgnore
    public String getTypeName() {
        return typeName;
    }

    @JsonIgnore
    public String getAccountTypeName() {
        return accountTypeName;
    }
}
