package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional.entity.CustomerDataItemEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IdentityDataResponse {
    private List<CustomerDataItemEntity> data;

    public String getCustomerName() {
        return String.format("%s %s", data.get(0).getFirstName(), data.get(0).getLastName());
    }
}
