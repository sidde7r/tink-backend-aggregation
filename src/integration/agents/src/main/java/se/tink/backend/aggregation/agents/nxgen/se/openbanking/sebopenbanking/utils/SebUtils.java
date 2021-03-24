package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.utils;

import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities.PaymentProduct;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class SebUtils {

    private SebUtils() {}

    private static final List<String> IBAN_PRODUCT_SCOPES =
            Arrays.asList(PaymentProduct.SEPA_CREDIT_TRANSFER.getValue());

    public static boolean isValidAccountForProduct(String paymentProduct, String accountNumber) {
        if (IBAN_PRODUCT_SCOPES.stream().anyMatch(paymentProduct::equalsIgnoreCase)) {
            IbanIdentifier ibanIdentifier = new IbanIdentifier(accountNumber);
            return ibanIdentifier.isValid();
        }
        return true;
    }

    public static ExactCurrencyAmount getZeroBalance(String currencyCode) {
        return ExactCurrencyAmount.zero(currencyCode);
    }

    public static CreditCardAccount createSubCreditCard(
            CreditCardAccount account, String creditCardNumber, String nameOnCard) {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(creditCardNumber)
                                .withBalance(
                                        SebUtils.getZeroBalance(
                                                account.getExactBalance().getCurrencyCode()))
                                .withAvailableCredit(
                                        SebUtils.getZeroBalance(
                                                account.getExactBalance().getCurrencyCode()))
                                .withCardAlias(nameOnCard)
                                .build())
                .withPaymentAccountFlag()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(creditCardNumber)
                                .withAccountNumber(creditCardNumber)
                                .withAccountName(creditCardNumber)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.PAYMENT_CARD_NUMBER,
                                                creditCardNumber))
                                .build())
                .addHolderName(nameOnCard)
                .build();
    }
}
