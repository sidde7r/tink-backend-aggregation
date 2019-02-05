package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransferDestinationsEntity {
    private List<String> periodicities;
    private List<ExternalRecipientEntity> externalRecipients;
    private LinksEntity links;

    public List<String> getPeriodicities() {
        return periodicities;
    }

    public List<ExternalRecipientEntity> getExternalRecipients() {
        return externalRecipients;
    }

    public LinksEntity getLinks() {
        return links;
    }
}
