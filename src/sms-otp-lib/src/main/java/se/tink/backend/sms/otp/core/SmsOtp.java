package se.tink.backend.sms.otp.core;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import org.joda.time.DateTime;
import org.joda.time.ReadablePeriod;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

@Table(value = "sms_otps")
public class SmsOtp {
    /**
     * Id of the OTP.
     */
    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    private UUID id;

    /**
     * The phone number that the OTP was sent to.
     */
    private String phoneNumber;

    /**
     * Timestamp when the OTP was created.
     */
    @Column("created")
    private Date createdAt;

    /**
     * Timestamp when the OTP was verified.
     */
    @Column("verified")
    private Date verifiedAt;

    /**
     * Timestamp when the OTP will expire.
     */
    @Column("expire")
    private Date expireAt;

    /**
     * The value/code of the OTP, for example 840114.
     */
    private String code;

    /**
     * Optional payload from the sms gateway.
     */
    private String payload;

    /**
     * Number of times the user has tried to verify the OTP.
     */
    private int verificationAttempts;

    /**
     * Status of the OTP.
     */
    private SmsOtpStatus status;

    /**
     * Type of the OTP
     */
    private OtpType type;

    public SmsOtpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public UUID getId() {
        return id;
    }

    public int getVerificationAttempts() {
        return verificationAttempts;
    }

    public OtpType getType() {
        return type;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public SmsOtpVerificationResult verify(String code, int maxVerificationAttempts) {
        this.verificationAttempts++;

        // Only OTP that was sent successfully can be verified
        if (status != SmsOtpStatus.SENT_SUCCESS) {
            return SmsOtpVerificationResult.INVALID_OTP_STATUS;
        }

        // Check that the OTP hasn't expired
        if (expireAt.before(new Date())) {
            return SmsOtpVerificationResult.OTP_EXPIRED;
        }

        if (verificationAttempts > maxVerificationAttempts) {
            return SmsOtpVerificationResult.TOO_MANY_VERIFICATION_ATTEMPTS;
        }

        if (Objects.equals(this.code, code)) {
            this.status = SmsOtpStatus.VERIFIED;
            this.verifiedAt = new Date();
            return SmsOtpVerificationResult.CORRECT_CODE;
        } else {
            return SmsOtpVerificationResult.INCORRECT_CODE;
        }
    }

    public SmsOtpConsumeResult consume() {
        if (status == SmsOtpStatus.VERIFIED) {
            status = SmsOtpStatus.CONSUMED;
            return SmsOtpConsumeResult.CONSUMED;
        } else if (status == SmsOtpStatus.CONSUMED) {
            return SmsOtpConsumeResult.ALREADY_CONSUMED;
        } else {
            return SmsOtpConsumeResult.INVALID_OTP_STATUS;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getExpireAt() {
        return expireAt;
    }

    public Date getVerifiedAt() {
        return verifiedAt;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPayload() {
        return payload;
    }

    public void setStatus(SmsOtpStatus status) {
        this.status = status;
    }

    public final static class Builder {
        private String phoneNumber;
        private String code;
        private OtpType type = OtpType.NUMERIC;
        private ReadablePeriod ttl;

        public Builder withPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder withCode(String code) {
            this.code = code;
            return this;
        }

        public Builder withTimeToLive(ReadablePeriod ttl) {
            this.ttl = ttl;
            return this;
        }

        public Builder withType(OtpType type) {
            this.type = type;
            return this;
        }

        public SmsOtp build() {
            Preconditions.checkState(!Strings.isNullOrEmpty(phoneNumber), "Phone number must not be null or empty.");
            Preconditions.checkState(!Strings.isNullOrEmpty(code), "Code must not be null or empty.");
            Preconditions.checkNotNull(type, "Type must not be null");
            Preconditions.checkNotNull(ttl, "Ttl must not be null.");

            SmsOtp otp = new SmsOtp();
            otp.id = UUID.randomUUID();
            otp.phoneNumber = phoneNumber;
            otp.code = this.code;
            otp.verificationAttempts = 0;
            otp.status = SmsOtpStatus.NOT_SENT;
            otp.type = type;

            otp.createdAt = new DateTime().toDate();
            otp.expireAt = new DateTime(otp.createdAt).plus(ttl).toDate();
            otp.verifiedAt = null;

            return otp;
        }
    }
}
