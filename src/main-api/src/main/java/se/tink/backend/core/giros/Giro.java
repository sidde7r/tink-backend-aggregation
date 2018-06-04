package se.tink.backend.core.giros;

import io.protostuff.Tag;
import java.util.Date;
import se.tink.libraries.account.AccountIdentifier;

public class Giro {
    @Tag(1)
    private String accountNumber;
    @Tag(2)
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
