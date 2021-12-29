package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.enums;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LastError {
    ACCOUNT_BLACKLISTED("account_blacklisted"),
    ACCOUNTS_FAILED("accounts_failed"),
    AGE_VERIFICATION_NOT_CONFIRMED("age_verification_not_confirmed"),
    AUTHORIZED_PERSON("authorized_person"),
    BALANCE_FAILED("balance_failed"),
    BANK_CODE_UNKNOWN("bank_code_unknown"),
    CLIENT_ABORTED("client_aborted"),
    CLIENT_NOT_STARTED("client_not_started"),
    CONSENT_INVALID("consent_invalid"),
    COUNTRY_ID_INVALID("country_id_invalid"),
    FORCED_TRANSPORT_DISABLED("forced_transport_disabled"),
    LOGIN_BLACKLISTED("login_blacklisted"),
    LOGIN_FAILED("login_failed"),
    LOGIN_NEXT_FAILED("login_next_failed"),
    MAX_LOGIN_TRIES("max_login_tries"),
    MAX_TAN_TRIES("max_tan_tries"),
    NO_COMPATIBLE_ACCOUNTS("no_compatible_accounts"),
    NO_TRANSPORTS_FOUND("no_transports_found"),
    PINNED_HOLDER_NOT_FOUND("pinned_holder_not_found"),
    PINNED_IBAN_NOT_FOUND("pinned_iban_not_found"),
    PP_NOT_CHECKED("pp_not_checked"),
    SECURITY_BALANCE_FAILED("security_balance_failed"),
    SECURITY_CHARGEBACKS_EXCEEDED("security_chargebacks_exceeded"),
    SECURITY_HIGH_ROLLER_VOLUME_REACHED("security_high_roller_volume_reached"),
    SECURITY_IS_SEIZURE("security_is_seizure"),
    SECURITY_LOSS_PMTS_CHECK_FAILED("security_loss_pmts_check_failed"),
    SECURITY_LOW_TURNOVER_COUNT("security_low_turnover_count"),
    SECURITY_MAX_AMOUNT_EXCEEDED("security_max_amount_exceeded"),
    SECURITY_MAX_TA_COUNT_REACHED("security_max_ta_count_reached"),
    SECURITY_MAX_VOLUME_REACHED("security_max_volume_reached"),
    SECURITY_OLD_TRANSACTION_MISSING("security_old_transaction_missing"),
    SECURITY_PENDING_PMTS_CHECK_FAILED("security_pending_pmts_check_failed"),
    SECURITY_PREBOOKED_ORDERS_EXCEEDED("security_prebooked_orders_exceeded"),
    SECURITY_SAME_BALANCE_FAILED("security_same_balance_failed"),
    SECURITY_TAGS_CHECK_FAILED("security_tags_check_failed"),
    SESSION_EXPIRED("session_expired"),
    STANDING_ORDERS_FAILED("standing_orders_failed"),
    TECH_ERROR("tech_error"),
    TESTMODE_ERROR("testmode_error"),
    TOKEN_MISMATCH("token_mismatch"),
    TRANSACTION_FAILED("transaction_failed"),
    TURNOVERS_FAILED("turnovers_failed"),
    TX_RX_IBAN_EQUAL("tx_rx_iban_equal"),
    VALIDATION_FAILED("validation_failed"),
    WRONG_TAN("wrong_tan"),
    UNKNOWN("Unknown");

    private final String code;

    public static LastError fromString(String text) {
        return Arrays.stream(LastError.values())
                .filter(s -> s.code.equalsIgnoreCase(text))
                .findFirst()
                .orElse(UNKNOWN);
    }

    @Override
    public String toString() {
        return code;
    }
}
