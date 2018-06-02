package se.tink.backend.utils;

/**
 * Can be used to tag API endpoints and logically separate them in for example the documentation.
 */
public class ApiTag {

    // Service name tags.
    public static final String FOLLOW_SERVICE = "Follow Service";
    public static final String CREDENTIALS_SERVICE = "Credentials Service";
    public static final String ACCOUNT_SERVICE = "Account Service";
    public static final String DEVICE_SERVICE = "Device Service";
    public static final String LOAN_SERVICE = "Loan Service";
    public static final String NOTIFICATION_SERVICE = "Notification Service";
    public static final String TRANSACTION_SERVICE = "Transaction Service";
    public static final String TRANSFER_SERVICE = "Transfer Service";
    public static final String USER_SERVICE = "User Service";
    public static final String USER_DATA_CONTROL_SERVICE = "User Data Control Service";

    // Additional tags. The HIDE tag can be used to hide certain endpoints from the API documentation. Swagger has a
    // `hidden = true` option, but that option hides it completely from the swagger JSON. If we want to have multiple
    // versions of the documentation where some endpoints are hidden in one and not the other, we can use a HIDE tag.
    public static final String HIDE = "HIDE";
}
