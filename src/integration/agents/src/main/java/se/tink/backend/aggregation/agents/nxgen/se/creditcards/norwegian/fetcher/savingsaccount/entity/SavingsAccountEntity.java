package se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.savingsaccount.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.NorwegianConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class SavingsAccountEntity {
    private String accountNumber;
    private String ocrNumber;
    private double amountAvailable;
    private BigDecimal balance;
    private double interestAccumulated;
    private String payToAccountNumber;
    private String nickName;
    private String region;
    private boolean showNickName;
    private boolean isPreferredPaymentAccountNumber;

    @JsonIgnore
    public TransactionalAccount toTinkAccount() {

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.SAVINGS)
                .withPaymentAccountFlag()
                .withBalance(
                        BalanceModule.of(
                                ExactCurrencyAmount.of(balance, NorwegianConstants.CURRENCY)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(NorwegianConstants.SAVINGS_IDENTIFIER)
                                .withAccountNumber(accountNumber)
                                .withAccountName(NorwegianConstants.SAVINGS_ALIAS)
                                .addIdentifier(new SwedishIdentifier(accountNumber))
                                .build())
                .build()
                .orElse(null);
    }
}
