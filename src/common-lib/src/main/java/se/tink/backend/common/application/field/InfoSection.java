package se.tink.backend.common.application.field;


public class InfoSection<T, B> {
    
    private final T title;
    private final B body;
    
    public InfoSection(T title, B body) {
        this.title = title;
        this.body = body;
    }
    
    public T getTitle() {
        return title;
    }
    
    public B getBody() {
        return body;
    }
}
