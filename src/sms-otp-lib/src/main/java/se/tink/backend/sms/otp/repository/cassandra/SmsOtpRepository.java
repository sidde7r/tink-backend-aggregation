package se.tink.backend.sms.otp.repository.cassandra;

import java.util.UUID;
import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.sms.otp.core.SmsOtp;

public interface SmsOtpRepository extends CassandraRepository<SmsOtp>, SmsOtpRepositoryCustom {
    SmsOtp findOneById(UUID id);
}
