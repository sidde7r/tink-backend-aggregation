package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto;

import java.math.BigDecimal;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmericanExpressUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class SupplementaryAccountsItem {

    private ProductDto product;

    private IdentifiersDto identifiers;

    private HolderDto holder;

    private StatusDto status;

    public ProductDto getProduct() {
        return product;
    }

    public IdentifiersDto getIdentifiers() {
        return identifiers;
    }

    public HolderDto getHolder() {
        return holder;
    }

    public StatusDto getStatus() {
        return status;
    }

    public CreditCardAccount toCreditCardAccount(ExactCurrencyAmount balance) {
        final String iban =
                AmericanExpressUtils.formatAccountId(identifiers.getDisplayAccountNumber());

        final String cardName =
                product.getDigitalInfo().getProductDesc() + " - " + iban.substring(4);
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(iban)
                                .withBalance(balance)
                                .withAvailableCredit(
                                        new ExactCurrencyAmount(
                                                new BigDecimal(0), balance.getCurrencyCode()))
                                .withCardAlias(cardName)
                                .build())
                .withPaymentAccountFlag()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(cardName)
                                .addIdentifier(new IbanIdentifier(iban.replace("-", "")))
                                .build())
                .addHolderName(holder.getProfile().getEmbossedName())
                .build();
    }
}
