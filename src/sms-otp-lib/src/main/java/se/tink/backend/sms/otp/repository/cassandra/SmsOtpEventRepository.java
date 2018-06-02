package se.tink.backend.sms.otp.repository.cassandra;

import java.util.List;
import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.sms.otp.core.SmsOtpEvent;

public interface SmsOtpEventRepository extends CassandraRepository<SmsOtpEvent>, SmsOtpEventRepositoryCustom {
    List<SmsOtpEvent> findAllByPhoneNumber(String phoneNumber);
}
