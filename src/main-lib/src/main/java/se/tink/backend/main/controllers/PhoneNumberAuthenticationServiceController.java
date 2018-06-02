package se.tink.backend.main.controllers;

import com.google.inject.Inject;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.main.helpers.SmsOtpAnalyticsHelper;
import se.tink.backend.rpc.auth.otp.InitiateSmsOtpCommand;
import se.tink.backend.rpc.auth.otp.InitiateSmsOtpResponse;
import se.tink.backend.rpc.auth.otp.VerifySmsOtpCommand;
import se.tink.backend.rpc.auth.otp.VerifySmsOtpResponse;
import se.tink.backend.sms.otp.controllers.SmsOtpController;
import se.tink.backend.sms.otp.core.exceptions.PhoneNumberBlockedException;
import se.tink.backend.sms.otp.core.exceptions.SmsOtpCouldNotBeSentException;
import se.tink.backend.sms.otp.rpc.GenerateSmsOtpRequest;
import se.tink.backend.sms.otp.rpc.GenerateSmsOtpResponse;
import se.tink.backend.sms.otp.rpc.VerifySmsOtpRequest;

public class PhoneNumberAuthenticationServiceController {
    private final SmsOtpAnalyticsHelper smsOtpAnalyticsHelper;
    private final SmsOtpController smsOtpController;
    private final UserRepository userRepository;

    @Inject
    public PhoneNumberAuthenticationServiceController(SmsOtpController smsOtpController, UserRepository userRepository,
            SmsOtpAnalyticsHelper smsOtpAnalyticsHelper) {
        this.smsOtpController = smsOtpController;
        this.smsOtpAnalyticsHelper = smsOtpAnalyticsHelper;
        this.userRepository = userRepository;
    }

    /**
     * Initiate a OTP by SMS.
     */
    public InitiateSmsOtpResponse initiateSmsOtp(InitiateSmsOtpCommand command)
            throws SmsOtpCouldNotBeSentException, PhoneNumberBlockedException {
        GenerateSmsOtpRequest otpRequest = new GenerateSmsOtpRequest(command.getPhoneNumber(), command.getLocale());

        GenerateSmsOtpResponse otpResponse = smsOtpController.generate(otpRequest);

        InitiateSmsOtpResponse initiateSmsOtpResponse = new InitiateSmsOtpResponse();
        initiateSmsOtpResponse.setToken(otpResponse.getId().toString());
        initiateSmsOtpResponse.setExpireAt(otpResponse.getExpireAt());
        initiateSmsOtpResponse.setOtpLength(otpResponse.getOtpLength());

        smsOtpAnalyticsHelper.trackSmsSent(command.getUser(), command.getPhoneNumber(), command.getRemoteAddress());

        return initiateSmsOtpResponse;
    }

    /**
     * Verify a SMS OTP.
     */
    public VerifySmsOtpResponse verifySmsOtp(VerifySmsOtpCommand command) {
        VerifySmsOtpRequest request = new VerifySmsOtpRequest(command.getSmsOtpVerificationToken(), command.getCode());

        se.tink.backend.sms.otp.rpc.VerifySmsOtpResponse otpResponse = smsOtpController.verify(request);

        VerifySmsOtpResponse result = new VerifySmsOtpResponse();
        result.setResult(otpResponse.getResult());
        result.setToken(otpResponse.getOtpId().toString());

        // Check and see if this phone number is used by an existing consumer or if it is a new user.
        if (otpResponse.isCorrectCode()) {
            result.setExistingUser(userRepository.findOneByUsername(otpResponse.getPhoneNumber()) != null);
        }

        smsOtpAnalyticsHelper.trackSmsVerified(command.getUser(), otpResponse.getResult(), command.getRemoteAddress());

        return result;
    }
}
