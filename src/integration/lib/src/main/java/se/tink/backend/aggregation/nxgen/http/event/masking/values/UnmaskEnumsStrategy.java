package se.tink.backend.aggregation.nxgen.http.event.masking.values;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import se.tink.backend.aggregation.nxgen.http.event.event_producers.pojo.FieldPathPart;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEventBankFieldType;

public class UnmaskEnumsStrategy implements RawBankDataFieldValueMaskingStrategy {

    private static final Set<String> ALLOWED_ENUM_VALUES =
            new HashSet<>(
                    Arrays.asList(
                            // UKOB-related enum values for data fetching
                            "SUCCESS",
                            "FAILURE",
                            "NOT_POSSIBLE",
                            "ISSUED_CREDIT_TRANSFERS",
                            "ISSUED_CASH_CONCENTRATION",
                            "ISSUED_DIRECT_DEBITS",
                            "ISSUED_CHEQUES",
                            "MERCHANT_CARD_TRANSACTIONS",
                            "CUSTOMER_CARD_TRANSACTIONS",
                            "DRAFTS_OF_ORDERS",
                            "BILL_OF_ORDERS",
                            "ISSUED_REAL_TIME_CREDIT_TRANSFER",
                            "RECEIVED_CREDIT_TRANSFERS",
                            "RECEIVED_CASH_CONCENTRATION",
                            "RECEIVED_DIRECT_DEBITS",
                            "RECEIVED_CHEQUES",
                            "LOCK_BOX",
                            "COUNTER_TRANSACTIONS",
                            "RECEIVED_REAL_TIME_CREDIT_TRANSFER",
                            "NOT_AVAILABLE",
                            "OTHER",
                            "MISCELLANEOUS_CREDIT_OPERATIONS",
                            "MISCELLANEOUS_DEBIT_OPERATIONS",
                            "CLEARED_BALANCE",
                            "AVAILABLE",
                            "CREDIT",
                            "EMERGENCY",
                            "PRE_AGREED",
                            "TEMPORARY",
                            "BOOKED",
                            "PENDING",
                            "REJECTED",
                            "MUTABLE",
                            "IMMUTABLE",
                            "UNDEFINED",
                            "BBAN",
                            "IBAN",
                            "PAYM",
                            "SORT_CODE_ACCOUNT_NUMBER",
                            "PAN",
                            "NWB_CURRENCY_ACCOUNT",
                            "RBS_CURRENCY_ACCOUNT",
                            "SAVINGS_ROLL_NUMBER",
                            "DANSKE_BANK_ACCOUNT_NUMBER",
                            "BILL_PAYMENT",
                            "ECOMMERCE_GOODS",
                            "ECOMMERCE_SERVICES",
                            "PERSON_TO_PERSON",
                            "OTHER",
                            "ACCEPTED_CUSTOMER_PROFILE",
                            "ACCEPTED_SETTLEMENT_COMPLETED",
                            "ACCEPTED_SETTLEMENT_IN_PROCESS",
                            "ACCEPTED_TECHNICAL_VALIDATION",
                            "PENDING",
                            "DEBIT",
                            "CREDIT",
                            "DELEGATE",
                            "JOINT",
                            "SOLE",
                            "AUTHORISED",
                            "AWAITING_AUTHORISATION",
                            "REVOKED",
                            "BUSINESS",
                            "PERSONAL",
                            "ClosingAvailable",
                            "ClosingBooked",
                            "ClosingCleared",
                            "Expected",
                            "ForwardAvailable",
                            "Information",
                            "InterimAvailable",
                            "InterimBooked",
                            "InterimCleared",
                            "OpeningAvailable",
                            "OpeningBooked",
                            "OpeningCleared",
                            "PreviouslyClosedBooked"));

    @Override
    public boolean shouldUseMaskingStrategy(
            List<FieldPathPart> fieldPathParts,
            String value,
            RawBankDataTrackerEventBankFieldType fieldType) {
        return ALLOWED_ENUM_VALUES.contains(value);
    }

    @Override
    public String produceMaskedValue(
            List<FieldPathPart> fieldPathParts,
            String value,
            RawBankDataTrackerEventBankFieldType fieldType) {
        return value;
    }
}
