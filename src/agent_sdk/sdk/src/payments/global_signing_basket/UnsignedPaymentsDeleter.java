package se.tink.agent.sdk.payments.global_signing_basket;

import java.util.List;
import se.tink.agent.sdk.models.payments.unsigned_payment.UnsignedPayment;

public interface UnsignedPaymentsDeleter {
    /** @return A list of unsigned payments. */
    List<UnsignedPayment> getUnsignedPayments();

    /**
     * @param unsignedPayments The unsigned payments to delete.
     * @return True if all unsigned payments were successfully deleted, otherwise false.
     */
    default boolean deleteUnsignedPayments(List<UnsignedPayment> unsignedPayments) {
        return unsignedPayments.stream().allMatch(this::deleteUnsignedPayment);
    }

    /**
     * @param unsignedPayment The unsigned payment to delete.
     * @return True if the unsigned payment was successfully deleted, otherwise false.
     */
    boolean deleteUnsignedPayment(UnsignedPayment unsignedPayment);
}
