package se.tink.backend.system.workers.processor.categorization;

import com.google.common.base.MoreObjects;
import java.util.List;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.core.Category;
import se.tink.backend.core.Transaction;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorCommandResult;

/**
 * Set the transaction uncategorized if there is no category set.
 * 
 * Sets:
 *  categoryId
 */
public class UnknownCategorizationCommand implements TransactionProcessorCommand {
	private Category unknownIncomeCategory;
	private Category unknownExpenseCategory;

	public UnknownCategorizationCommand(
			List<Category> categories, CategoryConfiguration categoryConfiguration) {
		unknownIncomeCategory = categories.stream()
				.filter(c -> categoryConfiguration.getIncomeUnknownCode().equals(c.getCode()))
				.findFirst()
				.get();
		unknownExpenseCategory = categories.stream()
				.filter(c -> categoryConfiguration.getExpenseUnknownCode().equals(c.getCode()))
				.findFirst()
				.get();
	}

	@Override
	public TransactionProcessorCommandResult execute(Transaction transaction) {
		// Only set process the transaction if it hasn't already been
		// categorized by some other transaction processor command.

		if (transaction.getCategoryId() != null) {
			return TransactionProcessorCommandResult.CONTINUE;
		}

		if (transaction.getAmount() > 0) {
			transaction.setCategory(unknownIncomeCategory);
		} else {
			transaction.setCategory(unknownExpenseCategory);
		}

		return TransactionProcessorCommandResult.CONTINUE;
	}

	public TransactionProcessorCommandResult initialize() {
		return TransactionProcessorCommandResult.CONTINUE;
	}

	/**
	 * Called for every command in command chain's reverse order at after processing all transactions.
	 */
	@Override
	public void postProcess() {
		// Deliberately left empty.
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("unknownIncomeCategory", unknownIncomeCategory)
				.add("unknownExpenseCategory", unknownExpenseCategory)
				.toString();
	}
}
