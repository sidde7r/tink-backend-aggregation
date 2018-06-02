package se.tink.backend.common.utils.repository;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterators;
import java.util.Iterator;
import rx.Observable;

public class RepositoryUtils {

    /**
     * Generate zero padded hex digits of fixed length.
     * <p>
     * Useful when fetching UUID prefixes in batches from database.
     * </p>
     * <p>
     * NB. Returns Iterable to support native Java foreach loop over this.
     * </p>
     * 
     * @param length
     *            the length of the hex strings.
     * @return For length of 2; "00", "01", ..., "ff".
     */
     public static Iterable<String> hexPrefixes(final int length) {
        Preconditions.checkArgument(length >= 0);
        return () -> hexPrefixesIterator(length);
    }

    /**
     * Generate zero padded hex digits of fixed length.
     * <p>
     * Useful when fetching UUID prefixes in batches from database.
     * </p>
     * 
     * @param length
     *            the length of the hex strings.
     * @return For length of 2; "00", "01", ..., "ff".
     */
    private static Iterator<String> hexPrefixesIterator(final int length) {
        Preconditions.checkArgument(length >= 0);

        if (length == 0) {
            return Iterators.singletonIterator("");
        }

        int upperCounterLimit = 1 << (4 * length); // Essentially 2^(4*length)=16^length since we are working in base
                                                   // 16.
        return Iterators.transform(se.tink.backend.common.utils.Iterators.range(0, upperCounterLimit, 1),
                input -> {
                    String hex = Long.toHexString(input);
                    if (hex.length() < length) {
                        // Pad with zeros
                        hex = Strings.repeat("0", length - hex.length()) + hex;
                    }
                    return hex;
                });
    }

    public static <T> Observable<T> streamAll(PrefixRepository<T> repository, int batchSize) {
        Preconditions.checkArgument(batchSize > 0, "batchSize must be positive.");
        return new RepositoryStreamer<T>(repository).streamAll(batchSize);
    }

}
