package se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.fetcher.rpc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.fetcher.rpc.entities.PositionEntity;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;

public class PositionsResponse extends ArrayList<PositionEntity> {

    public List<InstrumentModule> toInstruments() {

        return Optional.of(
                        this.stream()
                                .map(PositionEntity::toTinkInstrument)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }
}
