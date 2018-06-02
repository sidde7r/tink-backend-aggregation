package se.tink.backend.common.repository.cassandra;

import java.util.List;
import java.util.UUID;
import se.tink.libraries.cassandra.capabilities.Creatable;
import se.tink.backend.core.product.ProductInstance;

public interface ProductInstanceRepositoryCustom  extends Creatable {
    List<ProductInstance> findAllByUserId(UUID userId);
    ProductInstance findByUserIdAndId(UUID userId, UUID id);
}
