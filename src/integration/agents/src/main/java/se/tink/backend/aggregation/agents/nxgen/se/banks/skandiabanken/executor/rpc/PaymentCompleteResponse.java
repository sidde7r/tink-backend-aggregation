package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.rpc;

import java.util.ArrayList;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.entities.PaymentCompleteEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentCompleteResponse extends ArrayList<PaymentCompleteEntity> {}
