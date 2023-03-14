package trashsoftware.duckSonTranslator.translators;

import trashsoftware.duckSonTranslator.dict.BaseItem;
import trashsoftware.duckSonTranslator.dict.BigDictValue;
import trashsoftware.duckSonTranslator.grammar.Token;
import trashsoftware.duckSonTranslator.result.ResultToken;
import trashsoftware.duckSonTranslator.result.TranslationResult;
import trashsoftware.duckSonTranslator.wordPickers.wordPickerChsChi.ChsChiWordPicker;
import trashsoftware.duckSonTranslator.wordPickers.wordPickerChsGeg.ResultFromChs;

import java.util.*;

public class ChsToGegTranslator extends StdChsToLatin {
    public ChsToGegTranslator(DuckSonTranslator parent) {
        super(parent);
    }

    @Override
    public TranslationResult translate(String chs) {
        Map<Integer, Token> grammars = grammarTokens(chs);

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
        finishTokens(index, origIndexTokens, notTranslated, numBuilder, engBuilder, notTrans);

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
                bigDictTrans(notTransSeg, tokens, i, parent.getChsToGegPicker());
            }
        }
        applyGrammar(tokens);
        applyPronForms(tokens);

        return integrateToGeglish(tokens, chs);
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
//                            System.out.println(lastActual.getChs() + token.getChs());
                            ResultToken last = result.getLast();
                            replaceIfIsWord(lastActual, token, last);
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
                            replaceIfIsWord(lastActual, token, mergedToken);
                            result.add(mergedToken);
                        }
                    } else {
                        // 无事发生
                        replaceIfIsWord(lastActual, token, result.getLast());
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
    
    private void replaceIfIsWord(Token lastActual, Token thisActual, ResultToken resultToken) {
        String combinedChs = lastActual.getChs() + thisActual.getChs();
        ChsChiWordPicker wordPicker = parent.getChsToChiPicker();
        ResultFromChs wordResult = wordPicker.translate(combinedChs);
        if (wordResult == null || wordResult == ResultFromChs.NOT_FOUND) {
            return;
        }
        String sub = combinedChs.substring(0, wordResult.matchLength);
        if (sub.equals(combinedChs)) {
            boolean set = false;
            lastActual.setOriginalEng(wordResult.translated);
            lastActual.setPartOfSpeech(wordResult.partOfSpeech);
            if (lastActual.getEngAfterTense() != null) {
                lastActual.reapplyTenses();
                resultToken.setTranslated(lastActual.getEngAfterTense());
                set = true;
            }
            thisActual.setOriginalEng(wordResult.translated);
            thisActual.setPartOfSpeech(wordResult.partOfSpeech);
            if (thisActual.getEngAfterTense() != null) {
                thisActual.reapplyTenses();
                resultToken.setTranslated(thisActual.getEngAfterTense());
                set = true;
            }
            if (!set) {
                resultToken.setTranslated(wordResult.translated);
            }
        }
    }
}
