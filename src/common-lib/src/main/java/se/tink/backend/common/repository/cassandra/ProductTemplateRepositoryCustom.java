package se.tink.backend.common.repository.cassandra;

import java.util.UUID;
import se.tink.libraries.cassandra.capabilities.Creatable;
import se.tink.backend.core.product.ProductTemplate;

public interface ProductTemplateRepositoryCustom  extends Creatable {
    ProductTemplate findById(UUID id);
}
