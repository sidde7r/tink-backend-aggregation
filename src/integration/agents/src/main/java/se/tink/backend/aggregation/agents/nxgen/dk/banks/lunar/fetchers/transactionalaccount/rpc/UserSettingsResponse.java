package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities.SettingsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class UserSettingsResponse {
    private SettingsEntity settings;
}
