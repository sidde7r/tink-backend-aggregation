package se.tink.backend.core;

import com.google.common.collect.ImmutableList;

public class ClusterCategories {
    private final ImmutableList<Category> categories;

    public ClusterCategories(Iterable<Category> categories) {
        this.categories = ImmutableList.copyOf(categories);
    }

    public ImmutableList<Category> get() {
        return categories;
    }
}
