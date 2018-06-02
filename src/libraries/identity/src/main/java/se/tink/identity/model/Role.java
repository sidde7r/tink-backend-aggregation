package se.tink.libraries.identity.model;

public class Role {
    private String name;

    public Role(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Role of(String name) {
        return new Role(name);
    }
}
