package se.tink.libraries.identity.model;

import java.util.Date;

public class IdentityEventSummary {
    private String id;
    private Date date;
    private String description;
    private boolean seen;
    private IdentityAnswerKey answer;

    public IdentityEventSummary(String id, Date date, String description, boolean seen, IdentityAnswerKey answer) {
        this.id = id;
        this.date = date;
        this.description = description;
        this.seen = seen;
        this.answer = answer;
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

    public IdentityAnswerKey getAnswer() {
        return answer;
    }
}
