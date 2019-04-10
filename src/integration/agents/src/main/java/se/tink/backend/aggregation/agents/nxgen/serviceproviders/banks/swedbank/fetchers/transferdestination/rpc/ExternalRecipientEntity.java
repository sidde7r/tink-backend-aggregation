package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.rpc;

import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.AbstractAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@JsonObject
public class ExternalRecipientEntity extends AbstractAccountEntity implements GeneralAccountEntity {
    private String name;
    private String bank;
    private String id;
    private LinksEntity links;

    @Override
    public String getName() {
        return name;
    }

    public String getBank() {
        return bank;
    }

    public String getId() {
        return id;
    }

    public LinksEntity getLinks() {
        return links;
    }

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        return new SwedishIdentifier(this.fullyFormattedNumber);
    }

    @Override
    public String generalGetBank() {
        return this.bank;
    }

    @Override
    public String generalGetName() {
        return this.name;
    }
}
