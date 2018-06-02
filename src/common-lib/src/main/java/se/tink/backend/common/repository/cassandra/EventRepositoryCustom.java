package se.tink.backend.common.repository.cassandra;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import se.tink.libraries.cassandra.capabilities.Creatable;
import se.tink.backend.core.Event;


public interface EventRepositoryCustom extends Creatable {
    List<Event> findLastByUserId(UUID userId, int limit);
    
    List<Event> findUserEventsAfter(UUID userId, Date date);
}
