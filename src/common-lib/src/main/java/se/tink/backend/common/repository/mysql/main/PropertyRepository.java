package se.tink.backend.common.repository.mysql.main;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.core.property.Property;

public interface PropertyRepository extends JpaRepository<Property, String> {
    List<Property> findByUserId(String userId);
    Property findByUserIdAndId(String id, String propertyId);
}
