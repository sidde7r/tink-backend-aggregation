package se.tink.backend.core.account;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import java.util.Optional;
import java.util.UUID;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;

@Table(value = "transfer_destination_patterns")
public class TransferDestinationPattern implements Comparable<TransferDestinationPattern> {

    public static final String ALL = ".+";

    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID userId;
    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private UUID accountId;
    @PrimaryKeyColumn(ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private String type;
    @PrimaryKeyColumn(ordinal = 3, type = PrimaryKeyType.CLUSTERED)
    private String pattern;
    private boolean matchesMultiple;
    private String name;
    private String bank;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public AccountIdentifier.Type getType() {
        if (type == null) {
            return null;
        }
        return AccountIdentifier.Type.fromScheme(type);
    }

    public void setType(AccountIdentifier.Type type) {
        if (type == null) {
            this.type = null;
        } else {
            this.type = type.toString();
        }
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public boolean isMatchesMultiple() {
        return matchesMultiple;
    }

    public void setMatchesMultiple(boolean matchesMultiple) {
        this.matchesMultiple = matchesMultiple;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static TransferDestinationPattern createForMultiMatch(AccountIdentifier.Type type, String pattern) {
        TransferDestinationPattern destination = new TransferDestinationPattern();
        destination.setMatchesMultiple(true);
        destination.setType(type);
        destination.setPattern(pattern);
        return destination;
    }

    public static TransferDestinationPattern createForMultiMatchAll(AccountIdentifier.Type type) {
        return createForMultiMatch(type, ALL);
    }

    public static TransferDestinationPattern createForSingleMatch(AccountIdentifier identifier, String name,
            String bank) {

        TransferDestinationPattern destination = new TransferDestinationPattern();
        destination.setMatchesMultiple(false);
        destination.setType(identifier.getType());
        destination.setPattern(identifier.getIdentifier());
        destination.setName(name);
        destination.setBank(bank);

        return destination;
    }

    public static TransferDestinationPattern createForSingleMatch(SwedishIdentifier identifier) {
        return createForSingleMatch(identifier,
                identifier.getName().orElse(identifier.getIdentifier(new DisplayAccountIdentifierFormatter())),
                identifier.getBankName());
    }

    public Optional<AccountIdentifier> getAccountIdentifier() {
        if (isMatchesMultiple()) {
            return Optional.empty();
        }

        return Optional.ofNullable(AccountIdentifier.create(getType(), getPattern(), getName()));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof TransferDestinationPattern)) {
            return false;
        }

        TransferDestinationPattern other = (TransferDestinationPattern) obj;

        return this.compareTo(other) == 0;
    }

    @Override
    public int compareTo(TransferDestinationPattern other) {
        if (other == null) {
            return -1;
        }

        return ComparisonChain.start()
                .compare(this.getUserId(), other.getUserId())
                .compare(this.getAccountId(), other.getAccountId())
                .compare(this.getType().toString(), other.getType().toString())
                .compare(this.getPattern(), other.getPattern())
                .compare(this.getName(), other.getName(), Ordering.natural().nullsFirst())
                .result();
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass()).add("type", type).add("pattern", pattern)
                .add("name", name).add("bank", bank).toString();
    }
}
