package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.rpc;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class SearchCriteriaDto {
    private String fromDate;
    private String toDate;
    private int page;
    private String amountType;
    private int transactionsPerPage;
    private String text;
}
