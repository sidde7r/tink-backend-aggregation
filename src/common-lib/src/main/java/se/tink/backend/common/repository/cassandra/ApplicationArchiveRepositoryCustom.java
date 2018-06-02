package se.tink.backend.common.repository.cassandra;

import java.util.Optional;
import java.util.List;
import java.util.UUID;
import se.tink.libraries.cassandra.capabilities.Creatable;
import se.tink.backend.core.application.ApplicationArchiveRow;

public interface ApplicationArchiveRepositoryCustom extends Creatable {
    List<ApplicationArchiveRow> findAllByUserId(UUID userId);

    void setToSigned(UUID userId, UUID applicationId, String externalId);
    void setToSignedWithAddedNotes(UUID userId, UUID applicationId, String externalId, String note);

    Optional<ApplicationArchiveRow> findByUserIdAndApplicationId(UUID userId, UUID applicationId);

    /**
     * This method is in the interface just to explicitly in the implementation make a note that we shouldn't do
     * anything since we aren't allowed to delete archived user info.
     */
    void deleteByUserId(String userId);
}
