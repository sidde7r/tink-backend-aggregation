package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.upcomingtransaction.rpc;

import java.util.ArrayList;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.upcomingtransaction.entities.UpcomingPaymentEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchPaymentsResponse extends ArrayList<UpcomingPaymentEntity> {}
