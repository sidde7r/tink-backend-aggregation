package se.tink.backend.sms.otp.core;

import java.util.Date;
import java.util.UUID;
import org.joda.time.DateTime;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

@Table(value = "sms_otps_events")
public class SmsOtpEvent {
    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    private String phoneNumber;

    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private UUID id;
    private Date timestamp;
    private SmsOtpEventType type;

    public SmsOtpEvent() {
    }

    public SmsOtpEvent(String phoneNumber, SmsOtpEventType type) {
        this(phoneNumber, type, DateTime.now());
    }

    public SmsOtpEvent(String phoneNumber, SmsOtpEventType type, DateTime timestamp) {
        this.id = UUID.randomUUID();
        this.phoneNumber = phoneNumber;
        this.type = type;
        this.timestamp = timestamp.toDate();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public SmsOtpEventType getType() {
        return type;
    }

    public UUID getId() {
        return id;
    }
}
