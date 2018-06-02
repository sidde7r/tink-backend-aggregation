package se.tink.backend.common.mail.monthly.summary.model;

import com.google.common.base.Strings;

public class EmailResult {
    private String subject;
    private String content;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isEmpty() {
        return Strings.isNullOrEmpty(content);
    }
}
