package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.Loan;

public interface LoanDataRepository extends CassandraRepository<Loan>, LoanDataRepositoryCustom {

}