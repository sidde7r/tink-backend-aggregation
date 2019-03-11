package se.tink.backend.aggregation.agents.banks.norwegian.model;

import java.util.ArrayList;
import java.util.stream.Stream;

public class TransactionListResponse extends ArrayList<TransactionEntity> {

    @Override
    public Stream<TransactionEntity> stream() {
        return super.stream();
    }
}
