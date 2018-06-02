package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.product.ProductInstance;

public interface ProductInstanceRepository extends CassandraRepository<ProductInstance>, ProductInstanceRepositoryCustom {      
    
}
