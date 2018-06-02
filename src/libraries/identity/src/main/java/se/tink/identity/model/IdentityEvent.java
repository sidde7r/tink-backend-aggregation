package se.tink.libraries.identity.model;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import se.tink.backend.core.Transaction;

public class IdentityEvent {
    private String id;
    private Date date;
    private String description;
    private boolean seen;
    private String question;
    private List<IdentityEventAnswer> answers;
    private IdentityAnswerKey answer;
    private IdentityEventDocumentation documentation;
    private List<Transaction> transactions;

    public IdentityEvent(String id, Date date, String description, boolean seen, String question,
            List<IdentityEventAnswer> answers, IdentityAnswerKey answer, IdentityEventDocumentation documentation,
            List<Transaction> transactions) {
        this.id = id;
        this.date = date;
        this.description = description;
        this.seen = seen;
        this.question = question;
        this.answers = answers;
        this.answer = answer;
        this.documentation = documentation;
        this.transactions = transactions;
    }

    public String getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSeen() {
        return seen;
    }

    public String getQuestion() {
        return question;
    }

    public List<IdentityEventAnswer> getAnswers() {
        return answers;
    }

    public IdentityAnswerKey getAnswer() {
        return answer;
    }

    public IdentityEventDocumentation getDocumentation() {
        return documentation;
    }

    public List<Transaction> getTransactions() {
        return transactions == null ? Collections.emptyList() : transactions;
    }
}
