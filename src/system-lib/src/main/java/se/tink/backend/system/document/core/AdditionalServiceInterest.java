package se.tink.backend.system.document.core;

import java.util.List;
import java.util.Set;

public class AdditionalServiceInterest {

    private final String name;
    private final Set<String> interest;

    public AdditionalServiceInterest(String name, Set<String> interest) {
        this.name = name;
        this.interest = interest;
    }

    public String getName() {
        return name;
    }

    public Set<String> getInterest() {
        return interest;
    }
}
