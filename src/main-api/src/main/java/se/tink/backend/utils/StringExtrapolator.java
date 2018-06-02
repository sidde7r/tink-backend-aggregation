package se.tink.backend.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Set;
import java.util.SortedMap;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import org.apache.commons.collections4.trie.PatriciaTrie;

/**
 * Extrapolate string based on supplied corpus
 */
public class StringExtrapolator {

    private PatriciaTrie<String> trie;
    private final static boolean DEFAULT_GREEDY = false;
    
    private Set<String> terminationWords;
    
    // Used to only have one value reference (since the trie is a KV pair structure)
    private final static String EMPTY_STRING = "";
    
    public StringExtrapolator() {
        this.trie = new PatriciaTrie<String> ();
    }
    
    /**
     * Get the corpus
     * @return
     */
    public Set<String> getCorpus() {
        return trie.keySet();
    }
    
    /**
     * Load corpus (from file) to be used for extrapolation
     * @param filename
     * @return String extrapolator with corpus loaded from file
     * @throws IOException
     */
    public static StringExtrapolator load(String filename) throws IOException {
        return load(filename, null);
    }
    
    /**
     * Load corpus (from file) to be used for extrapolation
     * @param filename
     * @param transformation
     * @return String extrapolator with corpus loaded from file
     * @throws IOException
     */
    public static StringExtrapolator load(String filename, Function<String, String> transformation) throws IOException {
        
        StringExtrapolator extrapolator = new StringExtrapolator();
        BufferedReader reader = new BufferedReader(new InputStreamReader(Files.asByteSource(
                new File(filename)).openStream(), Charsets.UTF_8));
        
        String line = null;
        
        while ((line = reader.readLine()) != null) {
            if (transformation != null) {
                line = transformation.apply(line);
            }
            
            extrapolator.add(line);
        }
        
        reader.close();
        
        return extrapolator;
    }
    
    /**
     * Add string to be used for extrapolation
     * @param string
     */
    public void add(String string) {
        trie.put(string, EMPTY_STRING);
    }
    
    /**
     * Extrapolate string (with default greedyness)
     * @param string
     * @return Extrapolated string
     */
    public String extrapolate(String string) {
        return extrapolate(string, DEFAULT_GREEDY);
    }
    
    /**
     * Extrapolate string
     * @param string
     * @param greedy
     * @return Extrapolated string
     */
    public String extrapolate(String string, boolean greedy) {
        String extrapolated = string;
        
        // Just for optimization.
        if (isTerminationWord(extrapolated)) {
            return extrapolated;
        }
        
        SortedMap<String, String> originalPrefixMap = Maps.newTreeMap(trie.prefixMap(string));
        originalPrefixMap.remove(string);
        
        if (originalPrefixMap.size() > 0) {
            String sample = originalPrefixMap.firstKey();
            
            while (sample.length() > extrapolated.length()) {
                
                // Terminate if the word is a termination word.
                if (isTerminationWord(extrapolated)) {
                    break;
                }
                
                String tmp = extrapolated + sample.charAt(extrapolated.length());

                if (trie.prefixMap(tmp).size() == originalPrefixMap.size()) {
                    extrapolated = tmp;
                } else {
                    break;
                }
            }
        }

        if (greedy && !extrapolated.equals(string)) {
            return extrapolate(extrapolated, greedy);
        } else {
            return extrapolated;
        }
    }

    /**
     * Check if a word is a termination word.
     * @param word
     * @return
     */
    private boolean isTerminationWord(String word) {
        if (terminationWords == null || terminationWords.size() == 0) {
            return false;
        }
        
        return terminationWords.contains(word);
    }

    /**
     * Load termination words from file.
     * @param filename
     * @throws IOException
     */
    public void loadTerminationWords(String filename) throws IOException {
        setTerminationWords(Files.readLines(new File(filename), Charsets.UTF_8));
    }

    /**
     * Set a collection of termination words. Termination words will terminate the extrapolation if matched,
     * to avoid undesired extrapolations (due to the greedy nature of the routine).
     * @param terminationWords
     */
    public void setTerminationWords(Collection<String> terminationWords) {
        this.terminationWords = Sets.newHashSet(terminationWords);
    }
}
