package trashsoftware.duckSonTranslator.translators;

import trashsoftware.duckSonTranslator.dict.BaseItem;
import trashsoftware.duckSonTranslator.grammar.ChsToGegCase;
import trashsoftware.duckSonTranslator.grammar.GrammarEffect;
import trashsoftware.duckSonTranslator.grammar.Token;
import trashsoftware.duckSonTranslator.wordPickers.PickerFromChs;
import trashsoftware.duckSonTranslator.wordPickers.wordPickerChsGeg.ResultFromChs;

import java.io.IOException;
import java.util.*;

public abstract class StdChsToLatin extends Translator {

    protected ChsToGegCase chsToGegCaseAnalyzer;

    protected StdChsToLatin(DuckSonTranslator parent) {
        super(parent);
        
        try {
            chsToGegCaseAnalyzer = ChsToGegCase.getInstance();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean addSpecial(GrammarEffect ge,
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
                        Token token = new Token(sss[i], desPos[i][0], desPos[i][1],
                                beginIndex + acc, sss[i].length());
                        grammarTokens.put(beginIndex + acc, token);
                        acc += sss[i].length();
                    }
                } else {
                    grammarTokens.put(beginIndex, new Token(ss, desPos[0][0], desPos[0][1],
                            beginIndex, ss.length()));
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
                        Token token = new Token(sss[i], desPos[i][0], desPos[i][1],
                                index + acc, sss[i].length());
                        grammarTokens.put(index + acc, token);
                        acc += sss[i].length();
                    }
                } else {
                    grammarTokens.put(index, new Token(ss, desPos[0][0], desPos[0][1],
                            index, ss.length()));
                }
                return true;
            }
        }
        return false;
    }

    protected void bigDictTrans(String notTransSeg,
                                List<Token> tokens,
                                int startIndex,
                                PickerFromChs picker) {
        int index = startIndex;
        while (!notTransSeg.isEmpty()) {
            ResultFromChs match = picker.translate(notTransSeg);
            if (match != null && (match.strong || !parent.getOptions().isUseSameSoundChar())) {
                // 不strong的时候就先放一放，去检查小字典同音字
                String thisWord = notTransSeg.substring(0, match.matchLength);

                Token token = new Token(thisWord, match.translated, match.partOfSpeech,
                        index, match.matchLength);
                tokens.add(token);

                notTransSeg = notTransSeg.substring(match.matchLength);
                index += match.matchLength;
                continue;
            }

            Token trans;
            int pushLen;
            if (parent.getOptions().isUseSameSoundChar()) {
                char cur = notTransSeg.charAt(0);
                BaseItem sameSoundBase = baseDictSameSound(cur, true);
                if (sameSoundBase != null) {
                    trans = new Token(sameSoundBase.chs, sameSoundBase.eng, sameSoundBase.partOfSpeech,
                            index, 1);
                    pushLen = 1;
                } else if (match != null) {
                    String thisWord = notTransSeg.substring(0, match.matchLength);

                    trans = new Token(thisWord, match.translated, match.partOfSpeech,
                            index, match.matchLength);
                    pushLen = match.matchLength;
                } else {
                    trans = bigDictSameSoundTrans(cur, index);  // todo: 如果要查连续词，这里要改
                    if (trans == null) {
                        String curStr = String.valueOf(cur);
                        trans = new Token(curStr, curStr, "unk", index, 1);
                    }
                    pushLen = 1;
                }
            } else {
                if (match != null) {
                    String thisWord = notTransSeg.substring(0, match.matchLength);

                    trans = new Token(thisWord, match.translated, match.partOfSpeech,
                            index, match.matchLength);
                    pushLen = match.matchLength;
                } else {
                    char cur = notTransSeg.charAt(0);
                    String curStr = String.valueOf(cur);
                    String[] pinyin = parent.pinyinDict.getPinyinByChs(cur);
                    if (pinyin != null) {
                        String py = getPinNoTone(pinyin);
                        trans = new Token(curStr, py, "n", index, 1);
                    } else {
                        trans = new Token(curStr, curStr, "unk", index, 1);
                    }
                    pushLen = 1;
                }
            }
            tokens.add(trans);
            notTransSeg = notTransSeg.substring(pushLen);
            index += pushLen;
        }
    }

    protected BaseItem baseDictSameSound(char chs, boolean forcedCover) {
        String[] pinyin = parent.pinyinDict.getPinyinByChs(chs);
        if (pinyin == null) {
            return null;
        }
        BaseItem sameSoundWord;
        if (parent.options.isUseBaseDict()) {
            if (parent.options.isChongqingMode()) {
                sameSoundWord = parent.baseDict.getByCqPin(pinyin, forcedCover);
            } else {
                sameSoundWord = parent.baseDict.getByPinyin(pinyin, forcedCover);
            }
        } else {
            sameSoundWord = null;
        }
        return sameSoundWord;
    }

    private List<Character> getSameSoundChsChars(char chs) {
        String[] pinyin = parent.pinyinDict.getPinyinByChs(chs);
        if (pinyin == null) return null;
        List<Character> sameSound;
        if (parent.getOptions().isChongqingMode()) {
            sameSound = parent.pinyinDict.getChsListByCqPin(getPin(pinyin));
        } else {
            sameSound = parent.pinyinDict.getChsListByPinyin(getPin(pinyin));
        }
        return sameSound;
    }
    
    protected Token bigDictSameSoundTrans(char chs, int indexInFullSentence) {
        // 遍历同音字查询，只查一个字
        String[] pinyin = parent.pinyinDict.getPinyinByChs(chs);
        if (pinyin == null) return null;
        List<Character> sameSound = getSameSoundChsChars(chs);
        if (sameSound == null) return null;  // 其实和上面的重了
        Token tk = pickSameSoundWord(sameSound, indexInFullSentence);
        if (tk == null) {
            String cs = String.valueOf(chs);
            String py = getPinNoTone(pinyin);
            return new Token(cs, py, "n", indexInFullSentence, 1);
        } else {
            return tk;
        }
    }

    private Token pickSameSoundWord(List<Character> chsChars, int indexInFullSentence) {
        double maxPre = -Double.MAX_VALUE;
        String minChs = null;
        ResultFromChs minVal = null;
        for (Character c : chsChars) {
            String s = String.valueOf(c);
            ResultFromChs resultFromChs = parent.getChsToGegPicker().translate(s);
            if (resultFromChs != null && resultFromChs.precedence > maxPre) {
                maxPre = resultFromChs.precedence;
                minChs = s;
                minVal = resultFromChs;
            }
        }
        if (minVal == null) return null;
        return new Token(minChs, minVal.translated, minVal.partOfSpeech, indexInFullSentence, 1);
    }

    private String getPin(String[] pinyin) {
        return parent.options.isChongqingMode() ? pinyin[1] : pinyin[0];
    }

    protected String getPinNoTone(String[] pinyin) {
        String pin = getPin(pinyin);
        char tone = pin.charAt(pin.length() - 1);
        if (tone >= '0' && tone <= '4') {
            return pin.substring(0, pin.length() - 1);
        } else {
            return pin;
        }
    }

    protected Map<Integer, Token> grammarTokens(String chs) {
        SortedMap<Integer, Token> grammars = new TreeMap<>();

        // 处理语法token
        for (int i = 0; i < chs.length(); i++) {
            String len1 = chs.substring(i, i + 1);
            String len2;
            GrammarEffect ge;
            if (i < chs.length() - 2) {
                len2 = chs.substring(i, i + 2);
                ge = parent.grammarDict.tenseByChs.get(len2);
                if (ge != null) {
                    if (!addSpecial(ge, chs, i, grammars)) {
                        grammars.put(i, new Token(len2, ge.engDirect, ge, i, 2));
                        i += 1;
                        continue;
                    }
                }
            }
            ge = parent.grammarDict.tenseByChs.get(len1);
            if (ge != null) {
                if (!addSpecial(ge, chs, i, grammars)) {
                    grammars.put(i, new Token(len1, ge.engDirect, ge, i, 1));
                }
            }
        }
        return grammars;
    }

    protected boolean processNumOrUnknowns(char c,
                                           int index,
                                           Map<Integer, Token> origIndexTokens,
                                           StringBuilder numBuilder,
                                           StringBuilder engBuilder) {
        boolean interrupt = false;
        String cs = String.valueOf(c);
        if (c >= '0' && c <= '9') {
            numBuilder.append(c);
            interrupt = true;
        } else {
            if (numBuilder.length() > 0) {
                String numStr = numBuilder.toString();
                int index2 = index - numStr.length();
                origIndexTokens.put(index2, new Token(numStr, numStr, "num",
                        index2, numStr.length()));
                numBuilder.setLength(0);
            }
        }

        if (c >= 'A' && c <= 'z') {
            engBuilder.append(c);
            interrupt = true;
        } else {
            if (engBuilder.length() > 0) {
                String engStr = engBuilder.toString();
                int index2 = index - engStr.length();
                origIndexTokens.put(index2, new Token(engStr, engStr, "eng",
                        index2, engStr.length()));
                engBuilder.setLength(0);
            }
        }
        return interrupt;
    }

    protected boolean processAscii(char c,
                                   String cs,
                                   int index,
                                   Map<Integer, Token> origIndexTokens,
                                   Map<Integer, String> notTranslated,
                                   StringBuilder notTrans) {
        if (c < 128) {  // ASCII
            String pos = "unk";

            if (ETC.contains(c)) {
                pos = "etc";
            }
            Token token = new Token(cs, cs, pos, index, 1);
            origIndexTokens.put(index, token);
            if (notTrans.length() > 0) {
                notTranslated.put(index - notTrans.length(), notTrans.toString());
                notTrans.setLength(0);
            }
            return true;
        }
        return false;
    }

    protected boolean processChsPunctuation(char c,
                                            String cs,
                                            int index,
                                            Map<Integer, Token> origIndexTokens,
                                            Map<Integer, String> notTranslated,
                                            StringBuilder notTrans) {
        Character pun = CHS_PUNCTUATIONS.get(c);
        if (pun != null) {
            Token token = new Token(cs, String.valueOf(pun), "pun", index, 1);
            origIndexTokens.put(index, token);
            if (notTrans.length() > 0) {
                notTranslated.put(index - notTrans.length(), notTrans.toString());
                notTrans.setLength(0);
            }
            return true;
        }
        return false;
    }
    
    protected void finishTokens(int index,
                                Map<Integer, Token> origIndexTokens,
                                Map<Integer, String> notTranslated,
                                StringBuilder numBuilder,
                                StringBuilder engBuilder,
                                StringBuilder notTrans) {
        if (notTrans.length() > 0) {
            notTranslated.put(index - notTrans.length(), notTrans.toString());
            notTrans.setLength(0);
        }
        if (numBuilder.length() > 0) {
            String numStr = numBuilder.toString();
            int index2 = index - numStr.length();
            origIndexTokens.put(index2, new Token(numStr, numStr, "num",
                    index2, numStr.length()));
            numBuilder.setLength(0);
        }
        if (engBuilder.length() > 0) {
            String engStr = engBuilder.toString();
            int index2 = index - engStr.length();
            origIndexTokens.put(index2, new Token(engStr, engStr, "eng",
                    index2, engStr.length()));
            engBuilder.setLength(0);
        }
    }

    protected void applyPronForms(List<Token> tokens) {
        if (tokens.isEmpty()) {
            System.err.println("Empty tokens, weird");
            return;
        }
        
        if (tokens.size() == 1) {
            // 也懒得检查是不是actual了
            chsToGegCaseAnalyzer.applyToMiddleToken(null, tokens.get(0), null);
            return;
        }
        Token last = null;
        Token middle = null;
        Token next = null;
        for (Token cur : tokens) {
            if (cur.isTreatedAsActual()) {
                last = middle;
                middle = next;
                next = cur;
            }
            if (middle != null) {
                chsToGegCaseAnalyzer.applyToMiddleToken(last, middle, next);
            }
        }
        assert next != null;  // 逻辑上不存在这种情况
        chsToGegCaseAnalyzer.applyToMiddleToken(middle, next, null);
    }

    protected void applyGrammar(List<Token> tokens) {
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
                                token.setGrammarApplied(tk);
                                break;
                            }
                        } else if (tk.isActual() &&
                                GRAMMAR_TERMINATOR.contains(tk.getPartOfSpeech())) {
                            break;
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
                                token.setGrammarApplied(tk);
                                break;
                            }
                        } else if (tk.isActual() &&
                                GRAMMAR_TERMINATOR.contains(tk.getPartOfSpeech())) {
                            break;
                        }
                    }
                }
            }
        }
    }
}
