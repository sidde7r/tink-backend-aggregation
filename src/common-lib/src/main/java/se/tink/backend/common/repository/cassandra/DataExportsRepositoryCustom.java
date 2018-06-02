package se.tink.backend.common.repository.cassandra;

import java.util.UUID;
import se.tink.backend.core.DataExport;
import se.tink.libraries.cassandra.capabilities.Creatable;

public interface DataExportsRepositoryCustom extends Creatable {

    DataExport findOneByUserIdAndId(UUID userId, UUID id);
}
