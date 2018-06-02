package se.tink.backend.sms.otp.controllers;

import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import se.tink.backend.sms.gateways.SmsGateway;
import se.tink.backend.sms.gateways.rpc.SmsRequest;
import se.tink.backend.sms.gateways.rpc.SmsResponse;
import se.tink.backend.sms.otp.config.SmsOtpConfig;
import se.tink.backend.sms.otp.core.SmsOtp;
import se.tink.backend.sms.otp.core.SmsOtpConsumeResult;
import se.tink.backend.sms.otp.core.SmsOtpEvent;
import se.tink.backend.sms.otp.core.SmsOtpEventType;
import se.tink.backend.sms.otp.core.SmsOtpStatus;
import se.tink.backend.sms.otp.core.SmsOtpVerificationResult;
import se.tink.backend.sms.otp.core.exceptions.PhoneNumberBlockedException;
import se.tink.backend.sms.otp.core.exceptions.SmsOtpCouldNotBeSentException;
import se.tink.backend.sms.otp.core.exceptions.SmsOtpNotFoundException;
import se.tink.backend.sms.otp.dao.SmsOtpDao;
import se.tink.backend.sms.otp.rpc.ConsumeRequest;
import se.tink.backend.sms.otp.rpc.ConsumeResponse;
import se.tink.backend.sms.otp.rpc.GenerateSmsOtpRequest;
import se.tink.backend.sms.otp.rpc.GenerateSmsOtpResponse;
import se.tink.backend.sms.otp.rpc.VerifySmsOtpRequest;
import se.tink.backend.sms.otp.rpc.VerifySmsOtpResponse;
import se.tink.backend.sms.otp.utils.OtpCodeGenerator;
import se.tink.backend.sms.otp.utils.SmsOtpRateLimiter;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.uuid.UUIDUtils;

public class SmsOtpController {
    private final SmsOtpDao otpDao;
    private final SmsGateway smsGateway;
    private final SmsOtpConfig smsOtpConfig;
    private final SmsOtpRateLimiter rateLimiter;

    @Inject
    public SmsOtpController(SmsOtpDao otpDao, SmsGateway smsGateway, SmsOtpConfig smsOtpConfig,
            SmsOtpRateLimiter rateLimiter) {
        this.otpDao = otpDao;
        this.smsGateway = smsGateway;
        this.smsOtpConfig = smsOtpConfig;
        this.rateLimiter = rateLimiter;
    }

    /**
     * Generate and send an SMS OTP.
     */
    public GenerateSmsOtpResponse generate(GenerateSmsOtpRequest request)
            throws SmsOtpCouldNotBeSentException, PhoneNumberBlockedException {
        List<SmsOtpEvent> events = otpDao.findAllEventsByPhoneNumber(request.getPhoneNumber());

        rateLimiter.validate(events);

        SmsOtp otp = SmsOtp.builder()
                .withPhoneNumber(request.getPhoneNumber())
                .withCode(OtpCodeGenerator.generateCode(smsOtpConfig.getType(), smsOtpConfig.getOtpLength()))
                .withTimeToLive(smsOtpConfig.getTimeToLive())
                .withType(smsOtpConfig.getType())
                .build();

        SmsResponse response = sendSmsOtp(request, otp.getCode());

        otp.setStatus(response.isSuccess() ? SmsOtpStatus.SENT_SUCCESS : SmsOtpStatus.SENT_FAILED);
        otp.setPayload(response.getPayload());

        otpDao.save(otp);

        if (response.isSuccess()) {
            otpDao.trackEvent(new SmsOtpEvent(otp.getPhoneNumber(), SmsOtpEventType.SMS_SENT));

            GenerateSmsOtpResponse generateOtpResponse = new GenerateSmsOtpResponse();
            generateOtpResponse.setId(otp.getId());
            generateOtpResponse.setExpireAt(otp.getExpireAt());
            generateOtpResponse.setOtpLength(smsOtpConfig.getOtpLength());
            return generateOtpResponse;
        } else {
            throw new SmsOtpCouldNotBeSentException();
        }
    }

    /**
     * Send an OTP with the help of the SMS Gateway.
     */
    private SmsResponse sendSmsOtp(GenerateSmsOtpRequest request, String code) {
        Catalog catalog = Catalog.getCatalog(request.getLocale());

        String message = catalog.getString("Your verification code is") + ": " + code;

        SmsRequest smsRequest = SmsRequest.builder()
                .sender(smsOtpConfig.getSender())
                .to(request.getPhoneNumber())
                .message(message)
                .build();

        return smsGateway.send(smsRequest);
    }

    /**
     * Verify an OTP. An OTP can only be verified once (*).
     * (*) Lock handling isn't implemented. This means that two requests simultaneously could read up and update the
     * same row from the database.
     */
    public VerifySmsOtpResponse verify(VerifySmsOtpRequest request) {
        SmsOtp otp = otpDao.findOneById(request.getOtpId());

        final VerifySmsOtpResponse response = new VerifySmsOtpResponse();

        if (otp == null) {
            response.setResult(SmsOtpVerificationResult.OTP_NOT_FOUND);
            return response;
        }

        SmsOtpVerificationResult result = otp.verify(request.getCode(), smsOtpConfig.getMaxVerificationAttempts());

        otpDao.save(otp);

        if (result == SmsOtpVerificationResult.TOO_MANY_VERIFICATION_ATTEMPTS) {
            otpDao.trackEvent(new SmsOtpEvent(otp.getPhoneNumber(), SmsOtpEventType.TOO_MANY_VERIFICATION_ATTEMPTS));
        }

        response.setOtpId(otp.getId());
        response.setResult(result);
        response.setPhoneNumber(otp.getPhoneNumber());

        return response;
    }

    /**
     * Consume an OTP. An OTP can only be consumed once. OTP needs to be verified before it can be consumed.
     */
    public ConsumeResponse consume(ConsumeRequest request) throws SmsOtpNotFoundException {
        SmsOtp otp = otpDao.findOneById(request.getOtpId());

        if (otp == null) {
            throw new SmsOtpNotFoundException(request.getOtpId());
        }

        SmsOtpConsumeResult consumeResult = otp.consume();

        ConsumeResponse response = new ConsumeResponse();
        response.setResult(consumeResult);
        response.setPhoneNumber(otp.getPhoneNumber());

        otpDao.save(otp);

        return response;
    }

    /**
     * Return the phonenumber associated with an OTP
     *
     * @return Phone number if OTP is valid, otherwise empty
     */
    public Optional<String> getPhonenumberById(String otpId) {
        SmsOtp otp = otpDao.findOneById(UUIDUtils.fromString(otpId));

        if (otp == null) {
            return Optional.empty();
        }

        return Optional.of(otp.getPhoneNumber());
    }
}
