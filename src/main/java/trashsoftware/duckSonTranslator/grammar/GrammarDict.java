package trashsoftware.duckSonTranslator.grammar;

import trashsoftware.duckSonTranslator.dict.DictMaker;

import java.io.IOException;
import java.util.*;

public class GrammarDict {
    public final Map<String, GrammarEffect> tenseInfo = new HashMap<>();
    private int maxKeyLength;

    public GrammarDict() throws IOException {
        List<String[]> lines =
                DictMaker.readCsv(DictMaker.class.getResourceAsStream("grammar.csv"));

        for (String[] line : lines) {
            String keyWord = line[0].strip();
            if (keyWord.length() > maxKeyLength) maxKeyLength = keyWord.length(); 
            String tenseName = line[1].strip();
            int effectiveIndex = Integer.parseInt(line[2].strip());

            Set<String> partOfSpeech = new HashSet<>();
            String[] poses = line[3].split(";");
            for (String pos : poses) {
                partOfSpeech.add(pos.strip());
            }

            Map<String, String[][]> preCombos = analyzeCombo(line[4]);
            Map<String, String[][]> postCombos = analyzeCombo(line[5]);

            GrammarEffect grammarEffect = new GrammarEffect(
                    tenseName,
                    effectiveIndex,
                    partOfSpeech,
                    preCombos,
                    postCombos
            );
            tenseInfo.put(keyWord, grammarEffect);
        }
    }

    private static Map<String, String[][]> analyzeCombo(String part) {
        Map<String, String[][]> combos = new HashMap<>();
        String[] pre = part.split(";");
        for (String p : pre) {
            p = p.strip();
            if (p.isEmpty()) continue;
            String[] chsEng = p.split(":");
            String[] pp = chsEng[1].strip().split("\s");
            String[][] res = new String[pp.length][2];
            for (int i = 0; i < pp.length; i++) {
                String ppp = pp[i];
                String[] posWord = ppp.strip().split("\\.");
                res[i][0] = posWord[1].strip();
                res[i][1] = posWord[0].strip();
            }
            combos.put(chsEng[0].strip(), res);
        }
        return combos;
    }

    public int getMaxKeyLength() {
        return maxKeyLength;
    }
}
