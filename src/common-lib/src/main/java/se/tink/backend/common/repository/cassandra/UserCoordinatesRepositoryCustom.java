package se.tink.backend.common.repository.cassandra;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import se.tink.libraries.cassandra.capabilities.Creatable;
import se.tink.backend.core.UserCoordinates;

public interface UserCoordinatesRepositoryCustom extends Creatable {

    UserCoordinates findOneByUserId(UUID userId);

    ConcurrentHashMap<String, UserCoordinates> findAllCoordinatesByAddress();

}
