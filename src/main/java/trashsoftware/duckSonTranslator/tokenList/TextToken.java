package trashsoftware.duckSonTranslator.tokenList;

public class TextToken {
    
    public static final TextToken PLACE_HOLDER = new TextToken("");
    
    private String text;
    private String pre = "";
    private String post = "";
    TextToken link = PLACE_HOLDER;
    
    public TextToken(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return pre + text + post;
    }
    
    public void append(String textExt) {
        this.text += textExt;
    }
    
    public void append(String textExt, TextToken newLink) {
        this.text += textExt;
        this.link = newLink;
    }

    public void setPre(String pre) {
        this.pre = pre;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public TextToken getLink() {
        return link;
    }
    
    public int textLength() {
        return pre.length() + text.length() + post.length();
    }
    
    public int preLength() {
        return pre.length();
    }
    
    public int postLength() {
        return post.length();
    }
}
