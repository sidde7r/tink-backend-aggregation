package se.tink.backend.core;

import java.util.Date;

public class TemporalValue<T> {
    private Date date;
    private T value;

    public TemporalValue(Date date, T value) {
        this.date = date;
        this.value = value;
    }

    public Date getDate() {
        return date;
    }

    public T getValue() {
        return value;
    }
}

