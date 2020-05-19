package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.EnumMap;
import java.util.Optional;

public enum StatusReasonInformationEntity {
    AC01("AC01"),
    AC04("AC04"),
    AC06("AC06"),
    AG01("AG01"),
    AM18("AM18"),
    CH03("CH03"),
    CUST("CUST"),
    DS02("DS02"),
    FF01("FF01"),
    FRAD("FRAD"),
    MS03("MS03"),
    NOAS("NOAS"),
    RR01("RR01"),
    RR03("RR03"),
    RR04("RR04"),
    RR12("RR12");

    private String value;

    StatusReasonInformationEntity(String value) {
        this.value = value;
    }

    private static EnumMap<StatusReasonInformationEntity, String> rejectStatusToErrorMapper =
            new EnumMap<>(StatusReasonInformationEntity.class);

    static {
        rejectStatusToErrorMapper.put(
                AC01,
                "(IncorectAccountNumber): the account number is either invalid or does not exist.");
        rejectStatusToErrorMapper.put(
                AC04, "(ClosedAccountNumber): the account is closed and cannot be used.");
        rejectStatusToErrorMapper.put(
                AC06, "(BlockedAccount): the account is blocked and cannot be used.");
        rejectStatusToErrorMapper.put(
                AG01, "(Transaction forbidden): Transaction forbidden on this type of account.");
        rejectStatusToErrorMapper.put(
                AM18,
                "(InvalidNumberOfTransactions): the number of transactions exceeds the ASPSP acceptance limit.");
        rejectStatusToErrorMapper.put(
                CH03,
                "(RequestedExecutionDateOrRequestedCollectionDateTooFarInFuture): The requested execution date is too far in the future.");
        rejectStatusToErrorMapper.put(
                CUST,
                "(RequestedByCustomer): The reject is due to the debtor: refusal or lack of liquidity.");
        rejectStatusToErrorMapper.put(
                DS02, "(OrderCancelled): An authorized user has cancelled the order.");
        rejectStatusToErrorMapper.put(
                FF01,
                "(InvalidFileFormat): The reject is due to the original Payment Request which is invalid (syntax, structure or values).");
        rejectStatusToErrorMapper.put(
                FRAD, "(FraudulentOriginated): the Payment Request is considered as fraudulent.");
        rejectStatusToErrorMapper.put(
                MS03, "(NotSpecifiedReasonAgentGenerated): No reason specified by the ASPSP.");
        rejectStatusToErrorMapper.put(
                NOAS,
                "(NoAnswerFromCustomer): The PSU has neither accepted nor rejected the Payment Request and a time-out has occurred.");
        rejectStatusToErrorMapper.put(
                RR01,
                "(MissingDebtorAccountOrIdentification): The Debtor account and/or Identification are missing or inconsistent.");
        rejectStatusToErrorMapper.put(
                RR03,
                "(MissingCreditorNameOrAddress): Specification of the creditor's name and/or address needed for regulatory requirements is insufficient or missing.");
        rejectStatusToErrorMapper.put(RR04, "(RegulatoryReason): Reject from regulatory reason.");
        rejectStatusToErrorMapper.put(
                RR12,
                "(InvalidPartyID): Invalid or missing identification required within a particular country or payment type.");
    }

    @JsonCreator
    public static StatusReasonInformationEntity fromValue(String text) {
        for (StatusReasonInformationEntity b : StatusReasonInformationEntity.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }

    public static String mapRejectStatusToError(StatusReasonInformationEntity status) {
        return Optional.ofNullable(rejectStatusToErrorMapper.get(status))
                .orElse("Unknown Rejection error from bank.");
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }
}
