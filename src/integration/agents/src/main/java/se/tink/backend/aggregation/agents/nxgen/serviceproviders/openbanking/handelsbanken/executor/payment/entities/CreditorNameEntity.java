package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities;

import com.google.common.base.Strings;
import javax.annotation.Nullable;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditorNameEntity {
    private String name;

    public CreditorNameEntity(String name) {
        this.name = name;
    }

    public static @Nullable CreditorNameEntity of(String name) {
        if (Strings.isNullOrEmpty(name)) {
            return null;
        }
        return new CreditorNameEntity(name);
    }
}
