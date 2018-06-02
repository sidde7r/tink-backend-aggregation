package se.tink.backend.core.follow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public enum FollowTypes {
    EXPENSES, SEARCH, SAVINGS;

    public static final String DOCUMENTED =  "EXPENSES, SEARCH, SAVINGS";
}
