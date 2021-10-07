package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.transactionalaccounts.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.VolvoFinansConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities.Answer;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class SavingsAccountEntity {
    @JsonProperty("kontoId")
    private String accountId;

    @JsonProperty("kontonummer")
    private String accountNumber;

    @JsonProperty("produkt")
    private String product;

    @JsonProperty("namn")
    private String name;

    @JsonProperty("saldo")
    private double balance;

    @JsonProperty("rantesats")
    private double interestRate;

    @JsonProperty("kontoRoll")
    private String accountRole;

    @JsonProperty("intressenter")
    private List<AccountHolderEntity> accountHolders;

    private double upplupenRanta;
    private boolean kanOverfora;
    private String bankgiroInbetalning;
    private String ocrnummerInbetalning;

    public Optional<TransactionalAccount> toTinkAccount() {

        final AccountCapabilities capabilities =
                VolvoFinansConstants.ACCOUNT_CAPABILITIES_MAPPER
                        .translate(product)
                        .orElse(
                                new AccountCapabilities(
                                        Answer.UNKNOWN,
                                        Answer.UNKNOWN,
                                        Answer.UNKNOWN,
                                        Answer.UNKNOWN));

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.SAVINGS)
                .withoutFlags()
                .withBalance(buildBalance())
                .withId(buildId())
                .canWithdrawCash(capabilities.getCanWithdrawCash())
                .canPlaceFunds(capabilities.getCanPlaceFunds())
                .canExecuteExternalTransfer(capabilities.getCanExecuteExternalTransfer())
                .canReceiveExternalTransfer(capabilities.getCanReceiveExternalTransfer())
                .addHolderName(getHolderName())
                .setApiIdentifier(accountId)
                .sourceInfo(createAccountSourceInfo())
                .build();
    }

    private AccountSourceInfo createAccountSourceInfo() {
        return AccountSourceInfo.builder().bankProductCode(product).bankProductName(name).build();
    }

    private BalanceModule buildBalance() {
        return BalanceModule.builder()
                .withBalance(ExactCurrencyAmount.inSEK(balance))
                .setInterestRate(interestRate) // interest came as decimal without %
                .build();
    }

    private IdModule buildId() {
        return IdModule.builder()
                .withUniqueIdentifier(accountId)
                .withAccountNumber(accountNumber)
                .withAccountName(name)
                .addIdentifier(AccountIdentifier.create(AccountIdentifierType.TINK, accountNumber))
                .setProductName(product)
                .build();
    }

    @JsonIgnore
    public boolean isAccountHolder() {
        return VolvoFinansConstants.Fetcher.ACCOUNT_ROLE_MAIN_APPLICANT.equalsIgnoreCase(
                accountRole);
    }

    @JsonIgnore
    private String getHolderName() {
        return getAccountHoldersList().stream()
                .filter(
                        holder ->
                                VolvoFinansConstants.Fetcher.ACCOUNT_ROLE_MAIN_APPLICANT
                                        .equalsIgnoreCase(holder.getRole()))
                .findFirst()
                .map(AccountHolderEntity::getName)
                .orElse(null);
    }

    @JsonIgnore
    private List<AccountHolderEntity> getAccountHoldersList() {
        return Optional.ofNullable(accountHolders).orElseGet(Collections::emptyList);
    }

    public String getAccountId() {
        return accountId;
    }
}
