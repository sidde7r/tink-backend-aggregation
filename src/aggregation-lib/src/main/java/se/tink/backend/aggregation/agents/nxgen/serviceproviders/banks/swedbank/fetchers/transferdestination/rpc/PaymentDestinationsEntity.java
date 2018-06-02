package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
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
