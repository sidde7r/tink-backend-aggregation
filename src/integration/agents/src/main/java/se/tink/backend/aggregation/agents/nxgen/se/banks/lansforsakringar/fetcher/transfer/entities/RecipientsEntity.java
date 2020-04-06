package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transfer.entities;

import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarConstants.Accounts;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@JsonObject
public class RecipientsEntity implements GeneralAccountEntity {
    private String giroNumber;
    private String name;
    // `ocrType` is null - cannot define it!

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {

        if (Accounts.PATTERN_BG_RECIPIENT.matcher(giroNumber).matches()) {
            return new BankGiroIdentifier(giroNumber);
        } else if (Accounts.PATTERN_PG_RECIPIENT.matcher(giroNumber).matches()) {
            return new PlusGiroIdentifier(giroNumber);
        } else {
            return new SwedishIdentifier(giroNumber);
        }
    }

    @Override
    public String generalGetBank() {
        return null;
    }

    @Override
    public String generalGetName() {
        return name;
    }
}
