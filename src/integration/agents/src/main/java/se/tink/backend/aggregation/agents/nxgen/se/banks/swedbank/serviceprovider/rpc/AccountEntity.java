package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import javax.annotation.Nonnull;
import lombok.Getter;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.StorageKey;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities.Answer;
import se.tink.backend.aggregation.nxgen.core.account.entity.Holder;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

@Getter
@JsonObject
public abstract class AccountEntity extends AbstractAccountEntity {
    protected boolean selectedForQuickbalance;
    protected LinksEntity links;

    protected String priority;
    protected String currency;
    protected DetailsEntity details;
    protected String balance;
    protected boolean availableForFavouriteAccount;
    protected boolean availableForPriorityAccount;
    protected String type;
    protected String productId;
    protected TransactionsEntity transactions;

    public boolean isInvestmentAccount() {
        return SwedbankBaseConstants.ACCOUNT_TYPE_MAPPER
                .translate(productId)
                .orElse(AccountTypes.OTHER)
                .equals(AccountTypes.INVESTMENT);
    }

    private boolean isBalanceUndefined() {
        return balance == null || balance.replaceAll("[^0-9]", "").isEmpty();
    }

    public boolean isAvailableForFavouriteAccount() {
        return availableForFavouriteAccount;
    }

    public boolean isAvailableForPriorityAccount() {
        return availableForPriorityAccount;
    }

    public String getType() {
        return type;
    }

    @JsonIgnore
    protected Optional<TransactionalAccount> toTransactionalAccount(
            BankProfile bankProfile,
            @Nonnull AccountTypes defaultType,
            EngagementTransactionsResponse engagementTransactionsResponse) {
        if (fullyFormattedNumber == null || currency == null || isBalanceUndefined()) {
            return Optional.empty();
        }

        final AccountCapabilities capabilities =
                SwedbankBaseConstants.ACCOUNT_CAPABILITIES_MAPPER
                        .translate(productId)
                        .orElse(
                                new AccountCapabilities(
                                        Answer.UNKNOWN,
                                        Answer.UNKNOWN,
                                        Answer.UNKNOWN,
                                        Answer.UNKNOWN));
        String creditLimit = "0.0";

        if (engagementTransactionsResponse != null) {
            creditLimit = engagementTransactionsResponse.getAccount().getCreditGranted();
        }
        return TransactionalAccount.nxBuilder()
                .withType(getTinkAccountType(defaultType))
                .withInferredAccountFlags()
                .withBalance(buildBalanceModule(creditLimit))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(id)
                                .withAccountNumber(fullyFormattedNumber)
                                .withAccountName(name)
                                .addIdentifier(new SwedishIdentifier(fullyFormattedNumber))
                                .build())
                .canWithdrawCash(capabilities.getCanWithdrawCash())
                .canPlaceFunds(capabilities.getCanPlaceFunds())
                .canExecuteExternalTransfer(capabilities.getCanExecuteExternalTransfer())
                .canReceiveExternalTransfer(capabilities.getCanReceiveExternalTransfer())
                .putInTemporaryStorage(StorageKey.NEXT_LINK, getLinkOrNull())
                .putInTemporaryStorage(SwedbankBaseConstants.StorageKey.PROFILE, bankProfile)
                .addHolders(Holder.of(bankProfile.getProfile().getHolderName()))
                .build();
    }

    private BalanceModule buildBalanceModule(String creditLimit) {

        return BalanceModule.builder()
                .withBalance(
                        ExactCurrencyAmount.of(
                                StringUtils.parseAmount(balance)
                                        - StringUtils.parseAmount(creditLimit),
                                currency))
                .setAvailableBalance(
                        ExactCurrencyAmount.of(StringUtils.parseAmount(balance), currency))
                .setCreditLimit(
                        ExactCurrencyAmount.of(StringUtils.parseAmount(creditLimit), currency))
                .build();
    }

    private TransactionalAccountType getTinkAccountType(AccountTypes defaultType) {
        return TransactionalAccountType.from(
                        SwedbankBaseConstants.ACCOUNT_TYPE_MAPPER
                                .translate(productId)
                                .orElse(defaultType))
                .orElse(TransactionalAccountType.OTHER);
    }

    public LinkEntity getLinkOrNull() {
        return transactions != null
                ? transactions.getLinks().getNext()
                : Optional.ofNullable(links).map(LinksEntity::getNext).orElse(null);
    }
}
