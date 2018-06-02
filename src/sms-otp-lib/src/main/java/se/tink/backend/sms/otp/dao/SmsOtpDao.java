package se.tink.backend.sms.otp.dao;

import com.google.inject.Inject;
import java.util.List;
import java.util.UUID;
import org.joda.time.Days;
import org.joda.time.Seconds;
import se.tink.backend.sms.otp.core.SmsOtp;
import se.tink.backend.sms.otp.core.SmsOtpEvent;
import se.tink.backend.sms.otp.repository.cassandra.SmsOtpEventRepository;
import se.tink.backend.sms.otp.repository.cassandra.SmsOtpRepository;

public class SmsOtpDao {
    private static final Seconds TTL_SMS_OTP = Days.days(1).toStandardSeconds();

    private SmsOtpRepository repository;
    private SmsOtpEventRepository eventRepository;

    @Inject
    public SmsOtpDao(SmsOtpRepository repository, SmsOtpEventRepository eventRepository) {
        this.repository = repository;
        this.eventRepository = eventRepository;
    }

    public SmsOtp save(SmsOtp record) {
        return repository.save(record, TTL_SMS_OTP);
    }

    public SmsOtp findOneById(UUID id) {
        return repository.findOneById(id);
    }

    public SmsOtpEvent trackEvent(SmsOtpEvent record) {
        return eventRepository.save(record);
    }

    public List<SmsOtpEvent> findAllEventsByPhoneNumber(String phoneNumber) {
        return eventRepository.findAllByPhoneNumber(phoneNumber);
    }
}
