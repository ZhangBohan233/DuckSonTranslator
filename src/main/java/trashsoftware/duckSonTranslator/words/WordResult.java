package trashsoftware.duckSonTranslator.words;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class WordResult implements Serializable, Comparable<WordResult> {

    private final String original;
    private final String srcLang;
    private final String dstLang;
    private final String dst;
    private final boolean fromSameSound;
    private final Map<String, LinkedHashSet<String>> posDescription = new HashMap<>();

    WordResult(String original, String translated, String srcLang, String dstLang, boolean fromSameSound) {
        this.original = original;
        this.dst = translated;
        this.srcLang = srcLang;
        this.dstLang = dstLang;
        this.fromSameSound = fromSameSound;
    }

    public boolean isFromSameSound() {
        return fromSameSound;
    }

    public String getDst() {
        return dst;
    }

    public String getOriginal() {
        return original;
    }

    public String getSrcLang() {
        return srcLang;
    }

    public String getDstLang() {
        return dstLang;
    }

    @Override
    public int compareTo(WordResult o) {
        if (this.fromSameSound && !o.fromSameSound) return 1;
        if (!this.fromSameSound && o.fromSameSound) return -1;
        return 0;
    }

    public Map<String, LinkedHashSet<String>> getPosDescription() {
        return posDescription;
    }

    public void addPosDescription(Map<String, LinkedHashSet<String>> posDescription) {
        for (var entry : posDescription.entrySet()) {
            LinkedHashSet<String> existing = this.posDescription.get(entry.getKey());
            if (existing == null) {
                this.posDescription.put(entry.getKey(), entry.getValue());
            } else {
                existing.addAll(entry.getValue());
            }
        }
    }

    @Override
    public String toString() {
        return "WordResult{" +
                "original='" + original + '\'' +
                ", srcLang='" + srcLang + '\'' +
                ", dstLang='" + dstLang + '\'' +
                ", dst='" + dst + '\'' +
                ", posDescription=" + posDescription +
                '}';
    }
}
