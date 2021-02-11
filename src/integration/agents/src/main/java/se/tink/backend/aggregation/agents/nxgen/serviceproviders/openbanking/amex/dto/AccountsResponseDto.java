package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmericanExpressConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmericanExpressUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

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
            List<BalanceDto> balances, Map<Integer, String> statementMap) {
        final String iban =
                identifiers
                        .getDisplayAccountNumber()
                        .substring(identifiers.getDisplayAccountNumber().length() - 9);
        final String cardName =
                product.getDigitalInfo().getProductDesc() + " - " + iban.substring(4);
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(iban)
                                .withBalance(getBalance(balances))
                                .withAvailableCredit(
                                        new ExactCurrencyAmount(
                                                new BigDecimal(0),
                                                balances.stream()
                                                        .findFirst()
                                                        .get()
                                                        .getIsoAlphaCurrencyCode()))
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
                .putInTemporaryStorage(AmericanExpressConstants.StorageKey.STATEMENTS, statementMap)
                .build();
    }

    public List<CreditCardAccount> toSubCreditCardAccount() {
        return supplementaryAccounts.stream()
                .filter(Objects::nonNull)
                .map(t -> t.toCreditCardAccount(AmericanExpressUtils.createEmptyAmount()))
                .collect(Collectors.toList());
    }

    private static ExactCurrencyAmount getBalance(List<BalanceDto> balances) {
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

    public boolean haveSupplementaryAccounts() {
        return supplementaryAccounts != null;
    }
}
