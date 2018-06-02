package se.tink.backend.core.giros;

import io.protostuff.Tag;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;
import se.tink.libraries.account.AccountIdentifier;

import java.util.Date;

@Table(value = "giro_numbers")
public class Giro {
    @Tag(1)
    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String accountNumber;
    @Tag(2)
    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private String giroType;
    @Tag(3)
    private Date created;
    @Tag(4)
    private String name;

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public AccountIdentifier.Type getGiroType() {
        return giroType.equals("se-bg") ? AccountIdentifier.Type.SE_BG : AccountIdentifier.Type.SE_PG;
    }

    public void setGiroType(AccountIdentifier.Type giroType) {
        this.giroType = giroType.toString();
    }

    public static Giro fromIdentifier(AccountIdentifier identifier) throws IllegalArgumentException {
        if (!identifier.getName().isPresent()) {
            throw new IllegalArgumentException("Account identifier name required");
        }

        Giro giro = new Giro();
        giro.setAccountNumber(identifier.getIdentifier());
        giro.setGiroType(identifier.getType());
        giro.setCreated(new Date());
        giro.setName(identifier.getName().get());

        return giro;
    }

    public AccountIdentifier toAccountIdentifier() {
        AccountIdentifier.Type giroType = getGiroType();

        if (giroType != null) {
            AccountIdentifier identifier = AccountIdentifier.create(giroType, getAccountNumber());
            identifier.setName(name);

            return identifier;
        }

        return null;
    }
}
