package se.tink.backend.common.repository.cassandra;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import se.tink.libraries.cassandra.capabilities.Creatable;
import se.tink.backend.core.CompressedDocument;

public interface DocumentRepositoryCustom extends Creatable {

    Optional<CompressedDocument> findOneByUserIdAndIdentifier(UUID userId, String identifier);

    CompressedDocument findOneByUserIdAndToken(UUID userId, UUID token);

    List<CompressedDocument> findAllByUserId(UUID userId);

    void deleteByUserId(String userId);

}
