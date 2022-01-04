package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.fetcher.rpc;

import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.fetcher.entity.NickelOperation;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class NickelOperationList {

    private List<NickelOperation> operations;
}
