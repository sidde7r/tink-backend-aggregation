package se.tink.backend.utils;

import java.math.BigDecimal;
import se.tink.backend.core.Category;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPart;
import se.tink.backend.core.exceptions.InvalidCandidateException;
import se.tink.backend.core.exceptions.TransactionNotFoundException;

public class TransactionPartUtils {

    public static void link(Transaction transaction1, Transaction transaction2, Category category)
            throws TransactionNotFoundException, InvalidCandidateException {

        // Opposite sign required; an income can only be linked to an expense, and vice versa.
        if (Math.signum(transaction1.getAmount()) == Math.signum(transaction2.getAmount())) {
            throw new InvalidCandidateException("Transactions must be of opposite sign.");
        }

        // Calculate the dispensable amount
        BigDecimal dispensableAmount = calculateDispensableAmount(transaction1, transaction2);

        TransactionPart transaction1Part = TransactionPart.create(transaction1, dispensableAmount, category);
        TransactionPart transaction2Part = TransactionPart.create(transaction2, dispensableAmount, category);

        // Set the bilateral counterpart references.
        transaction1Part.setCounterpart(transaction2Part.getId(), transaction2.getId());
        transaction2Part.setCounterpart(transaction1Part.getId(), transaction1.getId());

        transaction1.addPart(transaction1Part);
        transaction2.addPart(transaction2Part);
    }

    private static BigDecimal calculateDispensableAmount(Transaction t1, Transaction t2) {
        return t1.getDispensableAmount().abs().min(t2.getDispensableAmount().abs());
    }
}
