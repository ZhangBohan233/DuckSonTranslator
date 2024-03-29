package trashsoftware.duckSonTranslator.result;

import trashsoftware.duckSonTranslator.dict.Util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResultToken implements Serializable {
    
    private String translated;
    private final List<int[]> origRanges = new ArrayList<>();
    
    public ResultToken(String translated, int origIndex, int origLength) {
        this(translated, List.of(new int[]{origIndex, origIndex + origLength}));
    }
    
    public ResultToken(String translated, List<int[]> ranges) {
        this.translated = translated;
        this.origRanges.addAll(ranges);
    }

    public ResultToken(String translated, List<int[]> ranges, int origIndex, int origLength) {
        this(translated, ranges);
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

    @Override
    public String toString() {
        return translated;
    }
    
    public void print() {
        System.out.print(translated + " " + Util.listOfArrayToString(origRanges));
    }
}
