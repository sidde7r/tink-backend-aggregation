package se.tink.backend.aggregation.provider.configuration.storage.module.clusterprovider;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.HashMap;
import java.util.Set;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderConfigurationStorage;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class AgentCapabilitiesMapModel extends HashMap<String, Set<ProviderConfigurationStorage.Capability>> {

}
