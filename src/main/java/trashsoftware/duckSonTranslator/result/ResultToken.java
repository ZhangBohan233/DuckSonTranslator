package trashsoftware.duckSonTranslator.result;

import trashsoftware.duckSonTranslator.dict.Util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ResultToken implements Serializable {

    private String translated;
    private final String partOfSpeech;
    private final List<int[]> origRanges = new ArrayList<>();

    public ResultToken(String translated, String partOfSpeech, int origIndex, int origLength) {
        this(translated, partOfSpeech, List.of(new int[]{origIndex, origIndex + origLength}));
    }

    public ResultToken(String translated, String partOfSpeech, List<int[]> ranges) {
        this.translated = translated;
        this.partOfSpeech = partOfSpeech;
        this.origRanges.addAll(ranges);
//        if (partOfSpeech == null) throw new RuntimeException(translated);
    }

    public ResultToken(String translated, String partOfSpeech, List<int[]> ranges, int origIndex, int origLength) {
        this(translated, partOfSpeech, ranges);
        origRanges.add(new int[]{origIndex, origIndex + origLength});
    }

    public void setTranslated(String translated) {
        this.translated = translated;
    }

    public void addRange(int origIndex, int origLength) {
        origRanges.add(new int[]{origIndex, origIndex + origLength});
    }

    public void addAllRanges(List<int[]> ranges) {
        origRanges.addAll(ranges);
    }

    public List<int[]> getOrigRanges() {
        return origRanges;
    }

    public String getTranslated() {
        return translated;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    @Override
    public String toString() {
        return translated;
    }
    
    public boolean needSpaceBetween(ResultToken after) {
        if (translated.isEmpty()) return false;
        if (Util.isChineseByRange(translated.charAt(translated.length() - 1))) return false;
        if (after.translated.isEmpty()) return false;
        if (Util.isChineseByRange(after.translated.charAt(after.translated.length() - 1))) return false;
        return true;
    }

    public void print() {
        System.out.print(translated + ": " + partOfSpeech + " " + Util.listOfArrayToString(origRanges));
    }
}
