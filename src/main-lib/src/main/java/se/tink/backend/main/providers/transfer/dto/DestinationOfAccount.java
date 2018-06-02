package se.tink.backend.main.providers.transfer.dto;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.core.Account;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.ImageUrls;
import se.tink.libraries.account.identifiers.GiroIdentifier;
import se.tink.libraries.account.identifiers.TinkIdentifier;
import se.tink.backend.utils.ProviderImageMap;

public class DestinationOfAccount extends Destination {
    private static class TinkIdentifierTransform implements Function<AccountIdentifier, AccountIdentifier> {
        private final String accountId;

        private TinkIdentifierTransform(final String accountId) {
            this.accountId = accountId;
        }

        @Override
        public AccountIdentifier apply(AccountIdentifier identifier) {
            switch (identifier.getType()) {
            case SE_BG:
            case SE_PG:
                return transform(identifier.to(GiroIdentifier.class));
            default:
                return identifier;
            }
        }

        private AccountIdentifier transform(GiroIdentifier giroIdentifier) {
            return giroIdentifier.getOcr().isPresent() ? new TinkIdentifier(accountId) : giroIdentifier;
        }
    }

    private static final Predicate<AccountIdentifier> TINK_IDENTIFIERS_ONLY = accountIdentifier -> accountIdentifier
            .is(AccountIdentifier.Type.TINK);

    public static final Function<Account, Destination> ACCOUNT_TO_DESTINATION =
            account -> {
                final List<AccountIdentifier> identifiers = account.getIdentifiers();
                for (AccountIdentifier identifier : identifiers) {
                    identifier.setName(account.getName());
                }

                DestinationOfAccount destination = new DestinationOfAccount(identifiers);

                destination.setAccountId(account.getId());
                destination.setBalance(account.getBalance());
                destination.setCredentialsId(account.getCredentialsId());
                destination.setName(account.getName());
                destination.setType(account.getType());

                return destination;
            };

    private String accountId;
    private Double balance;
    private String credentialsId;
    private AccountTypes type;

    public DestinationOfAccount(List<AccountIdentifier> identifiers) {
        super(identifiers);
    }

    public Optional<String> getAccountId() {
        return Optional.ofNullable(accountId);
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public AccountTypes getType() {
        return type;
    }

    public void setType(AccountTypes type) {
        this.type = type;
    }

    public ImageUrls getImageUrls(Map<String, String> providerNameByCredentialsId, ProviderImageMap providerImages) {
        String providerName = providerNameByCredentialsId.get(getCredentialsId());
        return providerImages.getImagesForAccount(providerName, getType());
    }

    @Override
    public Destination copyOf() {
        final DestinationOfAccount destination = new DestinationOfAccount(
                Lists.newArrayList(this.getIdentifiers()));

        destination.setName(this.getName().orElse(null));

        destination.setAccountId(this.accountId);
        destination.setBalance(this.balance);
        destination.setCredentialsId(this.credentialsId);
        destination.setType(this.type);

        return destination;
    }

    @Override
    public Optional<AccountIdentifier> getPrimaryIdentifier() {
        final List<AccountIdentifier> identifiers = getIdentifiers();

        Optional<AccountIdentifier> tinkIdentifier = identifiers.stream()
                .map(new TinkIdentifierTransform(accountId)::apply)
                .filter(TINK_IDENTIFIERS_ONLY::apply).findFirst();

        if (tinkIdentifier.isPresent()) {
            return tinkIdentifier;
        } else {
            return getFirstIdentifier();
        }
    }

    /**
     * TODO: We need to figure how we get primary identifier when there are multiple identifiers (as here)
     * For now we can live with only the swedish identifier as primary if it exists (otherwise just take the first
     * that comes up) since we only implement transfers for Sweden now.
     */
    @Override
    public Optional<AccountIdentifier> getDisplayIdentifier() {
        List<AccountIdentifier> identifiers = getIdentifiers();

        Optional<AccountIdentifier> swedishIdentifier = identifiers.stream()
                .filter(accountIdentifier -> accountIdentifier.is(AccountIdentifier.Type.SE))
                .findFirst();

        if (swedishIdentifier.isPresent()) {
            return swedishIdentifier;
        } else {
            Optional<AccountIdentifier> primaryIdentifier = getPrimaryIdentifier();

            if (primaryIdentifier.isPresent() && primaryIdentifier.get().is(AccountIdentifier.Type.TINK)) {
                return getFirstIdentifier();
            }

            return primaryIdentifier;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }

        DestinationOfAccount that = (DestinationOfAccount) o;

        if (balance != null ? !balance.equals(that.balance) : that.balance != null) {
            return false;
        }
        if (credentialsId != null ? !credentialsId.equals(that.credentialsId) : that.credentialsId != null) {
            return false;
        }
        return type == that.type;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (balance != null ? balance.hashCode() : 0);
        result = 31 * result + (credentialsId != null ? credentialsId.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return super.toStringHelper(this.getClass())
                .add("balance", balance)
                .add("credentialsId", credentialsId)
                .add("type", type)
                .toString();
    }
}
