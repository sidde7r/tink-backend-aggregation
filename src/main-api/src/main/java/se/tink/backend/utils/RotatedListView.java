package se.tink.backend.utils;

import java.util.AbstractList;
import java.util.List;

public class RotatedListView<E> extends AbstractList<E> {

    public static <T> List<T> of(List<T> delegate, int rotation) {
        return new RotatedListView<>(delegate, rotation);
    }

    private final List<E> delegate;
    private final int rotation;

    private RotatedListView(List<E> delegate, int rotation) {
        this.delegate = delegate;
        this.rotation = rotation;
    }

    @Override
    public E get(int index) {
        // See http://stackoverflow.com/a/5385053.
        int possiblyNegativeIndex = (index - rotation) % delegate.size();
        int positiveIndex = possiblyNegativeIndex < 0 ? possiblyNegativeIndex + delegate.size() : possiblyNegativeIndex;

        return delegate.get(positiveIndex);
    }

    @Override
    public int size() {
        return delegate.size();
    }

}
