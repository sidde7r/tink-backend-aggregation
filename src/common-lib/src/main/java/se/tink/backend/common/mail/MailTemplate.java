package se.tink.backend.common.mail;

import se.tink.backend.core.SubscriptionType;

public class MailTemplate {

    // USER RELATED
    public static final MailTemplate INFORM_USER_CHANGED;
    public static final MailTemplate FORGOT_PASSWORD;
    public static final MailTemplate FORGOT_PASSWORD_BLOCKED_USER;
    public static final MailTemplate GROWTH_SIGNUP;

    // REACTIVATION
    public static final MailTemplate REACTIVATE_USER;

    // ID CONTROL
    public static final MailTemplate ID_CONTROL_REMINDER;

    // CREDENTIALS
    public static final MailTemplate FAILING_CREDENTIALS_4;
    public static final MailTemplate FAILING_CREDENTIALS_8;
    public static final MailTemplate FAILING_CREDENTIALS_16;
    public static final MailTemplate FAILING_CREDENTIALS_32;

    static {
        INFORM_USER_CHANGED = new MailTemplate("tink-changed-user", null, false);
        FORGOT_PASSWORD = new MailTemplate("tink-forgot-password-v2", null, true);
        FORGOT_PASSWORD_BLOCKED_USER = new MailTemplate("tink-blocked-user-v2", null, true);

        GROWTH_SIGNUP = new MailTemplate("growth-signup", null, true);

        REACTIVATE_USER = new MailTemplate("tink-activate-tink-account-recurring", SubscriptionType.ROOT, false);

        ID_CONTROL_REMINDER = new MailTemplate("fraud-reminder", SubscriptionType.ROOT, false);

        FAILING_CREDENTIALS_4 = new MailTemplate("tink-failing-credentials-4", SubscriptionType.FAILING_CREDENTIALS_EMAIL, false);
        FAILING_CREDENTIALS_8 = new MailTemplate("tink-failing-credentials-8", SubscriptionType.FAILING_CREDENTIALS_EMAIL, false);
        FAILING_CREDENTIALS_16 = new MailTemplate("tink-failing-credentials-16", SubscriptionType.FAILING_CREDENTIALS_EMAIL, false);
        FAILING_CREDENTIALS_32 = new MailTemplate("tink-failing-credentials-32", SubscriptionType.FAILING_CREDENTIALS_EMAIL, false);
    }

    private final String identifier;
    private final SubscriptionType subscriptionType;
    private boolean sensitive;

    /**
     * Null subscriptionType means that the subscription check is bypassed. Should only be used for very special emails
     */
    MailTemplate(String identifier, SubscriptionType subscriptionType, boolean sensitive) {
        this.identifier = identifier;
        this.subscriptionType = subscriptionType;
        this.sensitive = sensitive;
    }

    public SubscriptionType getSubscriptionType() {
        return subscriptionType;
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean isSensitive() {
        return sensitive;
    }
}
