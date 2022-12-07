package trashsoftware.duckSonTranslator.translators;

public class ChiToGegTranslator extends IntermediateTranslator {
    protected ChiToGegTranslator(DuckSonTranslator parent) {
        super(parent, new ChiToChsTranslator(parent), new ChsToGegTranslator(parent));
    }
}
