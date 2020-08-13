package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@JsonObject
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountHistoryParameters {

    private PagingCriteria pagingCriteria;

    private String nextOrPreviousSetOfTransactionsIndicator = "";

    public AccountHistoryParameters(PagingCriteria pagingCriteria) {
        this.pagingCriteria = pagingCriteria;
    }
}
