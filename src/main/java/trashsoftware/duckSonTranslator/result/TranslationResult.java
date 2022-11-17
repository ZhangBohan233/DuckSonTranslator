package trashsoftware.duckSonTranslator.result;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TranslationResult implements Serializable {

    public final String originalText;
    private final List<ResultToken> resultTokens = new ArrayList<>();

    public TranslationResult(String originalText) {
        this.originalText = originalText;
    }

    public static List<int[]> rangeOf(List<ResultToken> tokens) {
        List<int[]> inter = new ArrayList<>();
        for (ResultToken token : tokens) {
            inter.addAll(token.getOrigRanges());
        }
        if (inter.isEmpty()) return inter;
        inter.sort(Comparator.comparingInt(o -> o[0]));
        List<int[]> res = new ArrayList<>();
        int[] active = inter.get(0);
        int begin = active[0];
        int end = active[1];
        for (int i = 1; i < inter.size(); i++) {
            active = inter.get(i);

            if (active[0] != end) {
                res.add(new int[]{begin, end});
                begin = active[0];
            }
            end = active[1];
        }
        res.add(new int[]{begin, end});
        return res;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void add(ResultToken token) {
        resultTokens.add(token);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (ResultToken token : resultTokens) {
            builder.append(token.toString());
        }
        return builder.toString();
    }

    public ResultToken findTokenAt(int index, int length) {
        for (ResultToken token : resultTokens) {
            List<int[]> ranges = token.getOrigRanges();
            for (int[] ran : ranges) {
                if (ran[0] == index && ran[1] == index + length) {
                    return token;
                }
            }
        }
        return null;
    }

    /**
     * 从翻译结果中找到位置within范围的tokens，只要沾到了就返回。
     */
    public List<ResultToken> findTokensInRange(int beginCharPos, int endCharPos) {
        List<ResultToken> res = new ArrayList<>();
        int cumLen = 0;
        for (int i = 0; i < resultTokens.size(); i++) {
            ResultToken rt = resultTokens.get(i);
            cumLen += rt.translated.length();
            if (cumLen > beginCharPos) {
                res.add(rt);
            }
            if (cumLen >= endCharPos) break;
        }
        return res;
    }

    public ResultToken removeLast() {
        return resultTokens.remove(resultTokens.size() - 1);
    }

    public ResultToken getLast() {
        return resultTokens.get(resultTokens.size() - 1);
    }

    public void printTokens() {
        for (ResultToken rt : resultTokens) {
            rt.print();
            System.out.print(", ");
        }
        System.out.println();
    }
}
