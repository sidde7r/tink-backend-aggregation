package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class SelectedRole {
    private String id;
    private String name;
}
