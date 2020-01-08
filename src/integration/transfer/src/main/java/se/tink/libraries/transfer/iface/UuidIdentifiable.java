package se.tink.libraries.transfer.iface;

import java.util.UUID;

public interface UuidIdentifiable {

    /** @return A unique UUID that is considered the ID of the instance */
    UUID getId();
}
