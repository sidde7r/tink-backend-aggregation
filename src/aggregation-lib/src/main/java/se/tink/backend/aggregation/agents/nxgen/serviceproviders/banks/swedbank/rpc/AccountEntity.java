package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import java.util.Optional;
import javax.annotation.Nonnull;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@JsonObject
public abstract class AccountEntity extends AbstractAccountEntity {
    protected boolean selectedForQuickbalance;
    protected LinksEntity links;
    protected String priority;
    protected String id;
    protected String currency;
    protected DetailsEntity details;
    protected String balance;
    protected boolean availableForFavouriteAccount;
    protected boolean availableForPriorityAccount;
    protected String type;

    public boolean isSelectedForQuickbalance() {
        return selectedForQuickbalance;
    }

    public LinksEntity getLinks() {
        return links;
    }

    public String getPriority() {
        return priority;
    }

    public String getId() {
        return id;
    }

    public String getCurrency() {
        return currency;
    }

    public DetailsEntity getDetails() {
        return details;
    }

    public String getBalance() {
        return balance;
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

    protected Optional<TransactionalAccount> toTransactionalAccount(@Nonnull AccountTypes type) {
        if (fullyFormattedNumber == null || currency == null || balance == null) {
            return Optional.empty();
        }

        return Optional.of(
                TransactionalAccount.builder(type, fullyFormattedNumber,
                        new Amount(currency, StringUtils.parseAmount(balance)))
                        .setName(name)
                        .setBankIdentifier(id)
                        .setUniqueIdentifier(fullyFormattedNumber)
                        .addIdentifier(new SwedishIdentifier(fullyFormattedNumber))
                        .addToTemporaryStorage(SwedbankBaseConstants.StorageKey.NEXT_LINK,
                                links != null ? links.getNext() : null)
                        .build());
    }
}
