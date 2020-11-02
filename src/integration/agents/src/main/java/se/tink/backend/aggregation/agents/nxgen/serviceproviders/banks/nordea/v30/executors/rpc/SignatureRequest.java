package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc;

import java.util.ArrayList;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.entities.SignatureEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SignatureRequest extends ArrayList<SignatureEntity> {}
