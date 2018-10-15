package se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.VolksbankConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class LoginUserNamePasswordForm extends AbstractForm {
    public LoginUserNamePasswordForm(String userId, String userName, String encryptedPassword, String viewState) {
        this.put(VolksbankConstants.Form.LOGINFORM + ":ignored-ids");
        this.put(VolksbankConstants.Form.LOGINFORM + ":userId", userId);
        this.put(VolksbankConstants.Form.LOGINFORM + ":userName", userName);
        this.put(VolksbankConstants.Form.LOGINFORM + ":password.encrypted", encryptedPassword);
        this.put(VolksbankConstants.Form.LOGINFORM + ":loginToken");
        this.put(VolksbankConstants.Form.LOGINFORM + ":signature");
        this.put("loginform_SUBMIT", "1");
        this.put(VolksbankConstants.Form.JSF_VIEWSTATE_KEY, viewState);
        this.put(VolksbankConstants.Form.JSF_EVENT_KEY, VolksbankConstants.Form.JSF_EVENT_CLICK);
        this.put(VolksbankConstants.Form.JSF_SOURCE_KEY, VolksbankConstants.Form.LOGINFORM + ":loginButton");
        this.put(VolksbankConstants.Form.JSF_PARTIAL_AJAX_KEY, VolksbankConstants.TRUE);
        this.put(VolksbankConstants.Form.JSF_PARTIAL_RESET_KEY, VolksbankConstants.TRUE);
        this.put(VolksbankConstants.Form.JSF_PARTIAL_EXECUTE_KEY, VolksbankConstants.Form.LOGINFORM);
        this.put(VolksbankConstants.Form.JSF_PARTIAL_RENDER_KEY, VolksbankConstants.Form.LOGINFORM);
        this.put(VolksbankConstants.Form.LOGINFORM, VolksbankConstants.Form.LOGINFORM);
    }

    public LoginUserNamePasswordForm(String generateId, String userId, String userName, String encryptedPassword,
            String viewState) {
        this.put(VolksbankConstants.Form.LOGINFORM + ":gid", generateId);
        this.put(VolksbankConstants.Form.LOGINFORM + ":ignored-ids");
        this.put(VolksbankConstants.Form.LOGINFORM + ":userId", userId);
        this.put(VolksbankConstants.Form.LOGINFORM + ":userName", userName);
        this.put(VolksbankConstants.Form.LOGINFORM + ":password.encrypted", encryptedPassword);
        this.put(VolksbankConstants.Form.LOGINFORM + ":loginToken");
        this.put(VolksbankConstants.Form.LOGINFORM + ":signature");
        this.put("loginform_SUBMIT", "1");
        this.put(VolksbankConstants.Form.JSF_VIEWSTATE_KEY, viewState);
        this.put(VolksbankConstants.Form.JSF_EVENT_KEY, VolksbankConstants.Form.JSF_EVENT_CLICK);
        this.put(VolksbankConstants.Form.JSF_SOURCE_KEY, VolksbankConstants.Form.LOGINFORM + ":loginButton");
        this.put(VolksbankConstants.Form.JSF_PARTIAL_AJAX_KEY, VolksbankConstants.TRUE);
        this.put(VolksbankConstants.Form.JSF_PARTIAL_RESET_KEY, VolksbankConstants.TRUE);
        this.put(VolksbankConstants.Form.JSF_PARTIAL_EXECUTE_KEY, VolksbankConstants.Form.LOGINFORM);
        this.put(VolksbankConstants.Form.JSF_PARTIAL_RENDER_KEY, VolksbankConstants.Form.LOGINFORM);
        this.put(VolksbankConstants.Form.LOGINFORM, VolksbankConstants.Form.LOGINFORM);
    }
}
