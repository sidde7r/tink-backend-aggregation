package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.creditcards.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1AmountUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.strings.StringUtils;

@JsonObject
@Data
public class CreditCardAccountEntity {
    private String id;
    private String name;
    private String formattedNumber;
    private String cardType;
    private String balanceInteger;
    private String balanceFraction;
    private String creditAvailableInteger;
    private String creditAvailableFraction;
    private boolean statusBlocked;
    private boolean statusInactive;

    @JsonIgnore
    public CreditCardAccount toAccount() {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(buildCardDetails())
                .withoutFlags()
                .withId(buildIdModule())
                .setApiIdentifier(id)
                .build();
    }

    private CreditCardModule buildCardDetails() {
        return CreditCardModule.builder()
                .withCardNumber(formattedNumber)
                .withBalance(Sparebank1AmountUtils.constructAmount(balanceInteger, balanceFraction))
                .withAvailableCredit(
                        Sparebank1AmountUtils.constructAmount(
                                creditAvailableInteger, creditAvailableFraction))
                .withCardAlias(name)
                .build();
    }

    private IdModule buildIdModule() {
        return IdModule.builder()
                .withUniqueIdentifier(id)
                .withAccountNumber(formattedNumber)
                .withAccountName(name)
                .addIdentifier(
                        AccountIdentifier.create(
                                AccountIdentifierType.NO,
                                StringUtils.removeNonAlphaNumeric(formattedNumber)))
                .setProductName(cardType)
                .build();
    }

    public boolean isActive() {
        return !statusBlocked && !statusInactive;
    }
}
