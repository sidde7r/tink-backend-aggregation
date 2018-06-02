package se.tink.backend.common.dao;

import java.io.IOException;

public class CacheWriteException extends RuntimeException {
    CacheWriteException(IOException e) {
        super(e);
    }
}
