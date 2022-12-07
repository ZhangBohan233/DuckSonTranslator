package trashsoftware.duckSonTranslator.translators;

import trashsoftware.duckSonTranslator.dict.BaseItem;
import trashsoftware.duckSonTranslator.grammar.Token;
import trashsoftware.duckSonTranslator.result.TranslationResult;
import trashsoftware.duckSonTranslator.wordPickers.wordPickerChsGeg.ResultFromChs;

import java.util.*;

public class ChsToChiTranslator extends StdChsToLatin {

    public ChsToChiTranslator(DuckSonTranslator parent) {
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
            BaseItem direct = parent.isUseBaseDict()
                    ? parent.baseDict.getByChs(chs, index)
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
        
        return integrateToGeglish(tokens, chs);
    }
    
    private void hugeDictTrans(String notTransSeg, List<Token> tokens, int startIndex) {
        int index = startIndex;
        while (!notTransSeg.isEmpty()) {
            ResultFromChs match = parent.chsToChiPicker.translate(notTransSeg);
            if (match != null) {
                if (match.matchLength == 1 && parent.isUseBaseDict()) {
                    // 一个字的，去小字典看看
                    BaseItem direct = parent.baseDict.getByChs(notTransSeg, 0);
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

            BaseItem direct = parent.isUseBaseDict()
                    ? parent.baseDict.getByChs(notTransSeg, 0)
                    : null;
            if (direct != null) {
                Token token = new Token(direct.chs, direct.eng, direct.partOfSpeech,
                        index, direct.chs.length());
                tokens.add(token);
                notTransSeg = notTransSeg.substring(direct.chs.length());
                index += direct.chs.length();
                continue;
            }
            
            if (parent.isUseSameSoundChar()) {
                if (parent.isUseBaseDict()) {
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
                String[] pinyin = parent.pinyinDict.getPinyinByChs(chs);
                String py = getPinNoTone(pinyin);
                Token token = new Token(cs, py, "n", index, 1);
                tokens.add(token);
                notTransSeg = notTransSeg.substring(1);
                index += 1;
            }
        }
    }
}
