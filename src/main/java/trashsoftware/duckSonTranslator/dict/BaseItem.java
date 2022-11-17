package trashsoftware.duckSonTranslator.dict;

import java.util.Arrays;

public class BaseItem {
    public final String chs;
    public final String cq;
    public final String pinyin;
    public final String eng;
    public final String partOfSpeech;
    private boolean coverSameSound = false;  // 是否覆盖同音字
    private boolean engDefault = false;  // 该词条是否是这个英文单词的默认词条

    BaseItem(String chs,
             String cq,
             String pinyin,
             String eng,
             String partOfSpeech) {
        this.chs = chs;
        this.cq = cq;
        this.pinyin = pinyin;
        this.eng = eng;
        this.partOfSpeech = partOfSpeech;
    }

    public void setCoverSameSound(boolean coverSameSound) {
        this.coverSameSound = coverSameSound;
    }

    public boolean isCoverSameSound() {
        return coverSameSound;
    }

    public void setEngDefault(boolean engDefault) {
        this.engDefault = engDefault;
    }

    public boolean isEngDefault() {
        return engDefault;
    }

    @Override
    public String toString() {
        return "BaseItem{" + chs + ", " + 
                cq + ", " + 
                pinyin + ", " +
                eng + ", " +
                partOfSpeech + '}';
    }
}
