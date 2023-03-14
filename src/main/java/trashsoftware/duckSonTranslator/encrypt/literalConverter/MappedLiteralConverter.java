package trashsoftware.duckSonTranslator.encrypt.literalConverter;

import trashsoftware.duckSonTranslator.dict.Util;
import trashsoftware.duckSonTranslator.encrypt.InvalidLiteralException;

import java.math.BigInteger;
import java.util.*;

public abstract class MappedLiteralConverter extends LiteralConverter {
    
    protected final Map<String, Integer> map;
    protected final Map<Integer, String> revMap;

    /**
     * Map的要求:
     * 1. 键不能是另一个键的前缀
     * 2. 值必须是从0开始的连续整数，意思是说最小值是0，最大值是map.size() - 1， 且不重复
     */
    protected MappedLiteralConverter(char splitChar, Map<String, Integer> map) {
        super(splitChar, map.size());
        
        this.map = map;
        this.revMap = Util.invertMap(map);
    }
    
    protected static Map<String, Integer> makeMapByKeys(String... keys) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], i);
        }
        return map;
    }

    @Override
    public String numToString(BigInteger num) {
        List<String> list = new ArrayList<>();

        while (num.compareTo(BigInteger.ZERO) != 0) {
            BigInteger[] divRem = num.divideAndRemainder(bigIntCarry);
            list.add(revMap.get(divRem[1].intValue()));
            num = divRem[0];
        }

        Collections.reverse(list);
        return String.join("", list);
    }

    @Override
    public BigInteger stringToNum(String string) {
        BigInteger result = BigInteger.ZERO;
        
        int strLen = string.length();
        
        int index = 0;
        while (index < strLen) {
            int len = 1;
            Integer val;
            while ((val = map.get(string.substring(index, index + len))) == null) {
                len += 1;
                if (index + len > strLen) {
                    throw new InvalidLiteralException(string.substring(index));
                }
            }

            result = result.multiply(bigIntCarry);
            result = result.add(BigInteger.valueOf(val));
            
            index += len;
        }

        return result;
    }
}
