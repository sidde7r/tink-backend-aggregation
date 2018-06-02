package se.tink.backend.utils;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;

public class CategorizationUtils {    
    public final static String VALID_CHARACTERS = "_0123456789abcdefghijklmnopqrstuvwxyz ";
    public final static char DEFAULT_CHARACTER = '_';
    
    public final static Pattern trimNumbersStart = Pattern.compile("^\\d+( |$)");
    public final static Pattern trimNumbersEnd = Pattern.compile("(^| )\\d+$");
    
    /**
     * 
     * @param string
     * @return
     */
    public static String removeSwedishPersonalIdentityNumber(String string) {
        return string.replaceAll("\\d{6}-\\d{4}", "");
    }
    
    
    public static String removeDutchCardNumber(String string) {
        return string.replaceAll("pas\\d{3,4}", "");
    }
    
    /**
     * 
     * @param string
     * @return
     */
    public static String clean(String string) {
        
        String cleaned = string;
        
        cleaned = cleaned
                .toLowerCase()
                .replaceAll("[,\\*\"&()]", " ")
                .replaceAll(" - ", " ")
                .replaceAll("[\t ]+", " ")
                .replaceAll(
                        String.format("[^%s]", VALID_CHARACTERS),
                        String.valueOf(DEFAULT_CHARACTER));
        
        return trim(cleaned);
    }
    
    /**
     * Trim disconnected numbers from a string (e.g. the 7 in "7 eleven", but not in "7-eleven"). 
     * @param string
     * @return
     */
    public static String trimNumbers(String string) {
        
        String cleaned = string;
        Matcher m;
        
        m = trimNumbersStart.matcher(cleaned);
        if (m.find()) {
            cleaned = m.replaceFirst("");
        }
        
        m = trimNumbersEnd.matcher(cleaned);
        if (m.find()) {
            cleaned = m.replaceFirst("");
        }
       
        return trim(cleaned);
    }
    
    /**
     * 
     * @param string
     * @return
     */
    public static String trim(String string) {
        return CharMatcher.anyOf(" \"").trimFrom(string);
    }
    
    /**
     * Interprets the `string` as a `string.length()` bit long number of the base `validCharacters.length()`
     * @param string
     * @return
     */
    public static int index(String string) {
        int index = 0;

        for (int i = 0; i < string.length(); i++) {
            index += Math.pow(VALID_CHARACTERS.length(), string.length() - (i + 1)) * VALID_CHARACTERS
                    .indexOf(string.charAt(i));
        }
        
        return index;
    }
    
    /**
     * Create feature vector from string (n-grams and possibly full, cleaned string [if exact match is boosted])
     * 
     * @param string
     * @return
     */
    public static Vector createFeatureVector(String string, int n, float boost, QuickIndex dynamicDomain) {
        Preconditions.checkNotNull(dynamicDomain, "dynamicDomain must not be null");

        int staticDomainSize = (int) Math.pow(VALID_CHARACTERS.length(), n);
        
        int cardinality = staticDomainSize + dynamicDomain.getMaxSize();
        String[] tokens = NGram.tokenize(string, n);        
        Map<Integer, Double> features = Maps.newHashMap();
        
        for (int i = 0; i < tokens.length; i++) {
            
            int index = CategorizationUtils.index(tokens[i]);

            if (features.containsKey(index)) {
                features.put(index, features.get(index) + 1d);
            } else {
                features.put(index, 1d);
            }
        }
        
        Vector vector = new RandomAccessSparseVector(cardinality);

        for (Entry<Integer, Double> feature : features.entrySet()) {
            vector.set(feature.getKey(), feature.getValue());
        }
        
        // Boost exact match
        if (boost > 0) { 
            int index = dynamicDomain.indexOf(string);
            if (index > -1 && index < dynamicDomain.getMaxSize()) {
                // Add the full description as a feature as well
                vector.set(staticDomainSize + index, boost);            
            }
        }
        
        return vector;
    }
}
