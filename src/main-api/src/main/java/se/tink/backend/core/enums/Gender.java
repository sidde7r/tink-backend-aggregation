package se.tink.backend.core.enums;

public enum Gender {
    FEMALE, MALE;

    public String toLowerCase() {
        return this.toString().toLowerCase();
    }

    public String toUpperCase() {
        return this.toString().toUpperCase();
    }

    public static Gender caseInsensitiveValueOf(String g) {
        if (g == null) {
            return null;
        }
        return Gender.valueOf(g.toUpperCase());
    }
}
