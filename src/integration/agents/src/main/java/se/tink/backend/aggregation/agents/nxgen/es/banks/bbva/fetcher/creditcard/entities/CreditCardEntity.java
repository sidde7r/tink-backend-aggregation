package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class CreditCardEntity {
    private String id;
    private String name;
    private String productDescription;
    private String productFamilyCode;
    private String subfamilyCode;
    private String subfamilyTypeCode;
    private String typeCode;
    private String TypeDescription;
    private String currency;
    private String availableBalance;
    private String availableBalances;
    private String branch;
    private String accountProductId;
    private String accountProductDescription;
    private String actualBalanceInOriginalCurrency;
    private String actualBanalce;

    @JsonIgnore
    public CreditCardAccount toTinkCreditCard() {
        return CreditCardAccount.builder(createUniqueIdFromName())
                .setAccountNumber(id)
                .setBalance(new Amount(currency, StringUtils.parseAmount(availableBalance)))
                .setName(name)
                .build();
    }

    @JsonIgnore
    private String createUniqueIdFromName() {
        String s = name.split("[*]")[1];
        return s;
    }
}
