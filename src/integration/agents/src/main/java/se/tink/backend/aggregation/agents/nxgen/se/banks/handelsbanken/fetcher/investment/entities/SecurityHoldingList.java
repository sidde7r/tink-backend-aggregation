package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;

@JsonObject
public class SecurityHoldingList {
    private List<SecurityHolding> holdingList;

    public Stream<Optional<InstrumentModule>> toInstrumentModules(HandelsbankenSEApiClient client) {
        if (holdingList == null) {
            return Stream.empty();
        }
        return holdingList.stream().map(holdingList -> holdingList.toInstrumentModule(client));
    }
}
