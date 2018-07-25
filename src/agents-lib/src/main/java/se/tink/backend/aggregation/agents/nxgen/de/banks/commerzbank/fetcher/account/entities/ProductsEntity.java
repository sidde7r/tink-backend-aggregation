package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account.entities;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;
import se.tink.libraries.account.AccountIdentifier;

@JsonObject
public class ProductsEntity {
    private ProductTypeEntity productType;
    private Object convertedBalance;
    private BalanceEntity originalBalance;
    private String bankCode;
    private String productNumber;
    private String tnvCategory;
    private List<String> validActions;
    private String identifier;
    private String bic;
    private String currency;
    private String externalAccountNumber;
    private String iban;
    private String internalAccountNumber;
    private String technicalAccountNumber;
    private String branch;
    private ProductIdEntity productId;
    private BalanceEntity externalLimit;
    private String externalLimitInterestRate;
    private String openingDate;
    private Object referenceAccount;
    private String underAccountDescriptionOne;
    private String underAccountDescriptionTwo;
    private String productVariationType;
    private String currentServerDate;
    private CreditLineDetailsEntity creditLineDetails;
    private InterestStatementEntity interestStatement;
    private String shipment;
    private String proximateInterestPayment;
    private List<DebitCardsEntity> debitCards;
    private boolean pfmenabled;
    private String accountCharacteristic;
    private String accountConditionModel;
    private Object accountCustomerNote;
    private String accountType;
    private Object blockingDateUntil;
    private Object blockingNote;
    private String branchNumber;
    private String capitalAccountCurrency;
    private String capitalAccountNumber;
    private String dispatchType;
    private String encashmentAccountCurrency;
    private String encashmentAccountNumber;
    private String externalSecuritiesAccountNumber;
    private String externalStatementCalculationCurrency;
    private String externalStatementCalculationSchedule;
    private String feeAccountCurrency;
    private String feeAccountNumber;
    private String ffttFlag;
    private String ifttFlag;
    private String internalSecuritiesAccountNumber;
    private String internalStatementCalculationCurrency;
    private String internalStatementCalculationSchedule;
    private Object limit;
    private Object ownershipFlag;
    private String profitAccountCurrency;
    private String profitAccountNumber;
    private String proxyPolicy;
    private String proxyPolicyValidUntil;
    private Object shortDescription;
    private String statementCalculationMethod;
    private String taxCertificatePolicy;
    private String technicalSecuritiesAccountNumber;
    private String valuationCutOffDate;
    private int numberOfPositions;
    private boolean priceForPositionsAvailable;

    public ProductTypeEntity getProductType() {
        return productType;
    }

    public BalanceEntity getOriginalBalance() {
        return originalBalance;
    }

    public String getBankCode() {
        return bankCode;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public String getTnvCategory() {
        return tnvCategory;
    }

    public List<String> getValidActions() {
        return validActions;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getBic() {
        return bic;
    }

    public String getCurrency() {
        return currency;
    }

    public String getExternalAccountNumber() {
        return externalAccountNumber;
    }

    public String getIban() {
        return iban;
    }

    public String getInternalAccountNumber() {
        return internalAccountNumber;
    }

    public String getTechnicalAccountNumber() {
        return technicalAccountNumber;
    }

    public String getBranch() {
        return branch;
    }

    public ProductIdEntity getProductId() {
        Preconditions.checkState(productId != null, "Expected a Product Id object but it was null");
        return productId;
    }

    public BalanceEntity getExternalLimit() {
        return externalLimit;
    }

    public String getExternalLimitInterestRate() {
        return externalLimitInterestRate;
    }

    public String getOpeningDate() {
        return openingDate;
    }

    public String getUnderAccountDescriptionOne() {
        return underAccountDescriptionOne;
    }

    public String getUnderAccountDescriptionTwo() {
        return underAccountDescriptionTwo;
    }

    public String getProductVariationType() {
        return productVariationType;
    }

    public String getCurrentServerDate() {
        return currentServerDate;
    }

    public CreditLineDetailsEntity getCreditLineDetails() {
        return creditLineDetails;
    }

    public InterestStatementEntity getInterestStatement() {
        return interestStatement;
    }

    public String getShipment() {
        return shipment;
    }

    public String getProximateInterestPayment() {
        return proximateInterestPayment;
    }

    public List<DebitCardsEntity> getDebitCards() {
        return debitCards;
    }

    public boolean isPfmenabled() {
        return pfmenabled;
    }

    public String getAccountCharacteristic() {
        return accountCharacteristic;
    }

    public String getAccountConditionModel() {
        return accountConditionModel;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getBranchNumber() {
        return branchNumber;
    }

    public String getCapitalAccountCurrency() {
        return capitalAccountCurrency;
    }

    public String getCapitalAccountNumber() {
        return capitalAccountNumber;
    }

    public String getDispatchType() {
        return dispatchType;
    }

    public String getEncashmentAccountCurrency() {
        return encashmentAccountCurrency;
    }

    public String getEncashmentAccountNumber() {
        return encashmentAccountNumber;
    }

    public String getExternalSecuritiesAccountNumber() {
        return externalSecuritiesAccountNumber;
    }

    public String getExternalStatementCalculationCurrency() {
        return externalStatementCalculationCurrency;
    }

    public String getExternalStatementCalculationSchedule() {
        return externalStatementCalculationSchedule;
    }

    public String getFeeAccountCurrency() {
        return feeAccountCurrency;
    }

    public String getFeeAccountNumber() {
        return feeAccountNumber;
    }

    public String getFfttFlag() {
        return ffttFlag;
    }

    public String getIfttFlag() {
        return ifttFlag;
    }

    public String getInternalSecuritiesAccountNumber() {
        return internalSecuritiesAccountNumber;
    }

    public String getInternalStatementCalculationCurrency() {
        return internalStatementCalculationCurrency;
    }

    public String getInternalStatementCalculationSchedule() {
        return internalStatementCalculationSchedule;
    }

    public String getProfitAccountCurrency() {
        return profitAccountCurrency;
    }

    public String getProfitAccountNumber() {
        return profitAccountNumber;
    }

    public String getProxyPolicy() {
        return proxyPolicy;
    }

    public String getProxyPolicyValidUntil() {
        return proxyPolicyValidUntil;
    }

    public String getStatementCalculationMethod() {
        return statementCalculationMethod;
    }

    public String getTaxCertificatePolicy() {
        return taxCertificatePolicy;
    }

    public String getTechnicalSecuritiesAccountNumber() {
        return technicalSecuritiesAccountNumber;
    }

    public String getValuationCutOffDate() {
        return valuationCutOffDate;
    }

    public int getNumberOfPositions() {
        return numberOfPositions;
    }

    public boolean isPriceForPositionsAvailable() {
        return priceForPositionsAvailable;
    }

    public AccountTypes getType() {
        switch (productType.getDisplayCategoryIndex()){
            case 1:
                return AccountTypes.CHECKING;
            default:
                return AccountTypes.OTHER;
        }
    }

    public Amount getTinkBalance() {
        return new Amount(getCurrency(), originalBalance.getValue());
    }

    public TransactionalAccount toTransactionalAccount() {

        return TransactionalAccount.builder(getType(), getInternalAccountNumber(), getTinkBalance())
                .setName(getProductType().getProductName())
                .setAccountNumber(getInternalAccountNumber())
                .addIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                .addToTemporaryStorage(CommerzbankConstants.HEADERS.IDENTIFIER, getProductId().getIdentifier())
                .addToTemporaryStorage(CommerzbankConstants.HEADERS.PRODUCT_TYPE, getProductId().getProductType())
                .build();
    }


    public Object getConvertedBalance() {
        return convertedBalance;
    }

    public Object getReferenceAccount() {
        return referenceAccount;
    }

    public Object getAccountCustomerNote() {
        return accountCustomerNote;
    }

    public Object getBlockingDateUntil() {
        return blockingDateUntil;
    }

    public Object getBlockingNote() {
        return blockingNote;
    }

    public Object getLimit() {
        return limit;
    }

    public Object getOwnershipFlag() {
        return ownershipFlag;
    }

    public Object getShortDescription() {
        return shortDescription;
    }
}
