package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InstrumentDetail {
    private List<InstrumentDetailLine> lines;

    public String toIsIn() {
        return findLine(InstrumentDetailLine::isIn);
    }

    public String toLista() {
        return findLine(InstrumentDetailLine::isLista);
    }

    public String toNamn() {
        return findLine(InstrumentDetailLine::isNamn);
    }

    private String findLine(Predicate<InstrumentDetailLine> with) {
        return getLines().stream()
                .filter(with)
                .map(InstrumentDetailLine::getValue)
                .findFirst()
                .orElse(null);
    }

    private List<InstrumentDetailLine> getLines() {
        return lines == null ? Collections.emptyList() : lines;
    }
}
