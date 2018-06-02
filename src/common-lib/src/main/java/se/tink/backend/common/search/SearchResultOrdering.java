package se.tink.backend.common.search;

import se.tink.backend.core.SearchResult;

import com.google.common.base.Preconditions;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Longs;

public class SearchResultOrdering {

    public static Ordering<SearchResult> orderingOnDate = new Ordering<SearchResult>() {
        @Override
        public int compare(SearchResult left, SearchResult right) {
            int result = 0;
            
            Preconditions.checkNotNull(left);
            Preconditions.checkNotNull(right);
            
            Preconditions.checkNotNull(left.getTransaction(), left);
            Preconditions.checkNotNull(right.getTransaction(), right);
            Preconditions.checkNotNull(left.getTransaction().getDate());
            Preconditions.checkNotNull(right.getTransaction().getDate());

            result = Longs.compare(left.getTransaction().getDate().getTime(), right.getTransaction().getDate()
                    .getTime());

            if (result == 0) {
                result = Longs.compare(left.getTransaction().getInserted(), right.getTransaction()
                        .getInserted());
            }
            
            Preconditions.checkNotNull(left.getTransaction(), left);
            Preconditions.checkNotNull(left.getTransaction(), right);

            if (result == 0) {
                result = left.getTransaction().getId().compareTo(right.getTransaction().getId());
            }

            return result;
        }
    };
    
    public static Ordering<SearchResult> orderingOnScore = new Ordering<SearchResult>() {
        @Override
        public int compare(SearchResult left, SearchResult right) {
            int result = 0;

            result = Doubles.compare(left.getScore(), right.getScore());

            Preconditions.checkNotNull(left.getTransaction(), left);
            Preconditions.checkNotNull(left.getTransaction(), right);

            if (result == 0) {
                result = left.getTransaction().getId().compareTo(right.getTransaction().getId());
            }
            
            return result;
        }
    };

    public static Ordering<SearchResult> orderingOnAmount = new Ordering<SearchResult>() {
        @Override
        public int compare(SearchResult left, SearchResult right) {
            int result = 0;
            
            Preconditions.checkNotNull(left.getTransaction(), left);
            Preconditions.checkNotNull(right.getTransaction(), right);

            result = Doubles.compare(left.getTransaction().getAmount(), right.getTransaction().getAmount());
            
            Preconditions.checkNotNull(left.getTransaction(), left);
            Preconditions.checkNotNull(left.getTransaction(), right);

            if (result == 0) {
                result = left.getTransaction().getId().compareTo(right.getTransaction().getId());
            }
            
            return result;
        }
    };
    
    public static Ordering<SearchResult> orderingOnDescription = new Ordering<SearchResult>() {
        @Override
        public int compare(SearchResult left, SearchResult right) {
            int result = 0;

            Preconditions.checkNotNull(left.getTransaction(), left);
            Preconditions.checkNotNull(left.getTransaction(), right);

            result = (left.getTransaction().getDescription().compareToIgnoreCase(right.getTransaction()
                    .getDescription()));
            
            Preconditions.checkNotNull(left.getTransaction(), left);
            Preconditions.checkNotNull(left.getTransaction(), right);

            if (result == 0) {
                result = left.getTransaction().getId().compareTo(right.getTransaction().getId());
            }

            return result;
        }
    };
    
}
