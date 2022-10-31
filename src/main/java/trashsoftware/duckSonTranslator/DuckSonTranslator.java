package trashsoftware.duckSonTranslator;

import trashsoftware.duckSonTranslator.dict.BaseDict;
import trashsoftware.duckSonTranslator.dict.BaseItem;
import trashsoftware.duckSonTranslator.dict.BigDict;
import trashsoftware.duckSonTranslator.dict.PinyinDict;
import trashsoftware.duckSonTranslator.grammar.GrammarDict;
import trashsoftware.duckSonTranslator.grammar.GrammarEffect;
import trashsoftware.duckSonTranslator.grammar.Token;
import trashsoftware.duckSonTranslator.translators.NoSuchWordException;

import java.io.IOException;
import java.util.*;

public class DuckSonTranslator {

    public static final Map<Character, Character> PUNCTUATIONS = Map.of(
            '，', ',', '。', '.', '：', ':', '；', ';',
            '“', '"', '”', '"', '‘', '\'', '’', '\''
    );

    private final BaseDict baseDict;
    private final PinyinDict pinyinDict;
    private final BigDict bigDict;
    private final GrammarDict grammarDict;
    private boolean singleCharMode;

    public DuckSonTranslator(boolean singleCharMode) throws IOException {
        this.baseDict = new BaseDict();
        this.pinyinDict = new PinyinDict();
        this.bigDict = new BigDict();
        this.grammarDict = new GrammarDict();
        
        this.singleCharMode = singleCharMode;
    }
    
    public DuckSonTranslator() throws IOException {
        this(true);
    }

    public void setSingleCharMode(boolean singleCharMode) {
        this.singleCharMode = singleCharMode;
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

        int index = 0;
        for (; index < chs.length(); index++) {
            Token grammarToken = grammars.get(index);
            if (grammarToken != null) {
                if (!notTrans.isEmpty()) {
                    notTranslated.put(index - notTrans.length(), notTrans.toString());
                    notTrans.setLength(0);
                }
                index += grammarToken.getChs().length() - 1;
                continue;
            }

            char c = chs.charAt(index);
            String cs = String.valueOf(c);
            if (c < 128) {  // ASCII
                Token token = new Token(cs, cs, "n");
                origIndexTokens.put(index, token);
                if (!notTrans.isEmpty()) {
                    notTranslated.put(index - notTrans.length(), notTrans.toString());
                    notTrans.setLength(0);
                }
                continue;
            }

            Character pun = PUNCTUATIONS.get(c);
            if (pun != null) {
                Token token = new Token(cs, String.valueOf(pun), "pun");
                origIndexTokens.put(index, token);
                if (!notTrans.isEmpty()) {
                    notTranslated.put(index - notTrans.length(), notTrans.toString());
                    notTrans.setLength(0);
                }
                continue;
            }

            BaseItem direct = baseDict.getByChs(chs, index);
            if (direct != null) {
                Token token = new Token(direct.chs, direct.eng, direct.partOfSpeech);
                origIndexTokens.put(index, token);
                index += direct.chs.length() - 1;
                if (!notTrans.isEmpty()) {
                    notTranslated.put(index - notTrans.length(), notTrans.toString());
                    notTrans.setLength(0);
                }
            } else {
                String pinyin = pinyinDict.getPinyinByChs(c);
                if (pinyin == null) {
                    throw new NoSuchWordException(cs);
                }
                BaseItem sameSoundWord = baseDict.getByPinyin(pinyin);
                if (sameSoundWord != null) {
                    Token token = new Token(cs, sameSoundWord.eng, sameSoundWord.partOfSpeech);
                    origIndexTokens.put(index, token);
                    if (!notTrans.isEmpty()) {
                        notTranslated.put(index - notTrans.length(), notTrans.toString());
                        notTrans.setLength(0);
                    }
                } else {  // 去真的英语翻译
                    notTrans.append(c);
                }
            }
        }
        if (!notTrans.isEmpty()) {
            notTranslated.put(index - notTrans.length(), notTrans.toString());
            notTrans.setLength(0);
        }

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
        String pinyin = pinyinDict.getPinyinByChs(cur);
        List<Character> sameSound = pinyinDict.getChsListByPinyin(pinyin);
        Token tk = pickSameSoundWord(sameSound);
        if (tk == null) {
            String cs = String.valueOf(cur);
            return new Token(cs, cs, "n");
        } else {
            return tk;
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
                if (next != null && !next.getPartOfSpeech().equals("pun")) {
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
