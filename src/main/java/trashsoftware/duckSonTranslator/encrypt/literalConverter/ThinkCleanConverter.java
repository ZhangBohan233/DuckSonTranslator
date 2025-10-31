package trashsoftware.duckSonTranslator.encrypt.literalConverter;

import java.math.BigInteger;

public class ThinkCleanConverter extends MappedLiteralConverter {
    
    private static ThinkCleanConverter instance;

    protected ThinkCleanConverter() {
        super('。',
                makeMapByKeys(
                        "李少", "李神", "李思洁", "孤身",
                        "回", "金沙",
                        "爷爷很失望",
                        "猪妹", "婕拉", "触手妈", "剑圣",
                        "蓝buff", "小龙", "峡谷先锋", "开团",
                        "包弟",
                        "剧本杀", "七百八",
                        "，", "、", "？", "！",
                        "..."
                ));
    }

    public static ThinkCleanConverter getInstance() {
        if (instance == null) {
            instance = new ThinkCleanConverter();
        }
        return instance;
    }

    public static void main(String[] args) {
        MappedLiteralConverter converter = new ThinkCleanConverter();
        BigInteger bigInteger = new BigInteger("139823792379723");
        String clean = converter.numToString(bigInteger);
        System.out.println(clean);
        BigInteger rev = converter.stringToNum(clean);
        System.out.println(rev);
    }
}
