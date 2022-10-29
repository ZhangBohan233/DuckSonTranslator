package trashsoftware.duckSonTranslator.translators;

public class NoSuchWordException extends RuntimeException {
    public NoSuchWordException() {
        
    }
    
    public NoSuchWordException(String word) {
        super("Word '" + word + "' is not translatable.");
    }
}
