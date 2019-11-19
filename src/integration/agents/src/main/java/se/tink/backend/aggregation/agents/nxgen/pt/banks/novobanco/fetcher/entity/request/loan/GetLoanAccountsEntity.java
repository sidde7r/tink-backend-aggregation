package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.request.loan;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetLoanAccountsEntity {
    public GetLoanAccountsEntity(int typeSection, String accountId) {
        this.typeSection = typeSection;
        this.accountId = accountId;
    }

    @JsonProperty("TipoSeccao")
    private int typeSection;

    @JsonProperty("Conta")
    private String accountId;
}
