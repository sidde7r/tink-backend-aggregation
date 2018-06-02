package se.tink.backend.insights.core.valueobjects;

import java.util.Objects;

public class EInvoiceId {
    private String id;

    private EInvoiceId(String id) {
        this.id = id;
    }

    public String value() {
        return id;
    }

    public static EInvoiceId of(String id) {
        return new EInvoiceId(id);
    }

    @Override
    public String toString() {
        return id;
    }

    public boolean equals(EInvoiceId that) {
        return Objects.equals(id, that.value());
    }
}
