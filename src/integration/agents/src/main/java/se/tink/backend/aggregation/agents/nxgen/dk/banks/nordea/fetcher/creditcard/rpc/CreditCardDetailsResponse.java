package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.exception.UnsupportedCurrencyException;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CreditCardDetailsResponse {

    @JsonIgnore
    private static final Logger log = LoggerFactory.getLogger(CreditCardDetailsResponse.class);

    @JsonIgnore
    private static final String EXPECTED_MASKED_CARD_NUMBER_FORMAT =
            "\\d{4}\\*{8}(\\d{4})"; // 1234xxxxxxxx5678

    @JsonIgnore
    private static final Pattern EXPECTED_MASKED_CARD_NUMER_PATTERN =
            Pattern.compile(EXPECTED_MASKED_CARD_NUMBER_FORMAT);

    @JsonIgnore private static final String ALL_WHITESPACES = "\\s+";

    @JsonProperty("credit")
    private CreditDetails creditDetails;

    private String currency;
    private String nickname;
    private String cardId;

    public CreditCardAccount toTinkAccount() {
        CreditCardModule cardModule =
                CreditCardModule.builder()
                        .withCardNumber(creditDetails.creditCardNumber)
                        .withBalance(balance())
                        .withAvailableCredit(availableCredit())
                        .withCardAlias(alias())
                        .build();
        IdModule idModule =
                IdModule.builder()
                        .withUniqueIdentifier(id())
                        .withAccountNumber(creditDetails.creditCardNumber)
                        .withAccountName(alias())
                        .addIdentifier(
                                AccountIdentifier.create(
                                        AccountIdentifierType.PAYMENT_CARD_NUMBER,
                                        creditDetails.creditCardNumber))
                        .build();
        return CreditCardAccount.nxBuilder()
                .withCardDetails(cardModule)
                .withInferredAccountFlags()
                .withId(idModule)
                // Withdraw funds at ATM
                .canWithdrawCash(AccountCapabilities.Answer.YES)
                // The `permissions` object does not contain any useful information for these
                // fields.
                // Setting to UNKNOWN for the time being
                .canPlaceFunds(AccountCapabilities.Answer.UNKNOWN)
                .canReceiveExternalTransfer(AccountCapabilities.Answer.UNKNOWN)
                .canExecuteExternalTransfer(AccountCapabilities.Answer.UNKNOWN)
                .setApiIdentifier(cardId)
                .build();
    }

    @JsonIgnore
    private ExactCurrencyAmount balance() {
        if (!NordeaDkConstants.CURRENCY.equals(currency)) {
            throw new UnsupportedCurrencyException(
                    "Cannot map credit card account, wrong currency");
        }
        return ExactCurrencyAmount.of(creditDetails.creditCardBalance, NordeaDkConstants.CURRENCY);
    }

    @JsonIgnore
    private ExactCurrencyAmount availableCredit() {
        if (!NordeaDkConstants.CURRENCY.equals(currency)) {
            throw new UnsupportedCurrencyException(
                    "Cannot map credit card account, wrong currency");
        }
        return ExactCurrencyAmount.of(creditDetails.availableCredit, NordeaDkConstants.CURRENCY);
    }

    @JsonIgnore
    private String id() {
        String maskedNumber = creditDetails.creditCardNumber.replaceAll(ALL_WHITESPACES, "");
        Matcher matcher = EXPECTED_MASKED_CARD_NUMER_PATTERN.matcher(maskedNumber);
        if (matcher.matches()) { // 1234********5678
            return matcher.group(1); // 5678
        }
        log.warn(
                "Credit card number {} does not match pattern \"1234 **** **** 5678\", unable to map to legacy format",
                maskedNumber);
        return maskedNumber;
    }

    @JsonIgnore
    private String alias() {
        return nickname != null ? nickname : creditDetails.creditCardNumber;
    }

    @JsonObject
    private static class CreditDetails {

        @JsonProperty("masked_credit_card_number")
        private String creditCardNumber;

        @JsonProperty("credit_booked_balance")
        private double creditCardBalance;

        @JsonProperty("credit_available_balance")
        private double availableCredit;
    }
}
