package se.tink.backend.common.repository.mysql.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.tink.backend.core.AbnAmroCustomer;
import se.tink.backend.core.AbnAmroCustomerPk;

@Repository
public interface AbnAmroCustomerRepository extends JpaRepository<AbnAmroCustomer, AbnAmroCustomerPk> {

}
