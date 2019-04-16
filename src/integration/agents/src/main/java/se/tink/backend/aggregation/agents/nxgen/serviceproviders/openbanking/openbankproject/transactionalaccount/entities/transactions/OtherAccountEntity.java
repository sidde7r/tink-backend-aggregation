package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.transactionalaccount.entities.transactions;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OtherAccountEntity {

    private String id;
    private HolderEntity holder;
    private BankRoutingEntity bankRouting;
    private List<AccountRoutingEntity> accountRoutings;
    private MetadataEntity metadata;

    public String getId() {
        return id;
    }

    public HolderEntity getHolder() {
        return holder;
    }

    public BankRoutingEntity getBankRouting() {
        return bankRouting;
    }

    public List<AccountRoutingEntity> getAccountRoutings() {
        return accountRoutings;
    }

    public MetadataEntity getMetadata() {
        return metadata;
    }
}
