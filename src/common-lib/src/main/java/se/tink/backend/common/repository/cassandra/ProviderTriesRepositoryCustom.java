package se.tink.backend.common.repository.cassandra;

import se.tink.libraries.cassandra.capabilities.Creatable;

public interface ProviderTriesRepositoryCustom extends Creatable {
    void truncate();
}
