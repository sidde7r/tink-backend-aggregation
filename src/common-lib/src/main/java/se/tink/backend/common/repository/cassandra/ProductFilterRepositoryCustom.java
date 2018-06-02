package se.tink.backend.common.repository.cassandra;

import java.util.List;
import java.util.UUID;
import se.tink.libraries.cassandra.capabilities.Creatable;
import se.tink.backend.core.product.ProductFilter;

public interface ProductFilterRepositoryCustom extends Creatable {
    List<ProductFilter> findAllByTemplateId(UUID templateId);
    ProductFilter findByTemplateIdAndId(UUID templateId, UUID id);
}
