package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEApiClient;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;

@JsonObject
public class PlacementEntity {
    private String type;
    private String name;
    private List<SubPlacementEntity> subPlacements;

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public List<SubPlacementEntity> getSubPlacements() {
        return subPlacements;
    }

    @JsonIgnore
    private List<SubPlacementEntity> getSubPlacementsOrEmptyList() {
        return Optional.ofNullable(subPlacements).orElseGet(Collections::emptyList);
    }

    public List<Instrument> toTinkFundInstruments(SwedbankSEApiClient apiClient) {
        return getSubPlacementsOrEmptyList().stream()
                .map(subPlacementEntity -> subPlacementEntity.toTinkFundInstruments(apiClient))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public List<Instrument> toTinkInstruments(SwedbankSEApiClient apiClient) {
        return getSubPlacementsOrEmptyList().stream()
                .map(subPlacementEntity -> subPlacementEntity.toTinkInstruments(apiClient))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public List<InstrumentModule> toTinkPensionInstruments(SwedbankSEApiClient apiClient) {
        return subPlacements.stream()
                .map(subPlacementEntity -> subPlacementEntity.toTinkPensionInstruments(apiClient))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
