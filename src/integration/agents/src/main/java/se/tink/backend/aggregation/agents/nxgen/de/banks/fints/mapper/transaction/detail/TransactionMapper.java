package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.transaction.detail;

import java.util.List;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public interface TransactionMapper {
    List<AggregationTransaction> parse(String rawTransaction);
}
