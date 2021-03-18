package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.creditcard.rpc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.CardDetails;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.CardState;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.CardTypes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.creditcard.entities.PaginationKey;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.rpc.HtmlResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class CreditCardResponse extends HtmlResponse {
    private static final Logger LOG = LoggerFactory.getLogger(CreditCardResponse.class);
    private static final Pattern MASKED_CARD_NUMBER_PATTERN = Pattern.compile("^\\*{12}(\\d{4})$");
    private static final Pattern TRANSACTIONS_JSF_SOURCE_PATTERN =
            Pattern.compile(
                    "source:'(movimientos-form:j_id[_0-9a-f]+)',process:'@all',update:'movimientos-form:listaMovimientos");
    private final Map<String, String> cardDetails;

    public CreditCardResponse(String body) {
        super(body);
        cardDetails = parseCardDetails();

        // log unknown values
        getCardType()
                .ifPresent(
                        type -> {
                            if (!CardTypes.ALL.contains(type)) {
                                LOG.warn("Unknown card type: " + type);
                            }
                        });

        if (!CardState.ALL.contains(getCardState())) {
            LOG.warn("Unknown card state: " + getCardState());
        }
    }

    private Map<String, String> parseCardDetails() {
        final NodeList keyNodes =
                evaluateXPath("//div[contains(@class,'head_datos_detalle')]/dl/dt", NodeList.class);

        final HashMap<String, String> dataValues = new HashMap<>();
        for (int i = 0; i < keyNodes.getLength(); i++) {
            final Node keyNode = keyNodes.item(i);
            final Node valueNode = evaluateXPath(keyNode, "following-sibling::dd", Node.class);
            if (!Objects.isNull(keyNode) && !Objects.isNull(valueNode)) {
                dataValues.put(
                        keyNode.getTextContent().trim().toLowerCase(),
                        valueNode.getTextContent().trim());
            }
        }

        return dataValues;
    }

    private Optional<String> getCardType() {
        return Optional.ofNullable(cardDetails.get(CardDetails.TYPE))
                .map(type -> type.toLowerCase());
    }

    private String getCardState() {
        return cardDetails.get(CardDetails.STATE);
    }

    private String getHolderName() {
        return cardDetails.get(CardDetails.HOLDER_NAME);
    }

    public boolean isCreditCard() {
        return getCardType().map(CardTypes.CREDIT::contains).orElse(false);
    }

    public CreditCardAccount toCreditCardAccount(String accountLink) {
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
                                                AccountIdentifierType.PAYMENT_CARD_NUMBER,
                                                cardNumberDigits))
                                .build())
                .addHolderName(getHolderName())
                .setApiIdentifier(Preconditions.checkNotNull(Strings.emptyToNull(accountLink)))
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
                        String.class));
    }

    private ExactCurrencyAmount getAvailableCredit() {
        return parseAmount(
                evaluateXPath(
                        "//div[contains(@class,'saldoDisponible')]//p[contains(@class,'cifra')]",
                        String.class));
    }

    public PaginationKey getFirstPaginationKey() {
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
