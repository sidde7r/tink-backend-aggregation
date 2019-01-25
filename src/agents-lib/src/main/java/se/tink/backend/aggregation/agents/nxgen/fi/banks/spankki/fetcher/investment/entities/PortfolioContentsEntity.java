package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.system.rpc.Instrument;

@JsonObject
public class PortfolioContentsEntity {
private String categoryId;
private String categoryName;
private List<PortfolioContentDetails> items;

    @JsonIgnore
    public List<Instrument> getTinkInstruments(Map<String, String> fundIdIsinMapper) {
        if (items == null) {
            return Collections.EMPTY_LIST;
        }

        return items.stream()
                .map(content -> content.toTinkInstrument(fundIdIsinMapper))
                .collect(Collectors.toList());
    }

    public List<PortfolioContentDetails> getItems() {
        return items;
    }
}
