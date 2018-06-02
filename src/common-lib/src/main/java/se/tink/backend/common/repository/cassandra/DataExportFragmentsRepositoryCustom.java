package se.tink.backend.common.repository.cassandra;

import java.util.UUID;
import se.tink.backend.core.DataExportFragment;
import se.tink.libraries.cassandra.capabilities.Creatable;

public interface DataExportFragmentsRepositoryCustom extends Creatable {

    DataExportFragment findOneByIdAndIndex(UUID id, int index);

    void deleteByIdAndIndex(UUID id, int index);
}
