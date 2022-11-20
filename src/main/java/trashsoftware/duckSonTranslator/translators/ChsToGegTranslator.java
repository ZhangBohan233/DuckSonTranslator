package trashsoftware.duckSonTranslator.translators;

import trashsoftware.duckSonTranslator.dict.BaseItem;
import trashsoftware.duckSonTranslator.grammar.ChsToGegCase;
import trashsoftware.duckSonTranslator.grammar.GrammarEffect;
import trashsoftware.duckSonTranslator.grammar.Token;
import trashsoftware.duckSonTranslator.result.ResultToken;
import trashsoftware.duckSonTranslator.result.TranslationResult;
import trashsoftware.duckSonTranslator.wordPickerChsGeg.MatchResult;

import java.io.IOException;
import java.util.*;

public class ChsToGegTranslator extends Translator {

    private static ChsToGegTranslator instance;

    ChsToGegCase chsToGegCaseAnalyzer;

    protected ChsToGegTranslator(DuckSonTranslator parent) {
        super(parent);

        try {
            chsToGegCaseAnalyzer = ChsToGegCase.getInstance();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ChsToGegTranslator getInstance(DuckSonTranslator parent) {
        if (instance == null) {
            instance = new ChsToGegTranslator(parent);
        }
        return instance;
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

    @Override
    public TranslationResult translate(String chs) {
        SortedMap<Integer, Token> grammars = new TreeMap<>();

        // 处理语法token
        for (int i = 0; i < chs.length(); i++) {
            String len1 = chs.substring(i, i + 1);
            String len2;
            GrammarEffect ge;
            if (i < chs.length() - 2) {
                len2 = chs.substring(i, i + 2);
                ge = parent.grammarDict.tenseInfo.get(len2);
                if (ge != null) {
                    if (!addSpecial(ge, chs, i, grammars)) {
                        grammars.put(i, new Token(len2, ge.engDirect, ge, i, 2));
                        i += 1;
                        continue;
                    }
                }
            }
            ge = parent.grammarDict.tenseInfo.get(len1);
            if (ge != null) {
                if (!addSpecial(ge, chs, i, grammars)) {
                    grammars.put(i, new Token(len1, ge.engDirect, ge, i, 1));
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

            if (interrupt) {
                if (notTrans.length() > 0) {
                    notTranslated.put(index - notTrans.length(), notTrans.toString());
                    notTrans.setLength(0);
                }
                continue;
            }

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
                continue;
            }

            Character pun = CHS_PUNCTUATIONS.get(c);
            if (pun != null) {
                Token token = new Token(cs, String.valueOf(pun), "pun", index, 1);
                origIndexTokens.put(index, token);
                if (notTrans.length() > 0) {
                    notTranslated.put(index - notTrans.length(), notTrans.toString());
                    notTrans.setLength(0);
                }
                continue;
            }

            // 查小字典
            BaseItem direct = parent.isUseBaseDict()
                    ? parent.baseDict.getByChs(chs, index)
                    : null;
            if (direct != null) {
                Token token = new Token(direct.chs, direct.eng, direct.partOfSpeech,
                        index, direct.chs.length());
                origIndexTokens.put(index, token);
                if (notTrans.length() > 0) {
                    notTranslated.put(index - notTrans.length(), notTrans.toString());
                    notTrans.setLength(0);
                }
                index += direct.chs.length() - 1;
            } else {
                BaseItem sameSoundWord = baseDictSameSound(c, false);

                if (parent.options.isUseSameSoundChar() && sameSoundWord != null) {
                    Token token = new Token(cs, sameSoundWord.eng, sameSoundWord.partOfSpeech,
                            index, sameSoundWord.chs.length());
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

//        System.out.println(origIndexTokens);
//        System.out.println(notTranslated);

        // 开始真正的英语翻译
        List<Token> tokens = new ArrayList<>();
        for (int i = 0; i < chs.length(); i++) {
            Token tk = origIndexTokens.get(i);
            if (tk != null) {
                tokens.add(tk);
                i += tk.getChs().length() - 1;  // fixme: risky
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
                bigDictTrans(notTransSeg, tokens, i);
            }
        }
        applyGrammar(tokens);
        applyPronForms(tokens);

        return integrateToGeglish(tokens, chs);
    }

    private void bigDictTrans(String notTransSeg,
                              List<Token> tokens,
                              int startIndex) {
        int index = startIndex;
        while (!notTransSeg.isEmpty()) {
            MatchResult match = parent.chsToGegPicker.translateWord(notTransSeg);
            if (match != null && (match.strong || !parent.isUseSameSoundChar())) {
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
            if (parent.isUseSameSoundChar()) {
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

    private BaseItem baseDictSameSound(char chs, boolean forcedCover) {
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
        if (parent.isChongqingMode()) {
            sameSound = parent.pinyinDict.getChsListByCqPin(getPin(pinyin));
        } else {
            sameSound = parent.pinyinDict.getChsListByPinyin(getPin(pinyin));
        }
        return sameSound;
    }

    private Token bigDictSameSoundTrans(char chs, int index) {
        // 遍历同音字查询，只查一个字
        String[] pinyin = parent.pinyinDict.getPinyinByChs(chs);
        if (pinyin == null) return null;
        List<Character> sameSound = getSameSoundChsChars(chs);
        if (sameSound == null) return null;  // 其实和上面的重了
        Token tk = pickSameSoundWord(sameSound, index);
        if (tk == null) {
            String cs = String.valueOf(chs);
            String py = getPinNoTone(pinyin);
            return new Token(cs, py, "n", index, 1);
        } else {
            return tk;
        }
    }

    private String getPin(String[] pinyin) {
        return parent.options.isChongqingMode() ? pinyin[1] : pinyin[0];
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

    private Token pickSameSoundWord(List<Character> chsChars, int index) {
        double maxPre = -Double.MAX_VALUE;
        String minChs = null;
        MatchResult minVal = null;
        for (Character c : chsChars) {
            String s = String.valueOf(c);
            MatchResult matchResult = parent.chsToGegPicker.translateWord(s);
            if (matchResult != null && matchResult.precedence > maxPre) {
                maxPre = matchResult.precedence;
                minChs = s;
                minVal = matchResult;
            }
        }
        if (minVal == null) return null;
        return new Token(minChs, minVal.translated, minVal.partOfSpeech, index, 1);
    }

    private void applyPronForms(List<Token> tokens) {
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

    private TranslationResult integrateToGeglish(List<Token> tokens, String original) {
//        StringBuilder builder = new StringBuilder();
        TranslationResult result = new TranslationResult(original);
        Token lastActual = null;
        List<Token> grammarTokens = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.isTreatedAsActual()) {
                if (lastActual != null &&
                        lastActual.getOrigEng().equals(token.getOrigEng()) &&
                        !lastActual.getChs().equals(token.getChs())) {
                    // 连续两个的英文一样但中文不一样
                    if (lastActual.getEngAfterTense() == null) {
                        if (token.getEngAfterTense() == null) {
                            // 把这个token原文的range附加上去
                            ResultToken last = result.getLast();
                            last.addRange(token.getPosInOrig(), token.getLengthInOrig());
                            lastActual = token;
                        } else {
                            // 不要上一个了，替换为这一个
//                            builder.setLength(builder.length() - lastActual.getEng().length());
//                            builder.append(token.getEngAfterTense());
                            ResultToken last = result.removeLast();
                            ResultToken mergedToken = new ResultToken(
                                    token.getEngAfterTense(),
                                    last.getOrigRanges(),
                                    token.getPosInOrig(),
                                    token.getLengthInOrig());
                            result.add(mergedToken);
                        }
                    } else {
                        // 无事发生
                        lastActual = token;
                    }

                    continue;
                }
                String pre = "";
                if (lastActual != null &&
                        !NO_SPACE_AFTER.contains(lastActual.getPartOfSpeech()) &&
                        !NO_SPACE_BEFORE.contains(token.getPartOfSpeech())) {
//                    builder.append(' ');
                    pre = " ";
                }
//                System.out.println(token);
//                builder.append(token.getEng());
                result.add(new ResultToken(pre + token.getEng(), token.getPosInOrig(), token.getLengthInOrig()));
                lastActual = token;
            } else if (token.isEffect()) {
                grammarTokens.add(token);
            }
        }
        for (Token gt : grammarTokens) {
//            System.out.println(gt.getPosInOrig() + " " + gt.getLengthInOrig());
            Token applied = gt.getTokenAppliedThisGrammar();
//            System.out.println(applied);
            ResultToken rt = result.findTokenAt(applied.getPosInOrig(), applied.getLengthInOrig());
            if (rt != null) {
                rt.addRange(gt.getPosInOrig(), gt.getLengthInOrig());
            } else {
                System.err.println("Why im null");
            }
        }
//        return builder.toString();
        return result;
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
