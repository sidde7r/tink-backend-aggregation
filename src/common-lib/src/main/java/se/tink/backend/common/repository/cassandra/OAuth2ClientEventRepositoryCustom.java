package se.tink.backend.common.repository.cassandra;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import se.tink.libraries.cassandra.capabilities.Creatable;
import se.tink.backend.core.OAuth2ClientEvent;

public interface OAuth2ClientEventRepositoryCustom extends Creatable {

    List<OAuth2ClientEvent> findAllByClientIdAndDateBetween(UUID clientId, Date min, Date max);

}
