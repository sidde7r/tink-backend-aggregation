package se.tink.backend.common.search.containers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Calendar;
import java.util.List;
import se.tink.backend.core.Category;
import se.tink.backend.core.Transaction;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.TagsUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionSearchContainer {
    private double amount;
    private Category category;
    private int date;
    private int dayOfWeek;
    private List<String> tags;
    private Transaction transaction;

    public TransactionSearchContainer() {

    }

    public TransactionSearchContainer(Transaction transaction, Category category) {
        this.transaction = transaction;
        this.category = category;

        this.amount = Math.abs(transaction.getAmount());
        this.date = DateUtils.toInteger(transaction.getDate());

        Calendar calendar = DateUtils.getCalendar();
        calendar.setTime(transaction.getDate());

        this.dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        if (dayOfWeek == 0) {
            dayOfWeek = 7;
        }

        this.tags = TagsUtils.extractUniqueTags(transaction);
    }

    public double getAmount() {
        return amount;
    }

    public Category getCategory() {
        return category;
    }

    public int getDate() {
        return date;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public List<String> getTags() {
        return tags;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setAmount(double amount) {
        this.amount = Math.abs(amount);
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
}
