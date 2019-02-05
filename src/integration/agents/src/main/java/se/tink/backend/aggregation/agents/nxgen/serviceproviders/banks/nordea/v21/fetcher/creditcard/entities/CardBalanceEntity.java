package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class CardBalanceEntity {
    @JsonProperty("atmAccountsIDs")
    private AccountIds atmAccountIds;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String authorityRoleType;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String balanceStatusCode;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String bankCode;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String cardGroupType;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String cardId;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String cardNumber;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String cardType;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String cardTypeExtension;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String country;
    @JsonProperty("creditAccount")
    private AccountIds creditAccountIds;
    @JsonDeserialize(using = NordeaHashMapDeserializer.Boolean.class)
    private Boolean creditChangePossible;
    @JsonDeserialize(using = NordeaHashMapDeserializer.Double.class)
    private Double creditLimit;
    @JsonDeserialize(using = NordeaHashMapDeserializer.Boolean.class)
    private Boolean creditLimitChangePossible;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String currency;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String debitNumber;
    @JsonDeserialize(using = NordeaHashMapDeserializer.Double.class)
    private Double fundsAvailable;
    @JsonDeserialize(using = NordeaHashMapDeserializer.Boolean.class)
    private Boolean hasCredit;
    @JsonDeserialize(using = NordeaHashMapDeserializer.Boolean.class)
    private Boolean hasParallelCards;
    @JsonDeserialize(using = NordeaHashMapDeserializer.Boolean.class)
    private Boolean hasPreapprovedRaise;
    @JsonDeserialize(using = NordeaHashMapDeserializer.Boolean.class)
    private Boolean isMainCard;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String otherCreditChangesLocation;
    @JsonDeserialize(using = NordeaHashMapDeserializer.Boolean.class)
    private Boolean ownTransferFrom;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String ownerName;
    @JsonProperty("paymentsAccountIDs")
    private AccountIds paymentAccountIds;
    private Map<Object, Object> specialStatusCodes;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String validityDate;
    @JsonDeserialize(using = NordeaHashMapDeserializer.Boolean.class)
    private Boolean view;

    public Double getCreditLimit() {
        return creditLimit;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public Amount getBalance() {
        return new Amount(currency, fundsAvailable - creditLimit);
    }

    public Amount getAvailableCredit() {
        return new Amount(currency, fundsAvailable);
    }

    public String getUniqueIdentifier() {
        return Optional.ofNullable(creditAccountIds).orElse(
                Optional.ofNullable(paymentAccountIds).orElse(atmAccountIds))
                .getAccountNumber();
    }

    public Boolean isViewable() {
        return view;
    }
}
