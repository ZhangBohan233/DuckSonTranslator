package trashsoftware.duckSonTranslator.grammar;

import trashsoftware.duckSonTranslator.dict.DictMaker;

import java.io.IOException;
import java.util.*;

public class GrammarDict {
    public final Map<String, GrammarEffect> tenseByChs = new HashMap<>();  // 键为中文
    public final Map<String, Map<String, GrammarEffect>> tenseByEng = new HashMap<>();  // {tenseMark: {pos: grammar}}
    private int maxKeyLength;
    private int maxEngCombLength;

    public GrammarDict() throws IOException {
        List<String[]> lines =
                DictMaker.readCsv(DictMaker.class.getResourceAsStream("grammar.csv"));

        for (String[] line : lines) {
            String[] keyWords = line[0].split(";");
            for (int i = 0; i < keyWords.length; i++) {
                String keyWord = keyWords[i].strip();
                keyWords[i] = keyWord;
                if (keyWord.length() > maxKeyLength) maxKeyLength = keyWord.length();
            }
            String engDirect = line[1].strip();
            
            String tenseName = line[2].strip();
            int effectiveIndex = Integer.parseInt(line[3].strip());

            Set<String> partOfSpeech = new HashSet<>();
            String[] poses = line[4].split(";");
            for (String pos : poses) {
                partOfSpeech.add(pos.strip());
            }

            Map<String, String[][]> preCombos = analyzeCombo(line[5]);
            Map<String, String[][]> postCombos = analyzeCombo(line[6]);
            
            GrammarEffect grammarEffect = new GrammarEffect(
                    keyWords[0],
                    engDirect,
                    tenseName,
                    effectiveIndex,
                    partOfSpeech,
                    preCombos,
                    postCombos
            );
            if (line.length >= 8) {
                addKwargs(line[7], grammarEffect);
            }
            for (String keyWord : keyWords) {
                tenseByChs.put(keyWord, grammarEffect);
            }
            Map<String, GrammarEffect> tensesOfThisMark = tenseByEng.computeIfAbsent(tenseName, k -> new TreeMap<>());
            // 多个pos可用的情况下就都加
            for (String pos : partOfSpeech) {
                tensesOfThisMark.put(pos, grammarEffect);
            }
        }
    }

    private Map<String, String[][]> analyzeCombo(String part) {
        Map<String, String[][]> combos = new HashMap<>();
        String[] pre = part.split(";");
        for (String p : pre) {
            p = p.strip();
            if (p.isEmpty()) continue;
            String[] chsEng = p.split(":");
            String[] pp = chsEng[1].strip().split(" ");
            String[][] res = new String[pp.length][2];
            for (int i = 0; i < pp.length; i++) {
                String ppp = pp[i];
                String[] posWord = ppp.strip().split("\\.");
                res[i][0] = posWord[1].strip();
                res[i][1] = posWord[0].strip();
            }
            if (res.length > maxEngCombLength) maxEngCombLength = res.length;
            combos.put(chsEng[0].strip(), res);
        }
        return combos;
    }

    public int getMaxKeyLength() {
        return maxKeyLength;
    }
    
    public int getMaxEngCombLength() {
        return maxEngCombLength;
    }
    
    private void addKwargs(String argText, GrammarEffect grammarEffect) {
        if (argText == null) return;
        argText = argText.strip();
        if (argText.isEmpty()) return;
        String[] kwargs = argText.split(";");
        for (String kwarg : kwargs) {
            kwarg = kwarg.strip();
            String[] ka = kwarg.split("=");
            if (ka.length == 2) {
                String key = ka[0].strip();
                String val = ka[1].strip();
                if ("ignores".equalsIgnoreCase(key)) {
                    // ignore
                    String[] ignoresLine = val.split("/");
                    for (String ig : ignoresLine) {
                        grammarEffect.ignoresLanguages.add(ig.strip());
                    }
                }
            }
        }
    }
}
