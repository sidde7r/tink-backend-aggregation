package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.interfaces;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.entities.DetailedPermissions;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc.HandelsbankenSEPaymentContext;
import se.tink.backend.aggregation.nxgen.http.URL;

public interface UpdatablePayment {
    DetailedPermissions getDetailedPermissions();
    boolean isChangeAllowed();
    HandelsbankenSEPaymentContext getContext();
    URL toPaymentContext();
    Optional<URL> toUpdate();
}
