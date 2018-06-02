package se.tink.backend.core.account;

import com.google.common.collect.ComparisonChain;
import java.util.UUID;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;
import se.tink.libraries.account.AccountIdentifier;

@Table(value = "users_transfer_destinations")
public class UserTransferDestination implements Comparable<UserTransferDestination> {
    @PrimaryKeyColumn(ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private String identifier;
    private String name;
    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private String type;
    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID userId;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public AccountIdentifier getAccountIdentifier() {
        return AccountIdentifier.create(getType(), getIdentifier(), getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof UserTransferDestination)) {
            return false;
        }

        UserTransferDestination other = (UserTransferDestination)obj;

        return this.compareTo(other) == 0;
    }

    @Override
    public int compareTo(UserTransferDestination other) {
        if (other == null) {
            return -1;
        }

        return ComparisonChain.start()
                .compare(this.getUserId(), other.getUserId())
                .compare(this.getType().toString(), other.getType().toString())
                .compare(this.getIdentifier(), other.getIdentifier())
                .result();
    }
}
