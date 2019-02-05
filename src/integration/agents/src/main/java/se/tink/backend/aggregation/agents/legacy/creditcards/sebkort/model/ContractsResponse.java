package se.tink.backend.aggregation.agents.creditcards.sebkort.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractsResponse extends BaseResponse {
    protected List<ContractEntity> body;

    public List<ContractEntity> getBody() {
        return body;
    }

    public void setBody(List<ContractEntity> body) {
        this.body = body;
    }
}
