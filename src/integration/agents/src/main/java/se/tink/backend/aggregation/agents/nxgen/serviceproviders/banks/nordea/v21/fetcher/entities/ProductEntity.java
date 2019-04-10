package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.URL;

@JsonObject
public class ProductEntity {
    private Map<String, Object> accountType = new HashMap<String, Object>();
    private Map<String, Object> balance = new HashMap<String, Object>();
    private Map<String, Object> branchId = new HashMap<String, Object>();
    private Map<String, Object> cardGroup = new HashMap<String, Object>();
    private Map<String, Object> currency = new HashMap<String, Object>();
    private Map<String, Object> fundsAvailable = new HashMap<String, Object>();
    private Map<String, Object> isMainCard = new HashMap<String, Object>();
    private Map<String, Object> productCode = new HashMap<String, Object>();
    private Map<String, Object> productId = new HashMap<String, Object>();
    private Map<String, Object> productNumber = new HashMap<String, Object>();
    private Map<String, Object> productType = new HashMap<String, Object>();
    private Map<String, Object> productTypeExtension = new HashMap<String, Object>();
    private Map<String, Object> warningCode = new HashMap<String, Object>();

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String nickName;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String productName;

    public Map<String, Object> getAccountType() {
        return accountType;
    }

    public Map<String, Object> getBranchId() {
        return branchId;
    }

    public Map<String, Object> getCardGroup() {
        return cardGroup;
    }

    public Map<String, Object> getFundsAvailable() {
        return fundsAvailable;
    }

    public Map<String, Object> getIsMainCard() {
        return isMainCard;
    }

    public Optional<String> getNickName() {
        return Optional.ofNullable(nickName);
    }

    public String getProductName() {
        return productName;
    }

    public Map<String, Object> getProductCode() {
        return productCode;
    }

    public Map<String, Object> getProductId() {
        return productId;
    }

    public Map<String, Object> getProductNumber() {
        return productNumber;
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

    @JsonIgnore
    public boolean canView() {
        return getProductIdBoolean("@view");
    }

    @JsonIgnore
    public boolean canMakePayment() {
        return getProductIdBoolean("@paymentAccount");
    }

    @JsonIgnore
    public boolean canReceiveInternalTransfer() {
        return getProductIdBoolean("@ownTransferTo");
    }

    @JsonIgnore
    public boolean canMakeInternalTransfer() {
        return getProductIdBoolean("@ownTransferFrom");
    }

    @SuppressWarnings("rawtypes")
    public String getInternalId() {
        return (String) productId.get("$");
    }

    public String getAccountNumber() {
        return productNumber.get("$").toString();
    }

    @JsonIgnore
    public String getAccountId() {
        if (productId.containsKey("$")) {
            Object o = productId.get("$");
            if (o instanceof String) {
                return (String) o;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @JsonIgnore
    public String getNordeaAccountIdV2() {
        return URL.urlDecode(((Map<String, Object>) getProductId().get("@id")).get("$").toString());
    }

    @JsonIgnore
    public String getNordeaProductType() {
        return getProductType().get("$").toString();
    }

    @JsonIgnore
    public String getNordeaCardGroup() {
        return getCardGroup().get("$").toString();
    }

    @JsonIgnore
    public String getNordeaProductTypeExtension() {
        if (productTypeExtension == null || !productTypeExtension.containsKey("$")) {
            return null;
        }

        return productTypeExtension.get("$").toString();
    }

    public double getBalance() {
        if (balance != null && balance.containsKey("$")) {
            return AgentParsingUtils.parseAmount(balance.get("$").toString());
        }

        return 0;
    }

    public String getCurrency() {
        if (currency.containsKey("$")) {
            return currency.get("$").toString();
        }

        return "";
    }

    @SuppressWarnings("rawtypes")
    public Boolean getProductIdBoolean(String key) {
        Object object = productId.get(key);
        if (object instanceof Map) {
            Object value = ((Map) object).get("$");
            if (value instanceof String) {
                return Boolean.parseBoolean((String) value);
            } else if (value instanceof Boolean) {
                return (Boolean) value;
            }
        }
        return null;
    }
}
