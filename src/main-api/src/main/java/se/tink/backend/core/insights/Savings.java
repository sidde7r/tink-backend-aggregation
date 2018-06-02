package se.tink.backend.core.insights;

import io.protostuff.Tag;

public class Savings implements Insight {
    @Tag(1)
    private String title;
    @Tag(2)
    private String body;
    @Tag(3)
    private Double amount;

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getBody() {
        return body;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
