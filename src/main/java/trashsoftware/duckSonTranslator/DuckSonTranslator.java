package trashsoftware.duckSonTranslator;

import trashsoftware.duckSonTranslator.dict.*;
import trashsoftware.duckSonTranslator.grammar.GrammarDict;
import trashsoftware.duckSonTranslator.grammar.GrammarEffect;
import trashsoftware.duckSonTranslator.grammar.Token;
import trashsoftware.duckSonTranslator.translators.NoSuchWordException;

import java.io.IOException;
import java.util.*;

public class DuckSonTranslator {
    public static final String CORE_VERSION = "0.2.1";

    public static final Set<String> NOT_BREAK_WORD = Set.of(
            "pun", "unk"
    );
    private static final Map<Character, Character> PUNCTUATIONS_REGULAR = Map.of(
            '，', ',', '。', '.', '：', ':', '；', ';',
            '！', '!', '？', '?', '、', ',', '·', ' '
    );
    private static final Map<Character, Character> PUNCTUATIONS_QUOTE = Map.of(
            '“', '"', '”', '"', '‘', '\'', '’', '\'',
            '《', '"', '》', '"', '【', '[', '】', ']',
            '『', '"', '』', '"'
    );
    public static final Map<Character, Character> CHS_PUNCTUATIONS = DictMaker.mergeMaps(
            PUNCTUATIONS_REGULAR, PUNCTUATIONS_QUOTE
    );
    public static final Map<Character, Character> ENG_PUNCTUATIONS =
            DictMaker.invertMap(CHS_PUNCTUATIONS);
    private final BaseDict baseDict;
    private final PinyinDict pinyinDict;
    private final BigDict bigDict;
    private final GrammarDict grammarDict;
    private boolean singleCharMode;
    private boolean chongqingMode;

    public DuckSonTranslator(boolean singleCharMode, boolean chongqingMode) throws IOException {
        this.baseDict = new BaseDict();
        this.pinyinDict = new PinyinDict();
        this.bigDict = new BigDict();
        this.grammarDict = new GrammarDict();

        this.singleCharMode = singleCharMode;
        this.chongqingMode = chongqingMode;
    }

    public DuckSonTranslator() throws IOException {
        this(true, true);
    }
    
    public String getCoreVersion() {
        return CORE_VERSION;
    }
    
    public String getDictionaryVersion() {
        return baseDict.getVersionStr() + "." + pinyinDict.getVersionStr(); 
    }

    private static String[] splitToN(String orig, int n) {
        if (orig.length() < n) throw new RuntimeException();
        int avg = orig.length() / n;

        String[] res = new String[n];
        int index = 0;
        for (int i = 0; i < n; i++) {
            String part;
            if (i == n - 1) {
                part = orig.substring(index);
            } else {
                part = orig.substring(index, index + avg);
                index += avg;
            }
            res[i] = part;
        }
        return res;
    }

    private static void increase(int[] limits, int[] indices) {
        indices[indices.length - 1]++;

        for (int i = limits.length - 1; i >= 0; i--) {
            if (indices[i] < limits[i]) return;  // 不用进位，杀鸽

            indices[i] = 0;
            indices[i - 1]++;  // 进位，不管溢出，让它报错
        }
    }

    private static String[][][] makeCombinations(List<String[]>[] possibles) {
        int ways = 1;
        int[] limits = new int[possibles.length];
        int[] indices = new int[possibles.length];
        for (int i = 0; i < possibles.length; i++) {
            List<String[]> p = possibles[i];
            ways *= p.size();
            limits[i] = p.size();
        }

        String[][][] result = new String[ways][possibles.length][];
        for (int i = 0; i < ways; i++) {
            for (int bitPos = 0; bitPos < indices.length; bitPos++) {
                result[i][bitPos] = possibles[bitPos].get(indices[bitPos]);
            }
            if (i < ways - 1) increase(limits, indices);
        }
        return result;
    }

    public String autoDetectLanguage(String input) {
        int totalLen = input.length();
        int chsCount = 0;
        int engCount = 0;
        int othersCount = 0;

        for (char c : input.toCharArray()) {
            if (c >= 'A' && c <= 'z') engCount++;
            else if (ENG_PUNCTUATIONS.containsKey(c)) engCount++;
            else if (pinyinDict.getPinyinByChs(c) != null) chsCount++;
            else if (CHS_PUNCTUATIONS.containsKey(c)) chsCount++;
            else othersCount++;
        }

        if ((double) chsCount / totalLen > 0.75) return "chs";
        if ((double) engCount / totalLen > 0.75) return "geg";

        int subTotal = chsCount + engCount;
        if ((double) chsCount / subTotal > 0.8) return "chs";
        if ((double) engCount / subTotal > 0.8) return "geg";

        return "unk";
    }

    public void setSingleCharMode(boolean singleCharMode) {
        this.singleCharMode = singleCharMode;
    }

    public boolean isSingleCharMode() {
        return singleCharMode;
    }

    public void setChongqingMode(boolean chongqingMode) {
        this.chongqingMode = chongqingMode;
    }

    public boolean isChongqingMode() {
        return chongqingMode;
    }

    private boolean addSpecial(GrammarEffect ge,
                               String chs,
                               int index,
                               SortedMap<Integer, Token> grammarTokens) {
        for (Map.Entry<String, String[][]> pre : ge.specialPreComb.entrySet()) {
            int beginIndex = index - pre.getKey().length();
            if (beginIndex < 0) continue;
            String sub = chs.substring(beginIndex, index);
            if (sub.equals(pre.getKey())) {
                String ss = sub + chs.charAt(index);
                String[][] desPos = pre.getValue();
                if (desPos.length > 1) {
                    String[] sss = splitToN(ss, desPos.length);
                    int acc = 0;
                    for (int i = 0; i < sss.length; i++) {
                        Token token = new Token(sss[i], desPos[i][0], desPos[i][1]);
                        grammarTokens.put(beginIndex + acc, token);
                        acc += sss[i].length();
                    }
                } else {
                    grammarTokens.put(beginIndex, new Token(ss, desPos[0][0], desPos[0][1]));
                }
                return true;
            }
        }
        for (Map.Entry<String, String[][]> pre : ge.specialPostComb.entrySet()) {
            int endIndex = index + pre.getKey().length() + 1;
            if (endIndex > chs.length() - 1) continue;
            String sub = chs.substring(index + 1, endIndex);
            if (sub.equals(pre.getKey())) {
                String ss = chs.charAt(index) + sub;
                String[][] desPos = pre.getValue();
                if (desPos.length > 1) {
                    String[] sss = splitToN(ss, desPos.length);
                    int acc = 0;
                    for (int i = 0; i < sss.length; i++) {
                        Token token = new Token(sss[i], desPos[i][0], desPos[i][1]);
                        grammarTokens.put(index + acc, token);
                        acc += sss[i].length();
                    }
                } else {
                    grammarTokens.put(index, new Token(ss, desPos[0][0], desPos[0][1]));
                }
                return true;
            }
        }
        return false;
    }

    public String chsToGeglish(String chs) {
        SortedMap<Integer, Token> grammars = new TreeMap<>();

        // 处理语法token
        for (int i = 0; i < chs.length(); i++) {
            String len1 = chs.substring(i, i + 1);
            String len2;
            GrammarEffect ge;
            if (i < chs.length() - 2) {
                len2 = chs.substring(i, i + 2);
                ge = grammarDict.tenseInfo.get(len2);
                if (ge != null) {
                    if (!addSpecial(ge, chs, i, grammars)) {
                        grammars.put(i, new Token(len2, ge));
                        i += 1;
                        continue;
                    }
                }
            }
            ge = grammarDict.tenseInfo.get(len1);
            if (ge != null) {
                if (!addSpecial(ge, chs, i, grammars)) {
                    grammars.put(i, new Token(len1, ge));
                }
            }
        }

        SortedMap<Integer, Token> origIndexTokens = new TreeMap<>();
        SortedMap<Integer, String> notTranslated = new TreeMap<>();

        StringBuilder notTrans = new StringBuilder();
        StringBuilder numBuilder = new StringBuilder();
        StringBuilder engBuilder = new StringBuilder();

        // 处理直接能翻译的
        int index = 0;
        for (; index < chs.length(); index++) {
            Token grammarToken = grammars.get(index);
            if (grammarToken != null) {
                if (notTrans.length() > 0) {
                    notTranslated.put(index - notTrans.length(), notTrans.toString());
                    notTrans.setLength(0);
                }
                index += grammarToken.getChs().length() - 1;
                continue;
            }

            boolean interrupt = false;
            char c = chs.charAt(index);
            String cs = String.valueOf(c);
            if (c >= '0' && c <= '9') {
                numBuilder.append(c);
                interrupt = true;
            } else {
                if (numBuilder.length() > 0) {
                    String numStr = numBuilder.toString();
                    origIndexTokens.put(index - numStr.length(), new Token(numStr, numStr, "num"));
                    numBuilder.setLength(0);
                }
            }

            if (c >= 'A' && c <= 'z') {
                engBuilder.append(c);
                interrupt = true;
            } else {
                if (engBuilder.length() > 0) {
                    String engStr = engBuilder.toString();
                    origIndexTokens.put(index - engStr.length(), new Token(engStr, engStr, "eng"));
                    engBuilder.setLength(0);
                }
            }

            if (interrupt) {
                if (notTrans.length() > 0) {
                    notTranslated.put(index - notTrans.length(), notTrans.toString());
                    notTrans.setLength(0);
                }
                continue;
            }

            if (c < 128) {  // ASCII
                String pos = "unk";

                Token token = new Token(cs, cs, pos);
                origIndexTokens.put(index, token);
                if (notTrans.length() > 0) {
                    notTranslated.put(index - notTrans.length(), notTrans.toString());
                    notTrans.setLength(0);
                }
                continue;
            }

            Character pun = CHS_PUNCTUATIONS.get(c);
            if (pun != null) {
                Token token = new Token(cs, String.valueOf(pun), "pun");
                origIndexTokens.put(index, token);
                if (notTrans.length() > 0) {
                    notTranslated.put(index - notTrans.length(), notTrans.toString());
                    notTrans.setLength(0);
                }
                continue;
            }

            BaseItem direct = baseDict.getByChs(chs, index);
            if (direct != null) {
                Token token = new Token(direct.chs, direct.eng, direct.partOfSpeech);
                origIndexTokens.put(index, token);
                if (notTrans.length() > 0) {
                    notTranslated.put(index - notTrans.length(), notTrans.toString());
                    notTrans.setLength(0);
                }
                index += direct.chs.length() - 1;
            } else {
                String[] pinyin = pinyinDict.getPinyinByChs(c);
                if (pinyin == null) {
                    throw new NoSuchWordException(cs);
                }
                BaseItem sameSoundWord;
                if (chongqingMode) {
                    sameSoundWord = baseDict.getByCqPin(pinyin);
                } else {
                    sameSoundWord = baseDict.getByPinyin(pinyin);
                }
                
                if (sameSoundWord != null) {
                    Token token = new Token(cs, sameSoundWord.eng, sameSoundWord.partOfSpeech);
                    origIndexTokens.put(index, token);
                    if (notTrans.length() > 0) {
                        notTranslated.put(index - notTrans.length(), notTrans.toString());
                        notTrans.setLength(0);
                    }
                } else {  // 去真的英语翻译
                    notTrans.append(c);
                }
            }
        }
        if (notTrans.length() > 0) {
            notTranslated.put(index - notTrans.length(), notTrans.toString());
            notTrans.setLength(0);
        }
        if (numBuilder.length() > 0) {
            String numStr = numBuilder.toString();
            origIndexTokens.put(index - numStr.length(), new Token(numStr, numStr, "num"));
            numBuilder.setLength(0);
        }
        if (engBuilder.length() > 0) {
            String engStr = engBuilder.toString();
            origIndexTokens.put(index - engStr.length(), new Token(engStr, engStr, "eng"));
            engBuilder.setLength(0);
        }

//        System.out.println(origIndexTokens);
//        System.out.println(notTranslated);

        // 开始真正的英语翻译
        List<Token> tokens = new ArrayList<>();
        for (int i = 0; i < chs.length(); i++) {
            Token tk = origIndexTokens.get(i);
            if (tk != null) {
                tokens.add(tk);
                i += tk.getChs().length() - 1;
                continue;
            }
            tk = grammars.get(i);
            if (tk != null) {
                tokens.add(tk);
                i += tk.getChs().length() - 1;
                continue;
            }
            String notTransSeg = notTranslated.get(i);
            if (notTransSeg != null) {
                if (singleCharMode) {
                    bigDictTransSingleChar(notTransSeg, tokens);
                } else {
                    bigDictTrans(notTransSeg, tokens);
                }
            }
        }
        applyGrammar(tokens);

        return integrateToGeglish(tokens);
    }

    public String geglishToChs(String geglish) {
        String[] baseWords = geglish.strip().split(" ");
        List<Token> tokens = deriveGeglishTokens(baseWords);

        tokens = translateCombineTokensGegToChs(tokens);

        for (Token token : tokens) {
            if (token.isUntranslatedEng()) {
                translateTokenGegToChs(token);
            }
        }
//        System.out.println(tokens);
        tokens = insertTokensByGrammar(tokens);
//        System.out.println(tokens);
        return integrateChsTokens(tokens);
    }

    private List<Token> deriveGeglishTokens(String[] baseWords) {
        List<Token> words = new ArrayList<>();
        for (String baseWord : baseWords) {
            StringBuilder word = new StringBuilder();
            StringBuilder number = new StringBuilder();
            for (char c : baseWord.toCharArray()) {
                if (c >= 'A' && c <= 'z') {
                    if (number.length() > 0) {
                        String nonWordS = number.toString();
                        number.setLength(0);
                        words.add(new Token(nonWordS, nonWordS, "num"));
                    }
                    word.append(c);
                } else {
                    if (word.length() > 0) {
                        words.add(new Token(word.toString()));
                        word.setLength(0);
                    }
                    if (c >= '0' && c <= '9') {
                        number.append(c);
                    } else {
                        if (number.length() > 0) {
                            String nonWordS = number.toString();
                            number.setLength(0);
                            words.add(new Token(nonWordS, nonWordS, "num"));
                        }
                        Character pun = ENG_PUNCTUATIONS.get(c);
                        if (pun != null) {
                            words.add(new Token(String.valueOf(pun), String.valueOf(c), "pun"));
                        } else {
                            words.add(new Token(String.valueOf(c), String.valueOf(c), "unk"));
                        }
                    }

                }
            }
            if (number.length() > 0) {
                String nonWordS = number.toString();
                words.add(new Token(nonWordS, nonWordS, "num"));
            }
            if (word.length() > 0) {
                words.add(new Token(word.toString()));
            }
        }
        return words;
    }

    @SuppressWarnings("unchecked")
    private List<Token> translateCombineTokensGegToChs(List<Token> tokens) {
        List<Token> newTokens = new ArrayList<>();
        OUT_LOOP:
        for (int i = 0; i < tokens.size(); i++) {
            for (int j = i + 1; j < Math.min(tokens.size(), i + grammarDict.getMaxEngCombLength()); j++) {
                List<String[]>[] possibles = new List[j - i + 1];
                for (int k = i; k <= j; k++) {
                    List<String[]> possiblesAtK = new ArrayList<>();
                    possibles[k - i] = possiblesAtK;
                    Token token = tokens.get(k);
                    possiblesAtK.add(new String[]{token.getEng(), ""});

                    String[][] possibleForms = token.getPossibleBaseEngForm();
                    if (possibleForms != null) Collections.addAll(possiblesAtK, possibleForms);
                }
                String[][][] permutation = makeCombinations(possibles);

                for (Map.Entry<String, GrammarEffect> entry : grammarDict.tenseInfo.entrySet()) {
                    GrammarEffect effect = entry.getValue();
                    for (Map.Entry<String[][], String> savedCombo : effect.combsEngChs.entrySet()) {
                        String[][] engPos = savedCombo.getKey();
                        if (engPos.length == possibles.length) {
                            POSS_LOOP:
                            for (String[][] poss : permutation) {
                                for (int kk = 0; kk < poss.length; kk++) {
                                    if (!poss[kk][0].equals(engPos[kk][0])) {
                                        continue POSS_LOOP;
                                    }
                                }
//                                System.out.println("Match!" + comb.length + Arrays.deepToString(engPos) + Arrays.deepToString(poss));

                                String[] chsSplit = splitToN(savedCombo.getValue(), engPos.length);
//                                System.out.println(Arrays.toString(chsSplit));
                                boolean isPost = effect.isPost(savedCombo.getValue());
                                if (isPost) {
                                    for (int x = 0; x < engPos.length - 2; x++) {
                                        newTokens.remove(newTokens.size() - 1);
                                    }
                                    i += 1;
                                } else {
                                    i += engPos.length - 1;
                                }

                                for (int x = 0; x < engPos.length; x++) {
                                    Token tk = new Token(chsSplit[x], engPos[x][0], engPos[x][1]);
                                    if (!poss[x][1].isEmpty()) tk.addTense(poss[x][1]);

                                    newTokens.add(tk);
                                }

                                continue OUT_LOOP;
                            }
                        }
                    }
                }
            }
            newTokens.add(tokens.get(i));
        }
        return newTokens;
    }

    private void translateTokenGegToChs(Token token) {
        // 检查baseDict
        String eng = token.getEng();
        BaseItem baseItem = baseDict.getByEng(eng);
        if (baseItem != null) {
            token.setChs(baseItem.chs);
            token.setPartOfSpeech(baseItem.partOfSpeech);
            return;
        }
        String[][] possibleForms = token.getPossibleBaseEngForm();
        if (possibleForms != null) {
            for (String[] engTense : possibleForms) {
                baseItem = baseDict.getByEng(engTense[0]);
                if (baseItem != null) {
                    token.setChs(baseItem.chs);
                    token.setPartOfSpeech(baseItem.partOfSpeech);
                    token.addTense(engTense[1]);
                    return;
                }
            }
        }

        // 检查bigDict
        BigDict.ChsResult chsDirect = bigDict.translateEngToChs(eng);
        if (chsDirect != null) {
            token.setChs(chsDirect.translated);
            token.setPartOfSpeech(chsDirect.partOfSpeech);
            return;
        }
        if (possibleForms != null) {
            for (String[] engTense : possibleForms) {
                chsDirect = bigDict.translateEngToChs(engTense[0]);
                if (chsDirect != null) {
                    token.setChs(chsDirect.translated);
                    token.setPartOfSpeech(chsDirect.partOfSpeech);
                    token.addTense(engTense[1]);
                    return;
                }
            }
        }
    }

    private List<Token> insertTokensByGrammar(List<Token> tokens) {
        List<Token> tokensAfterGrammar = new ArrayList<>();
        for (Token token : tokens) {
            if (token.getTenses().isEmpty()) {
                tokensAfterGrammar.add(token);
            } else {
//                System.out.println(token);
                tokensAfterGrammar.addAll(token.applyTenseToChs(grammarDict));
            }
        }
        return tokensAfterGrammar;
    }

    private String integrateChsTokens(List<Token> tokens) {
        StringBuilder builder = new StringBuilder();
        for (Token token : tokens) {
            if (token.isActual()) {
                if (token.getChs() != null) builder.append(token.getChs());
                else builder.append(token.getEng());
            } else {
                builder.append(token.getChs());
            }
        }
        return builder.toString();
    }

    private void bigDictTrans(String notTransSeg, List<Token> tokens) {
        while (!notTransSeg.isEmpty()) {
            BigDict.Result match = bigDict.translateOneWord(notTransSeg);
            if (match != null) {
                String thisWord = notTransSeg.substring(0, match.matchLength);

                Token token = new Token(thisWord, match.translated, match.partOfSpeech);
                tokens.add(token);

                notTransSeg = notTransSeg.substring(match.matchLength);
            } else {
                char cur = notTransSeg.charAt(0);
                tokens.add(bigDictSameSoundTrans(cur));

                notTransSeg = notTransSeg.substring(1);
            }
        }
    }

    private void bigDictTransSingleChar(String notTransSeg, List<Token> tokens) {
        for (char c : notTransSeg.toCharArray()) {
            BigDict.Result result = bigDict.translateOneCharChsEng(c);
            if (result != null) {
                tokens.add(new Token(String.valueOf(c), result.translated, result.partOfSpeech));
            } else {
                tokens.add(bigDictSameSoundTrans(c));
            }
        }
    }

    private Token bigDictSameSoundTrans(char cur) {
        // 遍历同音字查询，只查一个字
        String[] pinyin = pinyinDict.getPinyinByChs(cur);
        List<Character> sameSound = pinyinDict.getChsListByCqPin(getPin(pinyin));
        Token tk = pickSameSoundWord(sameSound);
        if (tk == null) {
            String cs = String.valueOf(cur);
            String py = getPinNoTone(pinyin);
            return new Token(cs, py, "n");
        } else {
            return tk;
        }
    }
    
    private String getPin(String[] pinyin) {
        return chongqingMode ? pinyin[1] : pinyin[0];
    }
    
    private String getPinNoTone(String[] pinyin) {
        String pin = getPin(pinyin);
        char tone = pin.charAt(pin.length() - 1);
        if (tone >= '0' && tone <= '4') {
            return pin.substring(0, pin.length() - 1);
        } else {
            return pin;
        }
    }

    private Token pickSameSoundWord(List<Character> chsChars) {
        int minLen = Integer.MAX_VALUE;
        String minChs = null;
        BigDict.Result minVal = null;
        for (Character c : chsChars) {
            String s = String.valueOf(c);
            BigDict.Result result = bigDict.translateOneWord(s);
            if (result != null && result.translated.length() < minLen) {
                minLen = result.translated.length();
                minChs = s;
                minVal = result;
            }
        }
        if (minVal == null) return null;
        return new Token(minChs, minVal.translated, minVal.partOfSpeech);
    }

    private String integrateToGeglish(List<Token> tokens) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.isActual()) {
                builder.append(token.getEng());
                Token next = findNextActual(tokens, i);
                if (next != null && !NOT_BREAK_WORD.contains(next.getPartOfSpeech())) {
                    builder.append(' ');
                }
            }
        }
        return builder.toString();
    }

    private Token findNextActual(List<Token> tokens, int currentIndex) {
        for (int i = currentIndex + 1; i < tokens.size(); i++) {
            Token next = tokens.get(i);
            if (next.isActual()) return next;
        }
        return null;
    }

    private void applyGrammar(List<Token> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (!token.isActual()) {
                GrammarEffect effect = token.getGrammarEffect();
                if (effect.effectiveIndex < 0) {  // 往过去找
                    int rem = -effect.effectiveIndex;
                    for (int j = i - 1; j >= 0; j--) {
                        Token tk = tokens.get(j);
                        if (tk.isActual() && effect.effectivePos.contains(tk.getPartOfSpeech())) {
                            rem -= 1;
                            if (rem == 0) {
                                tk.applyTense(effect.tenseName);
                                break;
                            }
                        }
                    }
                } else if (effect.effectiveIndex > 0) {  // 往未来找
                    int rem = effect.effectiveIndex;
                    for (int j = i + 1; j < tokens.size(); j++) {
                        Token tk = tokens.get(j);
                        if (tk.isActual() && effect.effectivePos.contains(tk.getPartOfSpeech())) {
                            rem -= 1;
                            if (rem == 0) {
                                tk.applyTense(effect.tenseName);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
}
