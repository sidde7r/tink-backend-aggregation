package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.rpc;

import com.google.common.collect.ImmutableList;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.time.annotations.KnabDateJsonFormat;
import se.tink.backend.aggregation.annotations.JsonObject;

@Builder
@JsonObject
public class ConsentRequest {

    @Builder.Default
    private List<String> access = ImmutableList.of("accounts", "transactions", "balances");

    @KnabDateJsonFormat private LocalDate validUntil;

    private Integer frequencyPerDay;

    private Boolean recurringIndicator;

    private Boolean combinedServiceIndicator;
}
