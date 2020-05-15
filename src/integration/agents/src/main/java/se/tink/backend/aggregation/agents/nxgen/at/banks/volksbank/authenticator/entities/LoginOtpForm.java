package se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.authenticator.entities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.VolksbankConstants;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class LoginOtpForm extends AbstractForm {

    private final DateFormat simpleDateFormat =
            new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'Z (zzz)", Locale.US);

    // For manual auth
    public LoginOtpForm(String viewState) {
        this.put("otplogin:otpform_SUBMIT", VolksbankConstants.ONE);
        this.put(VolksbankConstants.Form.JSF_VIEWSTATE_KEY, viewState);
        this.put(VolksbankConstants.Form.JSF_EVENT_KEY, VolksbankConstants.Form.JSF_EVENT_ACTION);
        this.put(
                VolksbankConstants.Form.JSF_SOURCE_KEY,
                VolksbankConstants.Form.OTPLOGINOTPFORM + ":noOTPData");
        this.put(VolksbankConstants.Form.JSF_PARTIAL_AJAX_KEY, VolksbankConstants.TRUE);
        this.put(
                VolksbankConstants.Form.JSF_PARTIAL_EXECUTE_KEY,
                VolksbankConstants.Form.OTPLOGINOTPFORM);
        this.put(VolksbankConstants.Form.OTPLOGINOTPFORM, VolksbankConstants.Form.OTPLOGINOTPFORM);
    }

    // For auto auth
    public LoginOtpForm(String viewState, String userId, String generateId, String totp) {
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Stockholm"));
        this.put("otplogin:otpform_SUBMIT", VolksbankConstants.ONE);
        this.put(VolksbankConstants.Form.JSF_VIEWSTATE_KEY, viewState);
        this.put("verfuegerNr", userId);
        this.put("geraeteId", generateId);
        this.put("otp", totp);
        this.put("otpTime", simpleDateFormat.format(new Date()));
        this.put(VolksbankConstants.Form.JSF_EVENT_KEY, VolksbankConstants.Form.JSF_EVENT_ACTION);
        this.put(
                VolksbankConstants.Form.JSF_SOURCE_KEY,
                VolksbankConstants.Form.OTPLOGINOTPFORM + ":otpsubmit");
        this.put(VolksbankConstants.Form.JSF_PARTIAL_AJAX_KEY, VolksbankConstants.TRUE);
        this.put(
                VolksbankConstants.Form.JSF_PARTIAL_EXECUTE_KEY,
                VolksbankConstants.Form.OTPLOGINOTPFORM);
        this.put(VolksbankConstants.Form.OTPLOGINOTPFORM, VolksbankConstants.Form.OTPLOGINOTPFORM);
    }
}
