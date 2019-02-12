package se.tink.backend.aggregation.agents.abnamro.client.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.abnamro.client.model.ContractContainer;

public class ContractsResponse extends ErrorResponse {
    private List<ContractContainer> contractList;

    public List<ContractContainer> getContractList() {
        return contractList;
    }

    public void setContractList(List<ContractContainer> contractList) {
        this.contractList = contractList;
    }

    public boolean isError() {
        return getMessages() != null && !getMessages().isEmpty();
    }
}
