package se.tink.backend.utils;


import com.google.common.base.Strings;

public class NGram {
    public static String[] tokenize(String string, int n) {
        int count = Math.max(1, (string.length() - (n - 1)));
        String[] tokens = new String[count];

        if (string.length() <= n) {
            tokens[0] = Strings.padStart(string, n, CategorizationUtils.DEFAULT_CHARACTER);
        } else {
            for (int i = 0; i < count; i++) {
                tokens[i] = string.substring(i, i + n);
            }
        }

        return tokens;
    }

    private int n;

    public NGram(int n) {
        this.n = n;
    }

    public int getN() {
        return n;
    }

    public String[] tokenize(String string) {
        return tokenize(string, n);
    }
}
