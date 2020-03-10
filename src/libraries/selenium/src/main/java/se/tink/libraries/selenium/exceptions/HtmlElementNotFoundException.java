package se.tink.libraries.selenium.exceptions;

public class HtmlElementNotFoundException extends RuntimeException {
    public HtmlElementNotFoundException(String s) {
        super(s);
    }

    HtmlElementNotFoundException(String s, Throwable throwable) {
        super(s, throwable);
    }

    HtmlElementNotFoundException(Throwable throwable) {
        super(throwable);
    }
}
