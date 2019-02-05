package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.stream.Stream;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsData {

    private String audience;
    @JsonProperty("codeGroupeProduitBancaire")
    private String bankProductGroupCode;
    @JsonProperty("caractUrlWP")
    private String characterUrlwp;
    @JsonProperty("isCDD")
    private boolean iscdd;
    @JsonProperty("blocOperations")
    private TransactionGroupEntity block;

    public Stream<TransactionEntity> getTransactions() {
        return block.getElements().stream();
    }

}
