package trashsoftware.duckSonTranslator.words;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class WordResult implements Serializable, Comparable<WordResult> {

    private final String searchedOrig;  // 用户搜的什么
    private final String firstOriginal;
    private final String srcLang;
    private final String dstLang;
    private final String dst;
    private final WordResultType type;
    private final Map<String, LinkedHashSet<String>> posDescription = new HashMap<>();
    
    transient double origWordPurity;  // 搜的词占最好的反向解释的长度的比例
    transient double bestPosPurity = -1;  // 最合适的词性的解释中含有目标词的词条数量比例
    boolean computed = false;

    WordResult(String searchedOrig, 
               String firstOriginal, 
               String translated, 
               String srcLang, 
               String dstLang, 
               WordResultType type) {
        this.searchedOrig = searchedOrig;
        this.firstOriginal = firstOriginal;
        this.dst = translated;
        this.srcLang = srcLang;
        this.dstLang = dstLang;
        this.type = type;
    }
    
    private void computePurities() {
        origWordPurity = (double) searchedOrig.length() / firstOriginal.length();  // 初始值
        for (var posDes : posDescription.entrySet()) {
            int matched = 0;
            for (String s : posDes.getValue()) {
                if (s.contains(searchedOrig)) {
                    matched++;
                    double lengthPurity = (double) searchedOrig.length() / s.length();
                    if (lengthPurity > origWordPurity) {
//                        System.out.println("Update to " + s);
                        origWordPurity = lengthPurity;
                    }
                }
            }
            double purity = (double) matched / posDes.getValue().size();
            if (purity > bestPosPurity) {
                bestPosPurity = purity;
            }
        }
        computed = true;
    }

    public WordResultType getType() {
        return type;
    }

    public String getDst() {
        return dst;
    }

    public String getFirstOriginal() {
        return firstOriginal;
    }

    public String getSrcLang() {
        return srcLang;
    }

    public String getDstLang() {
        return dstLang;
    }

    /**
     * 好的return -1
     */
    @Override
    public int compareTo(WordResult o) {
        if (type != o.type) {
            return Integer.compare(type.ordinal(), o.type.ordinal());
        }
        
        if (!this.computed) this.computePurities();
        if (!o.computed) o.computePurities();
        
        int origPurityCmp = Double.compare(this.origWordPurity, o.origWordPurity);
        if (origPurityCmp != 0) return -origPurityCmp;
        
        int posPurityCmp = Double.compare(this.bestPosPurity, o.bestPosPurity);
        if (posPurityCmp != 0) return -posPurityCmp;
        
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
                searchedOrig +
                ", original='" + firstOriginal + '\'' +
                ", srcLang='" + srcLang + '\'' +
                ", dstLang='" + dstLang + '\'' +
                ", dst='" + dst + '\'' +
                ", type=" + type + 
                ", posDescription=" + posDescription +
                '}';
    }
}
