package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmericanExpressConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmericanExpressUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.identifiers.MaskedPanIdentifier;
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

    public CreditCardAccount toCreditCardAccount(Map<Integer, String> statementMap) {
        final ExactCurrencyAmount balance =
                new ExactCurrencyAmount(
                        BigDecimal.ZERO,
                        Currency.getInstance(
                                        Locale.forLanguageTag(
                                                holder.getLocalizationPreferences()
                                                        .getCurrencyLocale()))
                                .getCurrencyCode());
        final String uniqueId =
                AmericanExpressUtils.formatAccountId(identifiers.getDisplayAccountNumber());
        final String pan = identifiers.getDisplayAccountNumber();
        final String cardName =
                product.getDigitalInfo().getProductDesc() + " - " + uniqueId.substring(4);
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(pan)
                                .withBalance(balance)
                                .withAvailableCredit(
                                        new ExactCurrencyAmount(
                                                new BigDecimal(0), balance.getCurrencyCode()))
                                .withCardAlias(cardName)
                                .build())
                .withPaymentAccountFlag()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(uniqueId)
                                .withAccountNumber(pan)
                                .withAccountName(cardName)
                                .addIdentifier(new MaskedPanIdentifier(pan))
                                .build())
                .addHolderName(holder.getProfile().getEmbossedName())
                .putInTemporaryStorage(AmericanExpressConstants.StorageKey.STATEMENTS, statementMap)
                .build();
    }
}
