package se.tink.backend.utils;

import java.util.Iterator;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class IterableUtils {

    public static <T> boolean isPrefixOf(Iterable<T> baseList, Iterable<T> prefixList) {
        Iterator<T> baseIt = baseList.iterator();
        Iterator<T> prefixIt = prefixList.iterator();

        if (!sharePrefixes(baseIt, prefixIt)) {
            return false;
        }

        if (prefixIt.hasNext() && !baseIt.hasNext()) {
            return false;
        }
        
        return true;
    }
    
    private static <T> boolean sharePrefixes(Iterator<T> i1, Iterator<T> i2) {
        // If an empty prefix screams in the forrest, is it still a prefix? We don't handle this to not make a
        // standpoint.
        Preconditions.checkArgument(i2.hasNext(), "Empty prefix not handled.");
        Preconditions.checkArgument(i1.hasNext(), "Empty base not handled.");
        
        while (i2.hasNext() && i1.hasNext()) {
            T baseElement = i1.next();
            T prefixElement = i2.next();
            if (!Objects.equal(baseElement, prefixElement)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Check if one of the iterators is a prefix of the other.
     * @param i1 first iterator.
     * @param i2 first iterator.
     * @return true if one of the iterators is a prefix of the other.
     */
    public static <T> boolean sharePrefixes(Iterable<T> i1, Iterable<T> i2) {
        Iterator<T> baseIt = i1.iterator();
        Iterator<T> prefixIt = i2.iterator();

        return sharePrefixes(baseIt, prefixIt);
    }
    
}
