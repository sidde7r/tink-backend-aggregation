package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.entitites;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class CreditCardEntity {
    private String id;
    private String accountNumber;
    private ProductEntity product;
    private OwnerEntity owner;
    private PropertiesEntity properties;
    private AccountBalanceEntity accountBalance;
    private List<CardsEntity> cards;
    @Getter private Map<String, LinkEntity> links;

    @JsonIgnore
    public CreditCardAccount toTinkCreditCard() {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(cards.get(0).getMaskedPAN())
                                .withBalance(
                                        ExactCurrencyAmount.of(
                                                accountBalance.getAccountingBalance(),
                                                properties.getCurrencyCode()))
                                .withAvailableCredit(
                                        ExactCurrencyAmount.of(
                                                accountBalance.getAvailableBalance(),
                                                properties.getCurrencyCode()))
                                .withCardAlias(product.getName())
                                .build())
                .withPaymentAccountFlag()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountNumber)
                                .withAccountNumber(accountNumber)
                                .withAccountName(product.getName())
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.PAYMENT_CARD_NUMBER,
                                                cards.get(0).getMaskedPAN()))
                                .build())
                .addHolderName(owner.getName())
                .setApiIdentifier(id)
                .putInTemporaryStorage(
                        SparebankenSorConstants.Storage.TEMPORARY_STORAGE_CREDIT_CARD_LINKS, links)
                .build();
    }
}
