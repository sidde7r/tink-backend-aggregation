package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public abstract class AbstractExecutorTransactionEntity {
    protected LinksEntity links;
    protected String id;
    protected String type;
    protected String currencyCode;
    protected boolean selected;
    protected String amount;
    protected TransferEntity transfer;
}
