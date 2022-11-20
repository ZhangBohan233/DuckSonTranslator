package trashsoftware.duckSonTranslator.grammar;

import trashsoftware.duckSonTranslator.dict.DictMaker;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChsToGegCase extends CaseAnalyzer {
    private static ChsToGegCase analyzer;

    private final Map<String, PronGroup> pronGroupMap = new HashMap<>();

    private ChsToGegCase() throws IOException {
        List<String[]> lines = DictMaker.readDictCsv("pronForms.csv", false, false);
        for (String[] line : lines) {
            if (line.length != 3) throw new RuntimeException();
            
            PronGroup group = new PronGroup(line[0], line[1], line[2]);
            pronGroupMap.put(line[0], group);
            pronGroupMap.put(line[1], group);
            pronGroupMap.put(line[2], group);
        }
    }

    public static ChsToGegCase getInstance() throws IOException {
        if (analyzer == null) {
            analyzer = new ChsToGegCase();
        }
        return analyzer;
    }

    /**
     * 将时态应用至中间的token
     * <p>
     * 注意：before和after都可以是null，意思是说token list有多长，这个循环就能跑多少遍，不存在-2的情况。
     */
    public void applyToMiddleToken(Token before, Token middle, Token after) {
        String engOrig = middle.getOrigEng();
        PronGroup pronGroup = pronGroupMap.get(engOrig);
        if (pronGroup == null) return;
        
        if (middle.appliedTenses.contains("belong")) {
            middle.setEngAfterTense(pronGroup.hisForm);
            return;
        }
        String form = pronFormChs(before, after);
        switch (form) {
            case "subject":
                middle.setEngAfterTense(pronGroup.subjectForm);
                break;
            case "object":
                middle.setEngAfterTense(pronGroup.objectForm);
                break;
            case "his":
                middle.setEngAfterTense(pronGroup.hisForm);
                break;
        }
    }

    private String pronFormChs(Token lastActual, Token nextActual) {
        if (lastActual != null) {
            if ("v".equals(lastActual.getPartOfSpeech()) || 
                    "adj".equals(lastActual.getPartOfSpeech())) {
                return "object";
            }
        }
        if (nextActual != null) {
            if ("v".equals(nextActual.getPartOfSpeech())) {
                return "subject";
            } else if ("n".equals(nextActual.getPartOfSpeech())) {
                return "his";
            }
        }
        return "";
    }
}
