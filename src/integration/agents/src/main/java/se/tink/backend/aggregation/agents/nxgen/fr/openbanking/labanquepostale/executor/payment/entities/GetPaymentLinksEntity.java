package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.entities;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetPaymentLinksEntity {

    private Href self;
    private Href confirmation;
}
