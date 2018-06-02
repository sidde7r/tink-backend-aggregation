package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.product.ProductFilter;

public interface ProductFilterRepository extends CassandraRepository<ProductFilter>, ProductFilterRepositoryCustom {      
    
}
