package trashsoftware.duckSonTranslator.translators;

import trashsoftware.duckSonTranslator.grammar.GrammarEffect;
import trashsoftware.duckSonTranslator.grammar.Token;
import trashsoftware.duckSonTranslator.result.ResultToken;
import trashsoftware.duckSonTranslator.result.TranslationResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class StdLatinToChs extends Translator {
    protected StdLatinToChs(DuckSonTranslator parent) {
        super(parent);
    }

    private static void increase(int[] limits, int[] indices) {
        indices[indices.length - 1]++;

        for (int i = limits.length - 1; i >= 0; i--) {
            if (indices[i] < limits[i]) return;  // 不用进位，杀鸽

            indices[i] = 0;
            indices[i - 1]++;  // 进位，不管溢出，让它报错
        }
    }

    public static String[][][] makeCombinations(List<String[]>[] possibles) {
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

    @Override
    public TranslationResult translate(String geglish) {
        List<Token> tokens = deriveGeglishTokens(geglish);

        // 把和语法有关的翻译了
        tokens = translateCombineTokensGegToChs(tokens);

        // 翻译重要的
        for (Token token : tokens) {
            if (token.isUntranslatedEng()) {
                translateToken(token);
            }
        }
//        System.out.println(tokens);
        tokens = insertTokensByGrammar(tokens);
//        System.out.println(tokens);
        return integrateChsTokens(tokens, geglish);
    }

    protected List<Token> deriveGeglishTokens(String geglish) {
        List<Token> words = new ArrayList<>();
        StringBuilder word = new StringBuilder();
        StringBuilder number = new StringBuilder();
        int index = 0;
        for (; index < geglish.length(); index++) {
            char c = geglish.charAt(index);
            if (c == ' ' || c == '\n') {
                if (number.length() > 0) {
                    String nonWordS = number.toString();
                    number.setLength(0);
                    words.add(new Token(nonWordS, nonWordS, "num",
                            index - nonWordS.length(), nonWordS.length()));
                }
                if (word.length() > 0) {
                    String wordS = word.toString();
                    words.add(new Token(wordS,
                            index - wordS.length(), wordS.length()));
                    word.setLength(0);
                }
                if (c == '\n') {
                    words.add(new Token("\n", "\n", "lf", index, 1));
                }
            } else if ((c >= 'A' && c <= 'z') || c == '\'') {
                if (number.length() > 0) {
                    String nonWordS = number.toString();
                    number.setLength(0);
                    words.add(new Token(nonWordS, nonWordS, "num",
                            index - nonWordS.length(), nonWordS.length()));
                }
                word.append(c);
            } else {
                if (word.length() > 0) {
                    String wordS = word.toString();
                    words.add(new Token(wordS, index - wordS.length(), wordS.length()));
                    word.setLength(0);
                }
                if (c >= '0' && c <= '9') {
                    number.append(c);
                } else {
                    if (number.length() > 0) {
                        String nonWordS = number.toString();
                        number.setLength(0);
                        words.add(new Token(nonWordS, nonWordS, "num",
                                index - nonWordS.length(), nonWordS.length()));
                    }
                    Character pun = ENG_PUNCTUATIONS.get(c);
                    if (pun != null) {
                        words.add(new Token(String.valueOf(pun), String.valueOf(c), "pun",
                                index, 1));
                    } else {
                        words.add(new Token(String.valueOf(c), String.valueOf(c), "unk",
                                index, 1));
                    }
                }

            }
//            }
        }
        if (number.length() > 0) {
            String nonWordS = number.toString();
            words.add(new Token(nonWordS, nonWordS, "num",
                    index - nonWordS.length(), nonWordS.length()));
            number.setLength(0);
        }
        if (word.length() > 0) {
            String wordS = word.toString();
            words.add(new Token(wordS, index - wordS.length(), wordS.length()));
            word.setLength(0);
        }
//        System.out.println(words);
        return words;
    }

    @SuppressWarnings("unchecked")
    protected List<Token> translateCombineTokensGegToChs(List<Token> tokens) {
        List<Token> newTokens = new ArrayList<>();
        OUT_LOOP:
        for (int i = 0; i < tokens.size(); i++) {
            for (int j = i + 1; j < Math.min(tokens.size(), i + parent.grammarDict.getMaxEngCombLength()); j++) {
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

                for (Map.Entry<String, GrammarEffect> entry : parent.grammarDict.tenseInfo.entrySet()) {
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
                                    // fixme: 存疑
                                    Token token = tokens.get(i - engPos.length + x + 1);
                                    Token tk = new Token(chsSplit[x], engPos[x][0], engPos[x][1],
                                            token.getPosInOrig(), token.getLengthInOrig());
//                                    System.out.println(tk);
                                    if (!poss[x][1].isEmpty()) {
                                        tk.addTense(poss[x][1]);
                                    }

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


    protected List<Token> insertTokensByGrammar(List<Token> tokens) {
        List<Token> tokensAfterGrammar = new ArrayList<>();
        for (Token token : tokens) {
            if (token.getTenses().isEmpty()) {
                tokensAfterGrammar.add(token);
            } else {
//                System.out.println(token);
                tokensAfterGrammar.addAll(token.applyTenseToChs(parent.grammarDict));
            }
        }
        return tokensAfterGrammar;
    }

    protected TranslationResult integrateChsTokens(List<Token> tokens, String original) {
//        StringBuilder builder = new StringBuilder();
        TranslationResult result = new TranslationResult(original);
        for (Token token : tokens) {
            if (token.isActual()) {
                ResultToken resultToken;
                if (token.getChs() != null) {
                    resultToken =
                            new ResultToken(token.getChs(), token.getPosInOrig(), token.getLengthInOrig());
                } else {
                    resultToken =
                            new ResultToken(token.getEng(), token.getPosInOrig(), token.getLengthInOrig());
                }
                result.add(resultToken);
            } else {
//                builder.append(token.getChs());
                ResultToken resultToken =
                        new ResultToken(token.getChs(), token.getPosInOrig(), token.getLengthInOrig());
                result.add(resultToken);
            }
        }
//        return builder.toString();
        return result;
    }
    
    protected abstract void translateToken(Token token);
}
