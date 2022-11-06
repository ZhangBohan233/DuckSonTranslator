package trashsoftware.duckSonTranslator.dict;

public class BaseItem {
    public final String chs;
    public final String cq;
    public final String pinyin;
    public final String eng;
    public final String partOfSpeech;
    private boolean coverSameSound = true;  // 是否覆盖同音字

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

    @Override
    public String toString() {
        return "BaseItem{" + chs + ", " + 
                cq + ", " + 
                pinyin + ", " + 
                eng + ", " +
                partOfSpeech + '}';
    }
}
