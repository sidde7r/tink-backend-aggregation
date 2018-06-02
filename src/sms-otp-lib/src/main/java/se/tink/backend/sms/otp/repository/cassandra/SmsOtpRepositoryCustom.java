package se.tink.backend.sms.otp.repository.cassandra;

import org.joda.time.Seconds;
import se.tink.backend.sms.otp.core.SmsOtp;
import se.tink.libraries.cassandra.capabilities.Creatable;

public interface SmsOtpRepositoryCustom extends Creatable {
    SmsOtp save(SmsOtp otp, Seconds ttl);
}
