package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import javax.annotation.Nonnull;
import lombok.Getter;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.utils.SwedbankSeSerializationUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
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

    @JsonIgnore
    public ExactCurrencyAmount getTinkAmount() {
        return SwedbankSeSerializationUtils.parseAmountForInput(balance, currency);
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

    private boolean isBalanceUndefined() {
        return balance == null || balance.replaceAll("[^0-9]", "").isEmpty();
    }

    @JsonIgnore
    protected Optional<TransactionalAccount> toTransactionalAccount(
            BankProfile bankProfile, @Nonnull AccountTypes type) {
        if (fullyFormattedNumber == null || currency == null || isBalanceUndefined()) {
            return Optional.empty();
        }

        return Optional.of(
                TransactionalAccount.builder(
                                type,
                                fullyFormattedNumber,
                                ExactCurrencyAmount.of(StringUtils.parseAmount(balance), currency))
                        .setAccountNumber(fullyFormattedNumber)
                        .setName(name)
                        .setBankIdentifier(id)
                        .addIdentifier(new SwedishIdentifier(fullyFormattedNumber))
                        .putInTemporaryStorage(StorageKey.NEXT_LINK, getLinkOrNull())
                        .putInTemporaryStorage(
                                SwedbankBaseConstants.StorageKey.PROFILE, bankProfile)
                        .setHolderName(new HolderName(bankProfile.getProfile().getHolderName()))
                        .build());
    }

    private LinkEntity getLinkOrNull() {
        return transactions != null
                ? transactions.getLinks().getNext()
                : Optional.ofNullable(links).map(LinksEntity::getNext).orElse(null);
    }
}
