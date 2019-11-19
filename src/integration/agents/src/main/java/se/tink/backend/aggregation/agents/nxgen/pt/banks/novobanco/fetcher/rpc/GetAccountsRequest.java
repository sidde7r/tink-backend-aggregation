package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.request.HeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.request.AccountRequestEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetAccountsRequest {
    public GetAccountsRequest(HeaderEntity header) {
        this.header = header;
    }

    public GetAccountsRequest(HeaderEntity header, AccountRequestEntity accountRequest) {
        this.header = header;
        this.accountRequest = accountRequest;
    }

    @JsonProperty("Header")
    private HeaderEntity header;

    @JsonProperty("Body")
    @JsonInclude(NON_NULL)
    private AccountRequestEntity accountRequest;
}
