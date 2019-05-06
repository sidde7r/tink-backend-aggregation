package se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.authenticator.rpc;

import java.util.ArrayList;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.authenticator.entity.ProviderEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProvidersListRsponse extends ArrayList<ProviderEntity> {}
