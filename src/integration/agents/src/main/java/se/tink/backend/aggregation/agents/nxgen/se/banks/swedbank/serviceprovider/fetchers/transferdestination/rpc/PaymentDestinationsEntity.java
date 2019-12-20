package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transferdestination.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.PayeeEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentDestinationsEntity {
    private List<PayeeEntity> payees;
    private LinksEntity links;

    public List<PayeeEntity> getPayees() {
        return payees;
    }

    public LinksEntity getLinks() {
        return links;
    }
}
