package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.einvoice.entities;

import com.google.common.base.MoreObjects;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EmptyBodyEntity {

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).toString();
    }

}
