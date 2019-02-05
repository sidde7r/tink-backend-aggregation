package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.PayeeEntity;
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
