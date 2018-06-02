package se.tink.backend.categorization.interfaces;

import se.tink.backend.core.Category;
import se.tink.backend.core.Transaction;

public interface Categorizer {
    /**
     * Categorize a transaction.
     *
     * If the category likely came from a USER_LEARNING Classifier and the category type of transaction t is TRANSFER,
     * a Categorizer should return the unaltered Category of transaction t.
     *
     * @param t the transaction to be categorized
     * @return category code for a category.
     */
    Category categorize(Transaction t);

    String getLabel();
}
