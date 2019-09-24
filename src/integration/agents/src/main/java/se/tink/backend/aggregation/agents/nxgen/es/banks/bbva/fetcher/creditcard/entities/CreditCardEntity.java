package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.NoSuchElementException;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class CreditCardEntity {
    private String id;
    private String name;
    private String pan;
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
        String uniqueId = createUniqueIdFromName();
        return CreditCardAccount.builder(uniqueId)
                // Using as number the ID created in previous step as that's how it's shown in the
                // app
                .setAccountNumber(getMaskedPan().orElse(uniqueId))
                .putInTemporaryStorage(BbvaConstants.StorageKeys.ACCOUNT_ID, id)
                .setBalance(new Amount(currency, StringUtils.parseAmount(availableBalance)))
                .setName(name)
                .build();
    }

    @JsonIgnore
    public boolean isCreditCard() {
        return BbvaConstants.AccountType.CREDIT_CARD.equals(this.subfamilyTypeCode);
    }

    @JsonIgnore
    private String createUniqueIdFromName() {
        String[] nameParts = name.split("[*]");
        if (nameParts.length > 1) {
            return nameParts[1];
        } else {
            // use last 4 digits of pan
            return getPanLast4Digits()
                    .orElseThrow(
                            () -> new NoSuchElementException("can't determine the card number"));
        }
    }

    @JsonIgnore
    private Optional<String> getMaskedPan() {
        return getPanLast4Digits().map(pan -> "************" + pan);
    }

    @JsonIgnore
    private Optional<String> getPanLast4Digits() {
        return Optional.ofNullable(pan)
                .filter(pan -> pan.length() >= 4)
                .map(pan -> pan.substring(pan.length() - 4));
    }
}
