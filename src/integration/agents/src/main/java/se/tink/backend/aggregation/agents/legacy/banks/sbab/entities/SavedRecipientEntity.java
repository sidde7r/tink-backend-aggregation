package se.tink.backend.aggregation.agents.banks.sbab.entities;

import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@JsonObject
public class SavedRecipientEntity implements GeneralAccountEntity {
    private int id;
    private String name;
    private String accountNumber;
    private String bankName;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getBankName() {
        return bankName;
    }

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        return new SwedishIdentifier(accountNumber);
    }

    @Override
    public String generalGetBank() {
        return bankName;
    }

    @Override
    public String generalGetName() {
        return name;
    }
}
