package trashsoftware.duckSonTranslator.tokenList;

import java.util.ArrayList;
import java.util.List;

public class TranslationResult {

    // 两个list同样长且对齐
    private final List<TextToken> srcLanguage = new ArrayList<>();
    private final List<TextToken> dstLanguage = new ArrayList<>();

    public TranslationResult() {

    }

    private static String asString(List<TextToken> tokens) {
        StringBuilder builder = new StringBuilder();
        for (TextToken token : tokens) builder.append(token.toString());
        return builder.toString();
    }

    private static int findTokenIndexByCharIndex(List<TextToken> list,
                                                 int charIndex) {
        int cumulatedLen = 0;
        for (int tokenIndex = 0; tokenIndex < list.size(); tokenIndex++) {
            TextToken token = list.get(tokenIndex);
            int tokenBegin = cumulatedLen;
            cumulatedLen += token.textLength();
            int tokenEnd = cumulatedLen;
            if (charIndex >= tokenBegin && charIndex <= tokenEnd) return tokenIndex;
        }
        return -1;
    }

    private static int findCharIndexByTokenIndex(List<TextToken> list,
                                                 int tokenIndex) {
        int cumulatedLen = 0;
        for (int ti = 0; ti < list.size(); ti++) {
            TextToken token = list.get(tokenIndex);
            int tokenBegin = cumulatedLen + token.preLength();
            cumulatedLen += token.textLength();
//            int tokenEnd = cumulatedLen - token.postLength();
            if (ti == tokenIndex) {
                return tokenBegin;
            }
        }
        return -1;
    }

    public void addOne(TextToken srcLang, TextToken dstLang) {
        srcLang.link = dstLang;
        dstLang.link = srcLang;
        srcLanguage.add(srcLang);
        dstLanguage.add(dstLang);
    }

    public void addOnlySrc(TextToken srcLang) {
        srcLang.link = TextToken.PLACE_HOLDER;
        srcLanguage.add(srcLang);
        dstLanguage.add(TextToken.PLACE_HOLDER);
    }

    public TextToken getLastSrc() {
        return srcLanguage.get(srcLanguage.size() - 1);
    }

    public TextToken getLastDst() {
        return dstLanguage.get(dstLanguage.size() - 1);
    }

    public TextToken getSrcTokenAt(int i) {
        return srcLanguage.get(i);
    }

    public TextToken getDstTokenAt(int i) {
        return dstLanguage.get(i);
    }

    public TextToken replaceLastDst(TextToken newLast) {
        TextToken last = dstLanguage.remove(dstLanguage.size() - 1);
        dstLanguage.add(newLast);
        return last;
    }

    public String getSrcAsString() {
        return asString(srcLanguage);
    }

    public String getDstAsString() {
        return asString(dstLanguage);
    }

    public int findSrcTokenIndexByCharIndex(int charIndex) {
        return findTokenIndexByCharIndex(srcLanguage, charIndex);
    }

    public int findDstTokenIndexByCharIndex(int charIndex) {
        return findTokenIndexByCharIndex(dstLanguage, charIndex);
    }

    public int findSrcCharIndexByTokenIndex(int tokenIndex) {
        return findCharIndexByTokenIndex(srcLanguage, tokenIndex);
    }

    public int findDstCharIndexByTokenIndex(int tokenIndex) {
        return findCharIndexByTokenIndex(dstLanguage, tokenIndex);
    }

    @Override
    public String toString() {
        return getDstAsString();
    }

    public void printTokens() {
        System.out.println(srcLanguage);
        System.out.println(dstLanguage);
    }
}
