package se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.entities;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionData {
    private List<Transaction> transaction;
}
