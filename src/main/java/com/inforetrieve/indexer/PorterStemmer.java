package com.inforetrieve.indexer;

/**
 * Implementation of Martin Porter's stemming algorithm for English words.
 * Reduces words to their morphological root stem.
 */
public class PorterStemmer {

    public String stem(String word) {
        if (word == null || word.length() <= 2) {
            return word != null ? word.toLowerCase() : "";
        }
        
        char[] b = word.toLowerCase().toCharArray();
        int k = b.length - 1;
        int k0 = 0;

        // Step 1a
        if (b[k] == 's') {
            if (endsWith(b, k0, k, "sses")) {
                k -= 2;
            } else if (endsWith(b, k0, k, "ies")) {
                k -= 2;
                b[k] = 'i';
            } else if (k > k0 && b[k - 1] != 's') {
                k--;
            }
        }

        if (endsWith(b, k0, k, "eed")) {
            if (m(b, k0, k - 3) > 0) {
                k--;
            }
        } else if ((endsWith(b, k0, k, "ed") || endsWith(b, k0, k, "ing")) && vowelInStem(b, k0, k - (endsWith(b, k0, k, "ed") ? 2 : 3))) {
            k -= (endsWith(b, k0, k, "ed") ? 2 : 3);
            if (endsWith(b, k0, k, "at")) {
                k = appendStr(b, k, "e");
            } else if (endsWith(b, k0, k, "bl")) {
                k = appendStr(b, k, "e");
            } else if (endsWith(b, k0, k, "iz")) {
                k = appendStr(b, k, "e");
            } else if (doubleConsonant(b, k0, k)) {
                k--;
                char ch = b[k];
                if (ch == 'l' || ch == 's' || ch == 'z') {
                    k++;
                }
            } else if (m(b, k0, k) == 1 && cvc(b, k0, k)) {
                k = appendStr(b, k, "e");
            }
        }

        // Step 1c
        if (endsWith(b, k0, k, "y") && vowelInStem(b, k0, k - 1)) {
            b[k] = 'i';
        }

        // Step 2
        switch (b[k]) {
            case 'a':
                if (endsWith(b, k0, k, "ational")) { k = replaceSuffix(b, k0, k, "ational", "ate"); break; }
                if (endsWith(b, k0, k, "tional")) { k = replaceSuffix(b, k0, k, "tional", "tion"); break; }
                break;
            case 'c':
                if (endsWith(b, k0, k, "enci")) { k = replaceSuffix(b, k0, k, "enci", "ence"); break; }
                if (endsWith(b, k0, k, "anci")) { k = replaceSuffix(b, k0, k, "anci", "ance"); break; }
                break;
            case 'e':
                if (endsWith(b, k0, k, "izer")) { k = replaceSuffix(b, k0, k, "izer", "ize"); break; }
                break;
            case 'l':
                if (endsWith(b, k0, k, "bli")) { k = replaceSuffix(b, k0, k, "bli", "ble"); break; }
                if (endsWith(b, k0, k, "alli")) { k = replaceSuffix(b, k0, k, "alli", "al"); break; }
                if (endsWith(b, k0, k, "entli")) { k = replaceSuffix(b, k0, k, "entli", "ent"); break; }
                if (endsWith(b, k0, k, "eli")) { k = replaceSuffix(b, k0, k, "eli", "e"); break; }
                if (endsWith(b, k0, k, "ousli")) { k = replaceSuffix(b, k0, k, "ousli", "ous"); break; }
                break;
            case 'o':
                if (endsWith(b, k0, k, "ization")) { k = replaceSuffix(b, k0, k, "ization", "ize"); break; }
                if (endsWith(b, k0, k, "ation")) { k = replaceSuffix(b, k0, k, "ation", "ate"); break; }
                if (endsWith(b, k0, k, "ator")) { k = replaceSuffix(b, k0, k, "ator", "ate"); break; }
                break;
            case 's':
                if (endsWith(b, k0, k, "alism")) { k = replaceSuffix(b, k0, k, "alism", "al"); break; }
                if (endsWith(b, k0, k, "iveness")) { k = replaceSuffix(b, k0, k, "iveness", "ive"); break; }
                if (endsWith(b, k0, k, "fulness")) { k = replaceSuffix(b, k0, k, "fulness", "ful"); break; }
                if (endsWith(b, k0, k, "ousness")) { k = replaceSuffix(b, k0, k, "ousness", "ous"); break; }
                break;
            case 't':
                if (endsWith(b, k0, k, "aliti")) { k = replaceSuffix(b, k0, k, "aliti", "al"); break; }
                if (endsWith(b, k0, k, "iviti")) { k = replaceSuffix(b, k0, k, "iviti", "ive"); break; }
                if (endsWith(b, k0, k, "biliti")) { k = replaceSuffix(b, k0, k, "biliti", "ble"); break; }
                break;
            case 'g':
                if (endsWith(b, k0, k, "logi")) { k = replaceSuffix(b, k0, k, "logi", "log"); break; }
                break;
        }

        // Step 3
        switch (b[k]) {
            case 'e':
                if (endsWith(b, k0, k, "icate")) { k = replaceSuffix(b, k0, k, "icate", "ic"); break; }
                if (endsWith(b, k0, k, "ative")) { k = replaceSuffix(b, k0, k, "ative", ""); break; }
                if (endsWith(b, k0, k, "alize")) { k = replaceSuffix(b, k0, k, "alize", "al"); break; }
                break;
            case 'i':
                if (endsWith(b, k0, k, "iciti")) { k = replaceSuffix(b, k0, k, "iciti", "ic"); break; }
                break;
            case 'l':
                if (endsWith(b, k0, k, "ical")) { k = replaceSuffix(b, k0, k, "ical", "ic"); break; }
                if (endsWith(b, k0, k, "ful")) { k = replaceSuffix(b, k0, k, "ful", ""); break; }
                break;
            case 's':
                if (endsWith(b, k0, k, "ness")) { k = replaceSuffix(b, k0, k, "ness", ""); break; }
                break;
        }

        if (k < k0) return new String(b, 0, Math.max(0, k + 1));

        // Step 4
        switch (b[k]) {
            case 'l': case 'a':
                if (endsWith(b, k0, k, "al")) k = step4Remove(b, k0, k, "al");
                break;
            case 'c':
                if (endsWith(b, k0, k, "ance")) k = step4Remove(b, k0, k, "ance");
                else if (endsWith(b, k0, k, "ence")) k = step4Remove(b, k0, k, "ence");
                else if (endsWith(b, k0, k, "ic")) k = step4Remove(b, k0, k, "ic");
                break;
            case 'e':
                if (endsWith(b, k0, k, "er")) k = step4Remove(b, k0, k, "er");
                else if (endsWith(b, k0, k, "able")) k = step4Remove(b, k0, k, "able");
                else if (endsWith(b, k0, k, "ible")) k = step4Remove(b, k0, k, "ible");
                break;
            case 'n':
                if (endsWith(b, k0, k, "ant")) k = step4Remove(b, k0, k, "ant");
                else if (endsWith(b, k0, k, "ement")) k = step4Remove(b, k0, k, "ement");
                else if (endsWith(b, k0, k, "ment")) k = step4Remove(b, k0, k, "ment");
                else if (endsWith(b, k0, k, "ent")) k = step4Remove(b, k0, k, "ent");
                else if (endsWith(b, k0, k, "ion") && k >= 3 && (b[k - 3] == 's' || b[k - 3] == 't')) k = step4Remove(b, k0, k, "ion");
                break;
            case 's':
                if (endsWith(b, k0, k, "ism")) k = step4Remove(b, k0, k, "ism");
                break;
            case 't':
                if (endsWith(b, k0, k, "ate")) k = step4Remove(b, k0, k, "ate");
                else if (endsWith(b, k0, k, "iti")) k = step4Remove(b, k0, k, "iti");
                break;
            case 'u':
                if (endsWith(b, k0, k, "ou")) k = step4Remove(b, k0, k, "ou");
                else if (endsWith(b, k0, k, "ous")) k = step4Remove(b, k0, k, "ous");
                break;
            case 'v':
                if (endsWith(b, k0, k, "ive")) k = step4Remove(b, k0, k, "ive");
                break;
            case 'z':
                if (endsWith(b, k0, k, "ize")) k = step4Remove(b, k0, k, "ize");
                break;
        }

        if (k < k0) return new String(b, 0, Math.max(0, k + 1));

        // Step 5a
        if (b[k] == 'e') {
            int a = m(b, k0, k - 1);
            if (a > 1 || (a == 1 && !cvc(b, k0, k - 1))) {
                k--;
            }
        }

        // Step 5b
        if (b[k] == 'l' && doubleConsonant(b, k0, k) && m(b, k0, k - 1) > 1) {
            k--;
        }

        return new String(b, 0, k + 1);
    }

    private boolean isConsonant(char[] b, int i) {
        switch (b[i]) {
            case 'a': case 'e': case 'i': case 'o': case 'u': return false;
            case 'y': return (i == 0) || !isConsonant(b, i - 1);
            default: return true;
        }
    }

    private int m(char[] b, int k0, int k) {
        int n = 0;
        int i = k0;
        while (true) {
            if (i > k) return n;
            if (isConsonant(b, i)) break;
            i++;
        }
        i++;
        while (true) {
            while (true) {
                if (i > k) return n;
                if (!isConsonant(b, i)) break;
                i++;
            }
            i++;
            n++;
            while (true) {
                if (i > k) return n;
                if (isConsonant(b, i)) break;
                i++;
            }
            i++;
        }
    }

    private boolean vowelInStem(char[] b, int k0, int k) {
        for (int i = k0; i <= k; i++) {
            if (!isConsonant(b, i)) return true;
        }
        return false;
    }

    private boolean doubleConsonant(char[] b, int k0, int i) {
        if (i < k0 + 1) return false;
        if (b[i] != b[i - 1]) return false;
        return isConsonant(b, i);
    }

    private boolean cvc(char[] b, int k0, int i) {
        if (i < k0 + 2 || !isConsonant(b, i) || isConsonant(b, i - 1) || !isConsonant(b, i - 2)) return false;
        int ch = b[i];
        return ch != 'w' && ch != 'x' && ch != 'y';
    }

    private boolean endsWith(char[] b, int k0, int k, String s) {
        int l = s.length();
        int o = k - l + 1;
        if (o < k0) return false;
        for (int i = 0; i < l; i++) {
            if (b[o + i] != s.charAt(i)) return false;
        }
        return true;
    }

    private void setEnd(char[] b, int k0, int k, String s) {
        int l = s.length();
        int o = k - l + 1;
        for (int i = 0; i < l; i++) {
            b[o + i] = s.charAt(i);
        }
    }

    private int getNewLength(char[] b, int k0, int k, String s) {
        return k - 3 + s.length();
    }

    private int replaceSuffix(char[] b, int k0, int k, String oldSuffix, String newSuffix) {
        if (m(b, k0, k - oldSuffix.length()) > 0) {
            String current = new String(b, 0, k + 1);
            String stem = current.substring(0, current.length() - oldSuffix.length());
            String replaced = stem + newSuffix;
            char[] repChars = replaced.toCharArray();
            System.arraycopy(repChars, 0, b, 0, repChars.length);
            return repChars.length - 1;
        }
        return k;
    }

    private int step4Remove(char[] b, int k0, int k, String suffix) {
        if (m(b, k0, k - suffix.length()) > 1) {
            return k - suffix.length();
        }
        return k;
    }

    private int appendStr(char[] b, int k, String str) {
        for (int i = 0; i < str.length(); i++) {
            b[k + 1 + i] = str.charAt(i);
        }
        return k + str.length();
    }
}
