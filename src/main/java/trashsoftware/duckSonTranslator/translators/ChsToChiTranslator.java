package trashsoftware.duckSonTranslator.translators;

import trashsoftware.duckSonTranslator.dict.BaseItem;
import trashsoftware.duckSonTranslator.dict.PinyinItem;
import trashsoftware.duckSonTranslator.grammar.Token;
import trashsoftware.duckSonTranslator.result.ResultToken;
import trashsoftware.duckSonTranslator.result.TranslationResult;
import trashsoftware.duckSonTranslator.wordPickers.wordPickerChsGeg.ResultFromChs;

import java.util.*;

public class ChsToChiTranslator extends StdChsToLatin {

    public ChsToChiTranslator(DuckSonTranslator parent) {
        super(parent, "chs", "chi");
    }

    @Override
    public TranslationResult translate(String chs) {
        Map<Integer, Token> grammars = findGrammarTokens(chs);

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

            char c = chs.charAt(index);
            String cs = String.valueOf(c);
            boolean interrupt = processNumOrUnknowns(
                    c,
                    index,
                    origIndexTokens,
                    numBuilder,
                    engBuilder);

            if (interrupt) {
                if (notTrans.length() > 0) {
                    notTranslated.put(index - notTrans.length(), notTrans.toString());
                    notTrans.setLength(0);
                }
                continue;
            }

            if (processAscii(c, cs, index, origIndexTokens, notTranslated, notTrans)) {
                continue;
            }

            if (processChsPunctuation(c, cs, index, origIndexTokens, notTranslated, notTrans)) {
                continue;
            }

            // 查小字典
            BaseItem direct = parent.getOptions().isUseBaseDict()
                    ? parent.baseDict.getByChs(chs, index, parent.getOptions())
                    : null;
            if (direct != null) {
                int chsLen = direct.chs.length();
                if (chsLen > 1) {
                    Token token = new Token(direct.chs, direct.eng, direct.partOfSpeech,
                            index, chsLen);
                    origIndexTokens.put(index, token);
                    if (notTrans.length() > 0) {
                        notTranslated.put(index - notTrans.length(), notTrans.toString());
                        notTrans.setLength(0);
                    }
                    index += direct.chs.length() - 1;
                } else {
                    // 单个字的不加，去真的英语翻译
                    notTrans.append(c);
                }
            } else {
                // 去真的英语翻译
                notTrans.append(c);
            }

//            if (notTrans.length() > 0) {
//                notTranslated.put(index - notTrans.length(), notTrans.toString());
//                notTrans.setLength(0);
//            }
        }
        finishTokens(index, origIndexTokens, notTranslated, numBuilder, engBuilder, notTrans);
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
                hugeDictTrans(notTransSeg, tokens, i);
            }
        }

        applyGrammar(tokens);
        applyPronForms(tokens);
        
        return integrateToChinglish(tokens, chs);
    }


    protected TranslationResult integrateToChinglish(List<Token> tokens, String original) {
//        StringBuilder builder = new StringBuilder();
        TranslationResult result = new TranslationResult(original);
        Token lastActual = null;
        List<Token> grammarTokens = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.isTreatedAsActual()) {
//                if (lastActual != null &&
//                        lastActual.getOrigEng().equals(token.getOrigEng()) &&
//                        !lastActual.getChs().equals(token.getChs())) {
//                    // 连续两个的英文一样但中文不一样
//                    if (lastActual.getEngAfterTense() == null) {
//                        if (token.getEngAfterTense() == null) {
//                            // 把这个token原文的range附加上去
//                            ResultToken last = result.getLast();
//                            last.addRange(token.getPosInOrig(), token.getLengthInOrig());
//                            lastActual = token;
//                        } else {
//                            // 不要上一个了，替换为这一个
////                            builder.setLength(builder.length() - lastActual.getEng().length());
////                            builder.append(token.getEngAfterTense());
//                            ResultToken last = result.removeLast();
//                            ResultToken mergedToken = new ResultToken(
//                                    token.getEngAfterTense(),
//                                    last.getOrigRanges(),
//                                    token.getPosInOrig(),
//                                    token.getLengthInOrig());
//                            result.add(mergedToken);
//                        }
//                    } else {
//                        // 无事发生
//                        lastActual = token;
//                    }
//
//                    continue;
//                }
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
    
    private void hugeDictTrans(String notTransSeg, List<Token> tokens, int startIndex) {
        int index = startIndex;
        while (!notTransSeg.isEmpty()) {
            ResultFromChs match = parent.getChsToChiPicker().translate(notTransSeg);
            if (match != null) {
                if (match.matchLength == 1 && parent.getOptions().isUseBaseDict()) {
                    // 一个字的，去小字典看看
                    BaseItem direct = parent.baseDict.getByChs(notTransSeg, 0, parent.getOptions());
                    if (direct != null) {
                        // 我敢保证这里的length是1
                        Token token = new Token(direct.chs, direct.eng, direct.partOfSpeech, 
                                index, direct.chs.length());
                        tokens.add(token);
                        
                        notTransSeg = notTransSeg.substring(direct.chs.length());  // 但我还是怂了
                        index += direct.chs.length();
                        continue;
                    }
                }
                
                String thisWord = notTransSeg.substring(0, match.matchLength);

                Token token = new Token(thisWord, match.translated, match.partOfSpeech,
                        index, match.matchLength);
                tokens.add(token);

                notTransSeg = notTransSeg.substring(match.matchLength);
                index += match.matchLength;
                continue;
            }

            BaseItem direct = parent.getOptions().isUseBaseDict()
                    ? parent.baseDict.getByChs(notTransSeg, 0, parent.getOptions())
                    : null;
            if (direct != null) {
                Token token = new Token(direct.chs, direct.eng, direct.partOfSpeech,
                        index, direct.chs.length());
                tokens.add(token);
                notTransSeg = notTransSeg.substring(direct.chs.length());
                index += direct.chs.length();
                continue;
            }
            
            if (parent.getOptions().isUseSameSoundChar()) {
                if (parent.getOptions().isUseBaseDict()) {
                    BaseItem baseSameSound = baseDictSameSound(notTransSeg.charAt(0), true);
                    if (baseSameSound != null) {
                        int len = baseSameSound.chs.length();
                        Token token = new Token(baseSameSound.chs, baseSameSound.eng, baseSameSound.partOfSpeech,
                                index, len);
                        tokens.add(token);
                        notTransSeg = notTransSeg.substring(len);
                        index += len;
                        continue;
                    }
                }
                Token bigDictSameSound = bigDictSameSoundTrans(notTransSeg.charAt(0), index);
                assert bigDictSameSound != null;
                
                tokens.add(bigDictSameSound);
                notTransSeg = notTransSeg.substring(bigDictSameSound.getLengthInOrig());
                index += bigDictSameSound.getLengthInOrig();
            } else {
                // 直接上拼音了
                char chs = notTransSeg.charAt(0);
                String cs = String.valueOf(chs);
                PinyinItem pinyin = parent.pinyinDict.getPinyinByChs(chs);
                String py = getPinNoTone(pinyin);
                Token token = new Token(cs, py, "n", index, 1);
                tokens.add(token);
                notTransSeg = notTransSeg.substring(1);
                index += 1;
            }
        }
    }
}
