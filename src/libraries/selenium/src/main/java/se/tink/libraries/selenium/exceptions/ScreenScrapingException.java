package se.tink.libraries.selenium.exceptions;

public class ScreenScrapingException extends RuntimeException {

    public ScreenScrapingException(String s) {
        super(s);
    }

    ScreenScrapingException(String s, Throwable throwable) {
        super(s, throwable);
    }

    ScreenScrapingException(Throwable throwable) {
        super(throwable);
    }
}
