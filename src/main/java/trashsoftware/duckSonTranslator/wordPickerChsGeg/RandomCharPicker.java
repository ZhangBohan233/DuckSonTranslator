package trashsoftware.duckSonTranslator.wordPickerChsGeg;

import trashsoftware.duckSonTranslator.dict.BigDict;
import trashsoftware.duckSonTranslator.dict.BigDictValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RandomCharPicker extends SingleCharPicker {
    private final Random random = new Random();
    
    protected RandomCharPicker(BigDict bigDict, PickerFactory factory) {
        super(bigDict, factory);
    }

    @Override
    protected Result translateChar(char chs) {
        Map<String, BigDictValue> matches = bigDict.getAllMatches(chs);
        if (matches.isEmpty()) return Result.NOT_FOUND;
        
        List<BigDictValue> values = new ArrayList<>(matches.values());
        int keyIndex = random.nextInt(values.size());
        BigDictValue value = values.get(keyIndex);
        
        List<String> poses = new ArrayList<>(value.value.keySet());
        int posIndex = random.nextInt(poses.size());
        String pickedPos = poses.get(posIndex);
        List<String> engList = value.value.get(pickedPos);
        
        int engIndex = random.nextInt(engList.size());
        
        return new Result(engList.get(engIndex), pickedPos, 1);
    }
}
