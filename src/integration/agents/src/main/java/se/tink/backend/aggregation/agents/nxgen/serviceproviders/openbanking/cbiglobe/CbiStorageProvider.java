package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;

@AllArgsConstructor
@Getter
public class CbiStorageProvider {

    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private final TemporaryStorage temporaryStorage;
}
