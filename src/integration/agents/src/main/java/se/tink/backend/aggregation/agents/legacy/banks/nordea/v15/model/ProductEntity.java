package se.tink.backend.aggregation.agents.banks.nordea.v15.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.se.ClearingNumber;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductEntity implements GeneralAccountEntity {

    private static final String personalAccountClearingNumber = "3300";
    private static final Set<String> personalAccountCodes = Sets.newHashSet("SE0000", "SE0200", "SE0300");

    private Map<String, Object> accountType = new HashMap<String, Object>();
    private Map<String, Object> balance = new HashMap<String, Object>();
    private Map<String, Object> branchId = new HashMap<String, Object>();
    private Map<String, Object> cardGroup = new HashMap<String, Object>();
    private Map<String, Object> currency = new HashMap<String, Object>();
    private Map<String, Object> fundsAvailable = new HashMap<String, Object>();
    private Map<String, Object> isMainCard = new HashMap<String, Object>();
    private Map<String, Object> nickName = new HashMap<String, Object>();
    private Map<String, Object> productCode = new HashMap<String, Object>();
    private Map<String, Object> productId = new HashMap<String, Object>();
    private Map<String, Object> productNumber = new HashMap<String, Object>();
    private Map<String, Object> productType = new HashMap<String, Object>();
    private Map<String, Object> productTypeExtension = new HashMap<String, Object>();
    private Map<String, Object> warningCode = new HashMap<String, Object>();

    public Map<String, Object> getAccountType() {
        return accountType;
    }

    public Map<String, Object> getBranchId() {
        return branchId;
    }

    public Map<String, Object> getCardGroup() {
        return cardGroup;
    }

    public Map<String, Object> getCurrency() {
        return currency;
    }

    public Map<String, Object> getFundsAvailable() {
        return fundsAvailable;
    }

    public Map<String, Object> getIsMainCard() {
        return isMainCard;
    }

    public Map<String, Object> getNickName() {
        return nickName;
    }

    public Map<String, Object> getProductCode() {
        return productCode;
    }

    public Map<String, Object> getProductId() {
        return productId;
    }

    public Map<String, Object> getProductType() {
        return productType;
    }

    public Map<String, Object> getProductTypeExtension() {
        return productTypeExtension;
    }

    public Map<String, Object> getWarningCode() {
        return warningCode;
    }

    public Optional<String> getBalance() {

        if (balance != null && balance.containsKey("$")) {
            return Optional.of(balance.get("$").toString());
        }
        return Optional.empty();
    }

    public Optional<String> getProductNumber() {

        if (productNumber != null && productNumber.containsKey("$")) {
            return Optional.of(productNumber.get("$").toString());
        }
        return Optional.empty();
    }

    public void setAccountType(Map<String, Object> accountType) {
        this.accountType = accountType;
    }

    public void setBalance(Map<String, Object> balance) {
        this.balance = balance;
    }

    public void setBranchId(Map<String, Object> branchId) {
        this.branchId = branchId;
    }

    public void setCardGroup(Map<String, Object> cardGroup) {
        this.cardGroup = cardGroup;
    }

    public void setCurrency(Map<String, Object> currency) {
        this.currency = currency;
    }

    public void setFundsAvailable(Map<String, Object> fundsAvailable) {
        this.fundsAvailable = fundsAvailable;
    }

    public void setIsMainCard(Map<String, Object> isMainCard) {
        this.isMainCard = isMainCard;
    }

    public void setNickName(Map<String, Object> nickName) {
        this.nickName = nickName;
    }

    public void setProductCode(Map<String, Object> productCode) {
        this.productCode = productCode;
    }

    public void setProductId(Map<String, Object> productId) {
        this.productId = productId;
    }

    public void setProductNumber(Map<String, Object> productNumber) {
        this.productNumber = productNumber;
    }

    public void setProductType(Map<String, Object> productType) {
        this.productType = productType;
    }

    public void setProductTypeExtension(Map<String, Object> productTypeExtension) {
        this.productTypeExtension = productTypeExtension;
    }

    public void setWarningCode(Map<String, Object> warningCode) {
        this.warningCode = warningCode;
    }

    public String getAccountNumber(boolean includeClearingNumber) {

        String accountNumber = productNumber.get("$").toString();

        if (includeClearingNumber) {
            String accountTypeCode = productTypeExtension.get("$").toString();

            if (accountTypeCode != null && personalAccountCodes.contains(accountTypeCode.toUpperCase())
                    && accountNumber.length() == 10) {
                return personalAccountClearingNumber + accountNumber;
            }
        }

        return accountNumber;
    }

    public boolean canMakePayment() {
        return Objects.equal(getProductIdBoolean("@paymentAccount"), true);
    }

    public boolean canReceiveInternalTransfer() {
        return Objects.equal(getProductIdBoolean("@ownTransferTo"), true);
    }

    public boolean canMakeInternalTransfer() {
        return Objects.equal(getProductIdBoolean("@ownTransferFrom"), true);
    }

    @SuppressWarnings("rawtypes")
    public String getInternalId() {
        Map map = (Map) productId.get("@id");

        return (String) map.get("$");
    }

    @JsonIgnore
    public String getAccountId() {
        if (productId.containsKey("$")) {
            Object o = productId.get("$");
            if (o instanceof String) {
                return (String)o;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @JsonIgnore
    public String getNordeaAccountIdV2() {
        return ((Map<String, Object>) getProductId().get("@id")).get("$").toString();
    }

    @JsonIgnore
    public String getNordeaProductType() {
        return getProductType().get("$").toString();
    }

    @JsonIgnore
    public String getNordeaProductTypeExtension() {
        Map<String, Object> productTypeExtension = getProductTypeExtension();

        if (productTypeExtension == null || !productTypeExtension.containsKey("$")) {
            return null;
        }

        return productTypeExtension.get("$").toString();
    }

    @SuppressWarnings("rawtypes")
    public Boolean getProductIdBoolean(String key) {
        Object object = productId.get(key);
        if (object instanceof Map) {
            Object value = ((Map)object).get("$");
            if (value instanceof String) {
                return Boolean.parseBoolean((String) value);
            } else if (value instanceof Boolean) {
                return (Boolean) value;
            }
        }
        return null;
    }

    /*
     * The methods below are for general purposes
     */

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        return new SwedishIdentifier(getAccountNumber(true));
    }

    @Override
    public String generalGetBank() {
        return ClearingNumber.Bank.NORDEA.getDisplayName();
    }

    @Override
    public String generalGetName() {
        Map<String, Object> map = getNickName();
        if (map != null && map.containsKey("$")) {
            return map.get("$").toString();
        }
        return null;
    }
}
