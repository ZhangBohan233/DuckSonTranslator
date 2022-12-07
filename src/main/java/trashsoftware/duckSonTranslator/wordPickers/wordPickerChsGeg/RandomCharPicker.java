package trashsoftware.duckSonTranslator.wordPickers.wordPickerChsGeg;

import trashsoftware.duckSonTranslator.dict.BigDict;
import trashsoftware.duckSonTranslator.dict.BigDictValue;
import trashsoftware.duckSonTranslator.wordPickers.PickerFactory;

import java.util.*;

public class RandomCharPicker extends SingleCharPicker {
    private final Random random = new Random();
    
    public RandomCharPicker(BigDict bigDict, PickerFactory factory) {
        super(bigDict, factory);
    }

    @Override
    protected ResultFromChs translateChar(char chs) {
        Map<String, BigDictValue> matches = bigDict.getAllMatches(chs);
        if (matches.isEmpty()) return ResultFromChs.NOT_FOUND;
        
        List<BigDictValue> values = new ArrayList<>(matches.values());
        int keyIndex = random.nextInt(values.size());
        BigDictValue value = values.get(keyIndex);
        
        List<String> poses = new ArrayList<>(value.value.keySet());
        int posIndex = random.nextInt(poses.size());
        String pickedPos = poses.get(posIndex);
        List<String> engList = new ArrayList<>(value.value.get(pickedPos));
        
        int engIndex = random.nextInt(engList.size());
        
        return new ResultFromChs(engList.get(engIndex), pickedPos, 1);
    }
}
