package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities;

import java.util.List;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class FilterEntity {
    private AmountFilterEntity amounts;
    private DateFilterEntity dates;
    private List<String> operationType;
}
