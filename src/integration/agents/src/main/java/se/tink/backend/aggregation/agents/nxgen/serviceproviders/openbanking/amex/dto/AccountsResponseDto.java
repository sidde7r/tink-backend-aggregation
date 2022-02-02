package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmericanExpressConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmericanExpressUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.identifiers.MaskedPanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
@JsonObject
@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AccountsResponseDto {

    private IdentifiersDto identifiers;
    private HolderDto holder;
    private ProductDto product;
    private StatusDto status;
    private List<SupplementaryAccountsItem> supplementaryAccounts;

    public CreditCardAccount toCreditCardAccount(
            List<BalanceDto> balances, StatementPeriodsDto statementPeriods, String currencyCode) {
        if (Strings.isNullOrEmpty(identifiers.getAccountKey())) {
            log.warn("Account Key missing!");
        }

        final String pan = identifiers.getDisplayAccountNumber();
        final String uniqueId =
                AmericanExpressUtils.formatAccountId(identifiers.getDisplayAccountNumber());
        final String cardName =
                product.getDigitalInfo().getProductDesc() + " - " + uniqueId.substring(4);

        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(pan)
                                .withBalance(getBalance(balances, currencyCode))
                                .withAvailableCredit(
                                        new ExactCurrencyAmount(new BigDecimal(0), currencyCode))
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
                .setHolderType(product.getHolderType())
                .putInTemporaryStorage(
                        AmericanExpressConstants.StorageKey.STATEMENTS,
                        getStatementMap(statementPeriods))
                .build();
    }

    public List<CreditCardAccount> toSubCreditCardAccount(
            StatementPeriodsDto statementPeriods, List<BalanceDto> balanceDtos) {

        String currencyCode =
                balanceDtos.stream()
                        .findFirst()
                        .map(BalanceDto::getIsoAlphaCurrencyCode)
                        .orElse(holder.getCurrencyCode());

        return supplementaryAccounts.stream()
                .filter(Objects::nonNull)
                .map(t -> t.toCreditCardAccount(getStatementMap(statementPeriods), currencyCode))
                .collect(Collectors.toList());
    }

    private ExactCurrencyAmount getBalance(List<BalanceDto> balances, String currencyCode) {
        if (!identifiers.isBasic()) {
            // supplementary card has no balance
            return new ExactCurrencyAmount(new BigDecimal(0), currencyCode);
        }

        return balances.stream()
                .findFirst()
                .map(AccountsResponseDto::convertBalanceEntityToExactCurrencyAmount)
                .orElseThrow(() -> new IllegalStateException("No balance found"));
    }

    private static ExactCurrencyAmount convertBalanceEntityToExactCurrencyAmount(
            BalanceDto balanceDto) {
        return new ExactCurrencyAmount(
                        balanceDto.getStatementBalanceAmount(),
                        balanceDto.getIsoAlphaCurrencyCode())
                .negate();
    }

    private Map<Integer, String> getStatementMap(StatementPeriodsDto statementPeriods) {
        return statementPeriods.getStatementPeriods().stream()
                .collect(
                        Collectors.toMap(StatementDto::getIndex, StatementDto::getEndDateAsString));
    }

    public boolean haveSupplementaryAccounts() {
        return supplementaryAccounts != null;
    }
}
