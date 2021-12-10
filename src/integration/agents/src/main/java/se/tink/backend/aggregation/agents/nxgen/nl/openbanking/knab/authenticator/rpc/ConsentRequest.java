package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.rpc;

import com.google.common.collect.ImmutableList;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.time.annotations.KnabDateJsonFormat;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ConsentRequest {

    private final List<String> access = ImmutableList.of("accounts", "transactions", "balances");

    @KnabDateJsonFormat private final LocalDate validUntil;

    private final Integer frequencyPerDay;

    private final Boolean recurringIndicator;

    private final Boolean combinedServiceIndicator;
}
