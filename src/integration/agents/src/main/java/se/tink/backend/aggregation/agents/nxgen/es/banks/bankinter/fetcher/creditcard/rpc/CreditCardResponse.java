package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.creditcard.rpc;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.creditcard.entities.PaginationKey;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.rpc.HtmlResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class CreditCardResponse extends HtmlResponse {
    static final Pattern MASKED_CARD_NUMBER_PATTERN = Pattern.compile("^\\*{12}(\\d{4})$");
    private static final Pattern TRANSACTIONS_JSF_SOURCE_PATTERN =
            Pattern.compile(
                    "source:'(movimientos-form:j_id[_0-9a-f]+)',process:'@all',update:'movimientos-form:listaMovimientos");

    public CreditCardResponse(String body) {
        super(body);
    }

    public CreditCardAccount toCreditCardAccount() {
        final String maskedCardNumber;
        final String cardNumberDigits;
        try {
            maskedCardNumber = getMaskedCardNumber();
            cardNumberDigits = getCardNumberDigits();
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }

        final String cardName = getName();
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(maskedCardNumber)
                                .withBalance(getBalance())
                                .withAvailableCredit(getAvailableCredit())
                                .withCardAlias(cardName)
                                .build())
                .withoutFlags()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(cardNumberDigits)
                                .withAccountNumber(cardNumberDigits)
                                .withAccountName(cardName)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                Type.PAYMENT_CARD_NUMBER, cardNumberDigits))
                                .build())
                .putInTemporaryStorage(StorageKeys.FIRST_PAGINATION_KEY, getFirstPaginationKey())
                .build();
    }

    private String getName() {
        return evaluateXPath("//a[contains(@class,'titulo_cuenta')]/text()", String.class).trim();
    }

    /**
     * @return Masked credit card number, as 12 asterisks and 4 digits: "************NNNN"
     * @throws ParseException if number can't be found
     */
    private String getMaskedCardNumber() throws ParseException {
        final String maskedCardNumber =
                evaluateXPath(
                                "//div[contains(@class,'head_datos')]//div[contains(@class,'cuenta')]",
                                String.class)
                        .replaceAll("\\s+", "");
        if (!MASKED_CARD_NUMBER_PATTERN.matcher(maskedCardNumber).find()) {
            throw new ParseException(
                    "Masked card number doesn't match regex: " + maskedCardNumber, 0);
        }
        return maskedCardNumber;
    }

    /**
     * @return Last 4 digits of credit card number
     * @throws ParseException if number can't be found
     */
    private String getCardNumberDigits() throws ParseException {
        return getMaskedCardNumber().substring(12);
    }

    private ExactCurrencyAmount getBalance() {
        return parseAmount(
                evaluateXPath(
                                "//div[contains(@class,'saldoGastado')]//p[contains(@class,'cifra')]",
                                String.class)
                        .replaceAll("\\s+", ""));
    }

    private ExactCurrencyAmount getAvailableCredit() {
        return parseAmount(
                evaluateXPath(
                                "//div[contains(@class,'saldoDisponible')]//p[contains(@class,'cifra')]",
                                String.class)
                        .replaceAll("\\s+", ""));
    }

    private PaginationKey getFirstPaginationKey() {
        final Matcher matcher = TRANSACTIONS_JSF_SOURCE_PATTERN.matcher(body);
        if (matcher.find()) {
            final String jsfSource = matcher.group(1);
            final String formId = jsfSource.split(":")[0];
            final String viewState = getViewState(formId);
            return new PaginationKey(jsfSource, viewState);
        } else {
            throw new IllegalStateException("Could not get pagination key for transactions.");
        }
    }
}
