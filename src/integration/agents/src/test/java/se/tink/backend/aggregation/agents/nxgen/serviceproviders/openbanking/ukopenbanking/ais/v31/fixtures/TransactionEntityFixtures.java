package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.entities.transaction.TransactionEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class TransactionEntityFixtures {

    private static final String TRANSACTION_WITHOUT_MUTABILITY_FIELD =
            "{\"Status\":\"Booked\",\"BookingDateTime\":\"2020-09-01T08:36:23.353Z\",\"TransactionInformation\":\"SAVING POTS\",\"Amount\":{\"Amount\":\"265.0000\",\"Currency\":\"GBP\"},\"ProprietaryBankTransactionCode\":{\"Code\":\"payport_faster_payments\",\"Issuer\":\"Monzo\"},\"DebtorAccount\":{\"SchemeName\":\"UK.OBIE.SortCodeAccountNumber\",\"Identification\":\"40476470719927\",\"Name\":\"Frisby C\"},\"SupplementaryData\":{\"Declined\":false,\"RawTransactionDescription\":\"SAVING POTS\"}}";
    private static final String TRANSACTION_WITH_MUTABILITY_FIELD =
            "{\"AccountId\":\"acc_00009x3PRejaI4rXF0Dbvd\",\"TransactionId\":\"tx_00009yi2isqmm5qyNHUQHy\",\"CreditDebitIndicator\":\"Credit\",\"Status\":\"Booked\",\"TransactionMutability\":\"Mutable\",\"BookingDateTime\":\"2020-09-01T08:36:23.353Z\",\"TransactionInformation\":\"SAVING POTS\",\"Amount\":{\"Amount\":\"265.0000\",\"Currency\":\"GBP\"},\"ProprietaryBankTransactionCode\":{\"Code\":\"payport_faster_payments\",\"Issuer\":\"Monzo\"},\"DebtorAccount\":{\"SchemeName\":\"UK.OBIE.SortCodeAccountNumber\",\"Identification\":\"40476470719927\",\"Name\":\"Frisby C\"},\"SupplementaryData\":{\"Declined\":false,\"RawTransactionDescription\":\"SAVING POTS\"}}";
    private static final String TRANSACTION_WITH_STATUS_PENDING_AND_MUTABILITY =
            "{\"AccountId\":\"acc_00009x3PRejaI4rXF0Dbvd\",\"TransactionId\":\"tx_00009yi2isqmm5qyNHUQHy\",\"CreditDebitIndicator\":\"Credit\",\"Status\":\"Pending\",\"TransactionMutability\":\"Mutable\",\"BookingDateTime\":\"2020-09-01T08:36:23.353Z\",\"TransactionInformation\":\"SAVING POTS\",\"Amount\":{\"Amount\":\"265.0000\",\"Currency\":\"GBP\"},\"ProprietaryBankTransactionCode\":{\"Code\":\"payport_faster_payments\",\"Issuer\":\"Monzo\"},\"DebtorAccount\":{\"SchemeName\":\"UK.OBIE.SortCodeAccountNumber\",\"Identification\":\"40476470719927\",\"Name\":\"Frisby C\"},\"SupplementaryData\":{\"Declined\":false,\"RawTransactionDescription\":\"SAVING POTS\"}}";
    private static final String TRANSACTION_WITH_STATUS_BOOKED_AND_MUTABILITY =
            "{\"AccountId\":\"acc_00009x3PRejaI4rXF0Dbvd\",\"TransactionId\":\"tx_00009yi2isqmm5qyNHUQHy\",\"CreditDebitIndicator\":\"Credit\",\"Status\":\"Booked\",\"TransactionMutability\":\"Immutable\",\"BookingDateTime\":\"2020-09-01T08:36:23.353Z\",\"TransactionInformation\":\"SAVING POTS\",\"Amount\":{\"Amount\":\"265.0000\",\"Currency\":\"GBP\"},\"ProprietaryBankTransactionCode\":{\"Code\":\"payport_faster_payments\",\"Issuer\":\"Monzo\"},\"DebtorAccount\":{\"SchemeName\":\"UK.OBIE.SortCodeAccountNumber\",\"Identification\":\"40476470719927\",\"Name\":\"Frisby C\"},\"SupplementaryData\":{\"Declined\":false,\"RawTransactionDescription\":\"SAVING POTS\"}}";

    public static TransactionEntity getBookedTransactionWithUnspecifiedMutability() {
        return SerializationUtils.deserializeFromString(
                TRANSACTION_WITHOUT_MUTABILITY_FIELD, TransactionEntity.class);
    }

    public static TransactionEntity getMutableBookedTransaction() {
        return SerializationUtils.deserializeFromString(
                TRANSACTION_WITH_MUTABILITY_FIELD, TransactionEntity.class);
    }

    public static TransactionEntity getMutablePendingTransaction() {
        return SerializationUtils.deserializeFromString(
                TRANSACTION_WITH_STATUS_PENDING_AND_MUTABILITY, TransactionEntity.class);
    }

    public static TransactionEntity getImmutableBookedTransaction() {
        return SerializationUtils.deserializeFromString(
                TRANSACTION_WITH_STATUS_BOOKED_AND_MUTABILITY, TransactionEntity.class);
    }
}
