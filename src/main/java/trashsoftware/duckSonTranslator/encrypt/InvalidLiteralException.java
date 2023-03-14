package trashsoftware.duckSonTranslator.encrypt;

public class InvalidLiteralException extends RuntimeException {
    
    private final String unkLiteral;
    
    public InvalidLiteralException() {
        this("");
    }
    
    public InvalidLiteralException(String literal) {
        this(literal, false);
    }

    public InvalidLiteralException(String literal, boolean isKey) {
        super(isKey ? literal : ("Invalid literal '" + literal + "'"));

        this.unkLiteral = isKey ? "" : literal;
    }

    public String getUnkLiteral() {
        return unkLiteral;
    }
}
