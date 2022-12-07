package trashsoftware.duckSonTranslator.translators;

public class GegToChiTranslator extends IntermediateTranslator {
    protected GegToChiTranslator(DuckSonTranslator parent) {
        super(parent, new GegToChsTranslator(parent), new ChsToChiTranslator(parent));
    }
}
