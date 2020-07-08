package interpreter;

public class Token {

    private String value;
    private String type;

    public Token(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getToken() {
        return "Token({" + type + "}, {" +  value + "})";
    }

    public String getType() {
        return type;
    }

    void setValue(String value) {
        this.value = value;
    }
    void setType(String type) {
        this.type = type;
    }

    String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        return type.equals(((Token)obj).getType()) && value.equals(((Token)obj).getValue());
    }
}
