package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SdcCustodyContentGroup {
    private List<SdcCustodyAsset> depositAssets;
    private String securityType;
    private String securityTypeLocalized;

    public Stream<Instrument> toInstruments() {
        if (depositAssets == null) {
            return Stream.empty();
        }
        return depositAssets.stream()
                .map(SdcCustodyAsset::toInstrument)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }
}
