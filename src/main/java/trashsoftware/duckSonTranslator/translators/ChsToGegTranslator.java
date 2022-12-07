package trashsoftware.duckSonTranslator.translators;

import trashsoftware.duckSonTranslator.dict.BaseItem;
import trashsoftware.duckSonTranslator.grammar.Token;
import trashsoftware.duckSonTranslator.result.TranslationResult;
import trashsoftware.duckSonTranslator.wordPickers.PickerFromChs;
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
                bigDictTrans(notTransSeg, tokens, i, parent.chsToGegPicker);
            }
        }
        applyGrammar(tokens);
        applyPronForms(tokens);

        return integrateToGeglish(tokens, chs);
    }
}
