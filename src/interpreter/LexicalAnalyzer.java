package interpreter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

public class LexicalAnalyzer {
    private String text;
    private int pos;
    private char currentChar;
    private boolean endOfText;
    private int currentLine;

    private TreeMap<String, String> keyWords;
    private TreeMap<String, String> operations;
    private TreeMap<String, String> brackets;
    private TreeMap<String, String> comparators;
    private TreeMap<String, String> logic;

    public LexicalAnalyzer(String text) {
        this.text = text;
        pos = 0;
        currentChar = text.charAt(pos);
        endOfText = false;
        currentLine = 1;
        init();
    }

    void init() {
        keyWords = new TreeMap<String, String>();
        keyWords.put("while", "While");
        keyWords.put("if", "If");
        keyWords.put("else", "Else");
        keyWords.put("for", "For");
        keyWords.put("continue", "Continue");
        keyWords.put("break", "Break");
        keyWords.put("int", "Type");
        keyWords.put("string", "Type");
        keyWords.put("double", "Type");
        keyWords.put("return", "Return");
        keyWords.put("void", "Void");
        keyWords.put("print", "Print");
        keyWords.put("scan", "Scan");
        keyWords.put("sqrt", "Sqrt");
        keyWords.put("pow", "Pow");
        operations = new TreeMap<String, String>();
        operations.put("+", "Plus");
        operations.put("-", "Minus");
        operations.put("/", "Div");
        operations.put("*", "Mul");
        operations.put("%", "Mod");
        brackets = new TreeMap<String, String>();
        brackets.put("(", "LRBracket");
        brackets.put(")", "RRBracket");
        brackets.put("{", "LCBracket");
        brackets.put("}", "RCBracket");
        comparators = new TreeMap<String, String>();
        comparators.put("==", "Equals");
        comparators.put("<", "Less");
        comparators.put(">", "Greater");
        comparators.put("<=", "LEquals");
        comparators.put(">=", "GEquals");
        logic = new TreeMap<String, String>();
        logic.put("&&", "And");
        logic.put("||", "Or");
    }

    public int getCurrentLine() {
        return currentLine;
    }

    void nextCharacter() {
        pos++;
        if (pos > text.length() - 1) {
            endOfText = true;
        }
        else {
            currentChar = text.charAt(pos);
        }
    }

    void skipWhiteSpace() {
        while (!endOfText && (Character.isSpaceChar(currentChar) || currentChar == '\n' || currentChar == '\t')) {
            if (currentChar == '\n') {
                currentLine++;
            }
            nextCharacter();
        }
    }

    String integerConst() throws IOException {
        String result = "";
        while (!endOfText && Character.isDigit(currentChar)) {
            result += currentChar;
            nextCharacter();
        }
        if (Character.isAlphabetic(currentChar)) {
            error("It was expected integer literal. Line: " + currentLine);
        }
        return result;
    }

    String identificator() {
        String result = "";
        while (!endOfText && Character.isAlphabetic(currentChar)) {
            result += currentChar;
            nextCharacter();
        }
        return result;
    }

    String stringConst() throws IOException {
        String result = "";
        int line = currentLine;
        nextCharacter();
        while(!endOfText && currentChar != '"') {
            result += currentChar;
            nextCharacter();
        }
        if (currentChar == '"') {
            result += currentChar;
            nextCharacter();
        } else {
            error("It was expected closing quote. Line: " + line);
        }
        return result;
    }

    void error(String message) throws IOException {
        throw new IOException(message);
    }

    public Token getNextToken() throws IOException { /* Лексический анализатор*/
        while(!endOfText) {
            String result = "";
            String key;

            if (Character.isSpaceChar(currentChar) || currentChar == '\n' || currentChar == '\t') {
                skipWhiteSpace();
                continue;
            }
            if (Character.isDigit(currentChar)) {
                result = integerConst();
                if (currentChar == '.') {
                    result += currentChar;
                    nextCharacter();
                    result += integerConst();
                    return new Token("ConstDouble", result);
                }
                return new Token("ConstInt", result);
            }
            if (currentChar == '"') {
                result = "\"" + stringConst();
                return new Token("ConstString", result);
            }
            if (Character.isAlphabetic(currentChar)) {
                result = identificator();
                if (keyWords.containsKey(result)) {
                    return new Token(keyWords.get(result), result);
                } else {

                    return new Token("ID", result);
                }

            }
            if (operations.containsKey(String.valueOf(currentChar))) {
                key = String.valueOf(currentChar);
                nextCharacter();
                return  new Token(operations.get(key), key);
            }
            if (brackets.containsKey(String.valueOf(currentChar))) {
                key = String.valueOf(currentChar);
                nextCharacter();
                return new Token(brackets.get(key), key);
            }
            if (currentChar == '>' || currentChar == '<' || currentChar == '=') {
                key = String.valueOf(currentChar);
                nextCharacter();
                if (comparators.containsKey(key + currentChar)) {
                    key += currentChar;
                    nextCharacter();
                }
                if (key.equals("=")) {
                    return new Token("Assign", "=");
                }
                return new Token(comparators.get(key), key);
            }
            if (currentChar == '|' || currentChar == '&') {
                key = String.valueOf(currentChar);
                nextCharacter();
                if (logic.containsKey(key + currentChar)) {
                    key += currentChar;
                    nextCharacter();
                    return new Token(logic.get(key), key);
                }
                error("Invalid symvol. Line: " + currentLine);
            }
            if (currentChar == ';') {
                nextCharacter();
                return new Token("Sep", ";");
            }
            if (currentChar == ',') {
                nextCharacter();
                return new Token("Comma", ",");
            }
            error("Invalid symbol. Line: " + currentLine);
        }
        return new Token("EOF", "None");
    }
}
