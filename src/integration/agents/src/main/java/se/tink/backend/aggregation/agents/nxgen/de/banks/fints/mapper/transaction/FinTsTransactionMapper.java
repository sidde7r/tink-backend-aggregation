package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.transaction;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.transaction.detail.DefaultCamtTransactionMapper;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.transaction.detail.DefaultSwiftTransactionMapper;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.transaction.detail.TransactionMapper;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class FinTsTransactionMapper {

    private static final TransactionMapper DEFAULT_CAMT_MAPPER = new DefaultCamtTransactionMapper();
    private static final TransactionMapper DEFAULT_SWIFT_MAPPER =
            new DefaultSwiftTransactionMapper();

    public List<AggregationTransaction> parseSwift(String rawMT940) {
        return DEFAULT_SWIFT_MAPPER.parse(rawMT940);
    }

    public List<AggregationTransaction> parseCamt(String rawXml) {
        return DEFAULT_CAMT_MAPPER.parse(rawXml);
    }
}
