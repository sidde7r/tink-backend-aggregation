package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.investment.entities;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.agents.models.Instrument;

@JsonObject
public class SecurityGroupEntity {
    private String type;
    private Double marketValue;
    private Double percent;
    private List<SecuritiesEntity> securities;

    public String getType() {
        return type;
    }

    public Double getMarketValue() {
        return marketValue;
    }

    public Double getPercent() {
        return percent;
    }

    public List<SecuritiesEntity> getSecurities() {
        return securities;
    }

    Stream<Optional<Instrument>> getInstruments() {
        if (securities == null) {
            return Stream.empty();
        }
        return securities.stream()
                .map(SecuritiesEntity::toTinkInstrument);
    }
}
