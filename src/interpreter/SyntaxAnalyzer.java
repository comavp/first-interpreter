package interpreter;

import java.io.IOException;
import java.util.*;

public class SyntaxAnalyzer {

    private LexicalAnalyzer lexer;
    private Token currentToken;
    private TreeSet<String> statements;
    private TreeSet<String> keyWords;
    public TreeMap<String, ArrayList<Identificator>> functionTable;
    public TreeMap<String, ArrayList<String>> prnTable;
    public TreeMap<String, TreeMap<String, Identificator>> idTable;
    public TreeMap<String, String> functionTypes;
    private ArrayList<Token> tokenTable;
    private TreeMap<String, Integer> operations;
    private String currentFunc;
    private String id;
    private String type;
    private boolean isCycle;

    public SyntaxAnalyzer(LexicalAnalyzer lexer) throws IOException {
        this.lexer = lexer;
        currentToken = lexer.getNextToken();
        statements = new TreeSet<String>();
        keyWords = new TreeSet<String>();
        functionTable = new TreeMap<String, ArrayList<Identificator>>();
        prnTable = new TreeMap<String, ArrayList<String>>();
        idTable = new TreeMap<String, TreeMap<String, Identificator>>();
        tokenTable = new ArrayList<Token>();
        functionTypes = new TreeMap<String, String>();
        operations = new TreeMap<String, Integer>();
        init();
    }

    void init() {
        statements.add("{");
        statements.add("While");
        statements.add("If");
        statements.add("For");
        statements.add("Continue");
        statements.add("Break");
        statements.add("ID");
        statements.add("Pow");
        statements.add("Sqrt");
        statements.add("Print");
        statements.add("Scan");
        statements.add("Return");
        statements.add("Type");

        keyWords.add("print");
        keyWords.add("scan");
        keyWords.add("sqrt");
        keyWords.add("pow");
        keyWords.add("return");
        keyWords.add("break");
        keyWords.add("continue");
        keyWords.add("if");
        keyWords.add("while");
        keyWords.add("else");
        keyWords.add("for");
        keyWords.add("void");
        keyWords.add("string");
        keyWords.add("double");
        keyWords.add("int");

        isCycle = false;

        operations.put("Plus", 4);
        operations.put("Minus", 4);
        operations.put("Mul", 5);
        operations.put("Mod", 5);
        operations.put("Div", 5);
        operations.put("Equals", 3);
        operations.put("Less", 3);
        operations.put("Greater", 3);
        operations.put("LEquals", 3);
        operations.put("GEquals", 3);
        operations.put("And", 1);
        operations.put("Or", 2);
        operations.put("Assign", 0);
        operations.put("UMinus", 6);
        operations.put("UPlus", 6);
    }

    void error(String message) throws IOException {
        throw new IOException(message);
    }

    void buildPrnTable() {
        Stack<Token> stack = new Stack<Token>();
        int flag = 0;
        Stack<String> openStatement = new Stack<String>();
        boolean isJzStatement = false;
        Integer whileJzSize = 0;
        Stack<Integer> address = new Stack<Integer>();
        Stack<Integer> whileJzAddress  = new Stack<Integer>();
        Stack<Integer> breakAddress = new Stack<Integer>();
        Stack<Integer> continueAddress = new Stack<Integer>();
        openStatement.push("Block");

        for (int i = 0; i < tokenTable.size(); i++) {
            if (!tokenTable.get(i).getType().equals("Type")) {
                if (tokenTable.get(i).getType().equals("ConstInt") || tokenTable.get(i).getType().equals("ConstDouble")
                        || tokenTable.get(i).getType().equals("ConstString")) {
                    prnTable.get(currentFunc).add(tokenTable.get(i).getValue());
                }
                if (tokenTable.get(i).getType().equals("Pow") || tokenTable.get(i).getType().equals("Sqrt") || tokenTable.get(i).getType().equals("Print")
                        || tokenTable.get(i).getType().equals("Scan") || functionTable.containsKey(tokenTable.get(i).getValue()) ){
                    stack.push(tokenTable.get(i));
                }
                if (tokenTable.get(i).getType().equals("Comma")) {
                    if (stack.size() != 0) {
                        Token s = stack.peek();
                        while (!s.getType().equals("LRBracket")) {
                            prnTable.get(currentFunc).add(s.getValue());
                            s = stack.pop();
                        }
                    }
                }
                if (tokenTable.get(i).getType().equals("ID")) {
                    if(!functionTable.containsKey(tokenTable.get(i).getValue())) {
                        prnTable.get(currentFunc).add(tokenTable.get(i).getValue());
                    }
                }
                if (tokenTable.get(i).getType().equals("LRBracket")) {
                    stack.push(tokenTable.get(i));
                }
                if (tokenTable.get(i).getType().equals("RRBracket")) {
                    Token s = stack.pop();
                    while(!s.getType().equals("LRBracket")) {
                        prnTable.get(currentFunc).add(s.getValue());
                        s = stack.pop();
                    }
                    if (stack.size() != 0) {
                        if (functionTypes.containsKey(stack.peek().getValue()) || stack.peek().getType().equals("Pow") ||
                                stack.peek().getType().equals("Sqrt") || stack.peek().getType().equals("Print")
                                || stack.peek().getType().equals("Scan")) {
                            prnTable.get(currentFunc).add(stack.pop().getValue());
                        }
                    }
                    if (isJzStatement) {
                        prnTable.get(currentFunc).add("placeForAddress");
                        address.push(prnTable.get(currentFunc).size() - 1);
                        prnTable.get(currentFunc).add("jz");
                        if (openStatement.peek().equals("While")) {
                            whileJzAddress.push(prnTable.get(currentFunc).size()  - whileJzSize);
                            whileJzSize = 0;
                        }
                        isJzStatement = false;
                    }
                }
                if (tokenTable.get(i).getType().equals("Break")) {
                    prnTable.get(currentFunc).add("placeForAddress");
                    breakAddress.push(prnTable.get(currentFunc).size() - 1);
                    prnTable.get(currentFunc).add("jmp");
                }
                if (tokenTable.get(i).getType().equals("Continue")) {
                    prnTable.get(currentFunc).add("placeForAddress");
                    continueAddress.push(prnTable.get(currentFunc).size() - 1);
                    prnTable.get(currentFunc).add("jmp");
                }
                if (tokenTable.get(i).getType().equals("While")) {
                    isJzStatement = true;
                    openStatement.push("While");
                }
                if (tokenTable.get(i).getType().equals("For")) {
                }
                if (tokenTable.get(i).getType().equals("If")) {
                    isJzStatement = true;
                    openStatement.push("If");
                }
                if (tokenTable.get(i).getType().equals("Else")) {
                    openStatement.push("Else");
                    prnTable.get(currentFunc).add("placeForAddress");
                    address.push(prnTable.get(currentFunc).size() - 1);
                    prnTable.get(currentFunc).add("jmp");
                }
                if (tokenTable.get(i).getType().equals("LCBracket")) {
                    if (!tokenTable.get(i - 1).getType().equals("RRBracket") && !tokenTable.get(i - 1).getType().equals("Else")) {
                        openStatement.push("Block");
                    }
                }
                if (tokenTable.get(i).getType().equals("RCBracket")) {
                    if (openStatement.peek().equals("Block")) {
                        openStatement.pop();
                        continue;
                    }
                    Integer addr = address.pop();
                    String stat = openStatement.pop();
                    if (stat.equals("If")) {
                        if (tokenTable.get(i + 1).getType().equals("Else")) {
                            prnTable.get(currentFunc).set(addr, String.valueOf(prnTable.get(currentFunc).size() + 2));
                        } else {
                            prnTable.get(currentFunc).set(addr, String.valueOf(prnTable.get(currentFunc).size()));
                        }
                    }
                    if(stat.equals("Else")) {
                        prnTable.get(currentFunc).set(addr, String.valueOf(prnTable.get(currentFunc).size()));
                    }
                    if (stat.equals("While")) {
                        Integer jzAddress = whileJzAddress.pop();
                        prnTable.get(currentFunc).add(String.valueOf(jzAddress));
                        address.push(prnTable.get(currentFunc).size() + 1);
                        prnTable.get(currentFunc).add("jmp");
                        if (continueAddress.size() != 0) {
                            prnTable.get(currentFunc).set(continueAddress.pop(), String.valueOf(jzAddress));
                        }
                        if (breakAddress.size() != 0) {
                            prnTable.get(currentFunc).set(breakAddress.pop(), String.valueOf(prnTable.get(currentFunc).size()));
                        }
                        prnTable.get(currentFunc).set(addr, String.valueOf(prnTable.get(currentFunc).size()));
                    }
                }

                if (operations.containsKey(tokenTable.get(i).getType())) {
                    if (tokenTable.get(i).getType().equals("Minus")) {
                        if (!tokenTable.get(i - 1).getType().equals("ID") && !tokenTable.get(i - 1).getType().equals("RRBracket") &&
                                !tokenTable.get(i - 1).getType().equals("ConstInt") && !tokenTable.get(i - 1).getType().equals("ConstDouble")
                                && !tokenTable.get(i - 1).getType().equals("ConstString")) {
                            tokenTable.get(i).setType("UMinus");
                            tokenTable.get(i).setValue("u-");
                        }
                    }
                    if (tokenTable.get(i).getType().equals("Plus")) {
                        if (!tokenTable.get(i - 1).getType().equals("ID") && !tokenTable.get(i - 1).getType().equals("RRBracket") &&
                                !tokenTable.get(i - 1).getType().equals("ConstInt") && !tokenTable.get(i - 1).getType().equals("ConstDouble")
                                && !tokenTable.get(i - 1).getType().equals("ConstString")) {
                            tokenTable.get(i).setType("UPlus");
                            tokenTable.get(i).setValue("u+");
                        }
                    }
                    if (stack.size() != 0) {
                        while (operations.containsKey(stack.peek().getType())) {
                            if (operations.get(stack.peek().getType()) >= operations.get(tokenTable.get(i).getType())) {
                                prnTable.get(currentFunc).add(stack.pop().getValue());
                            } else {
                                stack.push(tokenTable.get(i));
                                break;
                            }
                            if (stack.size() == 0) {
                                break;
                            }
                        }
                        if (stack.peek().getValue().equals("(")) {
                            stack.push(tokenTable.get(i));
                        }
                    } else {
                        stack.push(tokenTable.get(i));
                    }

                }
            }
            if (openStatement.size() != 0) {
                if (isJzStatement && openStatement.peek().equals("While")) {
                    whileJzSize++;
                }
            }
            if (tokenTable.get(i).getType().equals("Sep")) {
                while (!stack.empty()) {
                    prnTable.get(currentFunc).add(stack.pop().getValue());
                }
            }
        }
        tokenTable.clear();
    }

    boolean getCertainToken(String type) throws IOException {
        currentToken = lexer.getNextToken();
        return currentToken.getType().equals(type);
    }

    public void program() throws IOException {
        while (currentToken.getType().equals("Void") || currentToken.getType().equals("Type")) {
            funcDefinition();
            currentToken = lexer.getNextToken();
        }
        if (!currentToken.getType().equals("EOF")) {
            error("It was expected \"int\", \"double\", \"string\" or \"void\". Line: " + lexer.getCurrentLine());
        }
    }

    void funcDefinition() throws IOException {
        type = currentToken.getValue();

        if (!getCertainToken("ID")) {
            error("It was expected identeficator. Line: " + lexer.getCurrentLine());
        }

        currentFunc = currentToken.getValue();
        if (keyWords.contains(currentFunc)) {
            error("Incorrect identificator. Line" + lexer.getCurrentLine());
        }
        functionTypes.put(currentFunc, type);
        functionTable.put(currentFunc, new ArrayList<Identificator>());
        idTable.put(currentFunc, new TreeMap<String, Identificator>());
        prnTable.put(currentFunc, new ArrayList<String>());

        if (!getCertainToken("LRBracket")) {
            error("It was expected \"(\". Line: " + lexer.getCurrentLine());
        }
        if (!getCertainToken("RRBracket")) {
            formalParameters();
            if (!currentToken.getType().equals("RRBracket")) {
                error("It was expected \")\". Line: " + lexer.getCurrentLine());
            }
        }
        if (!getCertainToken("LCBracket")) {
            error("It was expected \"{\". Line: " + lexer.getCurrentLine());
        }
        block();
        buildPrnTable();
    }

    void formalParameters() throws IOException {
        var();
        currentToken = lexer.getNextToken();
        while (currentToken.getType().equals("Comma")) {
            currentToken = lexer.getNextToken();
            var();
            currentToken = lexer.getNextToken();
        }
    }

    void var() throws IOException {
        if (!currentToken.getType().equals("Type")) {
            error("It was expected type. Line: " + lexer.getCurrentLine());
        }
        type = currentToken.getValue();

        if (!getCertainToken("ID")) {
            error("It was expected identeficator. Line: " + lexer.getCurrentLine());
        }

        id = currentToken.getValue();
        functionTable.get(currentFunc).add(new Identificator(type, id, ""));
        if (idTable.get(currentFunc).containsKey(id)) {
            error("Identificator is already exist. Line: " + lexer.getCurrentLine());
        }
        idTable.get(currentFunc).put(id, new Identificator(type, "", ""));
    }

    void statement() throws IOException {
        switch (currentToken.getType()) {
            case "While":
                whileStatement();
                break;
            case "If":
                ifStatement();
                break;
            case "For":
                forStatement();
                break;
            case "Print":
                printStatement();
                break;
            case "Scan":
                scanStatement();
                break;
            case "Pow":
                powStatement();
                break;
            case "Sqrt":
                sqrtStatement();
                break;
            case "Break":
                if (!getCertainToken("Sep")) {
                    error("It was expected \";\". Line: " + lexer.getCurrentLine());
                }
                if (!isCycle) {
                    error("It was expected \"while\" or \"for\". Line: " + lexer.getCurrentLine());
                }
                tokenTable.add(currentToken);
                break;
            case "Continue":
                if (!getCertainToken("Sep")) {
                    error("It was expected \";\". Line: " + lexer.getCurrentLine());
                }
                if (!isCycle) {
                    error("It was expected \"while\" or \"for\". Line: " + lexer.getCurrentLine());
                }
                tokenTable.add(currentToken);
                break;
            case "Return":
                returnStatement();
                break;
            case "ID":
                boolean isLibFun;
                String idFunc;
                if (!idTable.get(currentFunc).containsKey(currentToken.getValue())) {
                    error("Can not find \"" + currentToken.getValue() + "\". Line: " + lexer.getCurrentLine());
                }
                if (keyWords.contains(currentToken.getValue())) {
                    isLibFun = true;
                } else {
                    isLibFun = false;
                }
                idFunc = currentToken.getValue();
                currentToken = lexer.getNextToken();
                if (!currentToken.getType().equals("Assign")) {
                    tokenTable.add(currentToken);
                }

                if (!currentToken.getType().equals("LRBracket") && !currentToken.getType().equals("Assign")) {
                    error("It was expected \"(\" or \"=\". Line: " + lexer.getCurrentLine());
                } else {
                    if (currentToken.getType().equals("LRBracket")) {
                        funcCall(isLibFun, idFunc);
                        if (!getCertainToken("Sep")) {
                            error("It was expcted \";\". Line: " + lexer.getCurrentLine());
                        }
                        tokenTable.add(currentToken);
                    }
                    if (currentToken.getType().equals("Assign")) {
                        varStatemant();
                    }
                }
                break;
            case "Type":
                varStatemant();
                break;
            case "{":
                block();
                break;
            default:
                error("It was exptected statement. Line: " + lexer.getCurrentLine());
        }
    }

    void assignment() throws IOException {
        tokenTable.add(currentToken);
        currentToken = lexer.getNextToken();
        tokenTable.add(currentToken);
        expr();
    }

    void block() throws IOException {
        currentToken = lexer.getNextToken();
        tokenTable.add(currentToken);
        while (!currentToken.getType().equals("RCBracket")) {
            if (statements.contains(currentToken.getType())) {
                statement();
            } else {
                expr();
                if (!getCertainToken("Sep")) {
                    error("It was expected \";\". Line: " + lexer.getCurrentLine());
                }
                tokenTable.add(currentToken);
            }
            if (!statements.contains(currentToken.getType())) {
                currentToken = lexer.getNextToken();
            }
            tokenTable.add(currentToken);
        }
    }

    void varStatemant() throws IOException {
        if (currentToken.getType().equals("Assign")) {
            assignment();
        }

        if (currentToken.getType().equals("Type")) {
            type = currentToken.getValue();
            if (!getCertainToken("ID")) {
                error("It was expected identificator. Line: " + lexer.getCurrentLine());
            }
            tokenTable.add(currentToken);

            id = currentToken.getValue();
            if (keyWords.contains(id)) {
                error("Incorrect identificator. Line" + lexer.getCurrentLine());
            }
            if (idTable.get(currentFunc).containsKey(id)) {
                error("Identificator already exists" + lexer.getCurrentLine());
            }
            if (idTable.containsKey(id)) {
                error("Function with this name already exists");
            }
            idTable.get(currentFunc).put(id, new Identificator(type, "", ""));

            currentToken = lexer.getNextToken();
            if (!currentToken.getType().equals("Assign")) {
                tokenTable.add(currentToken);
            }
            if (!currentToken.getType().equals("Assign") && !currentToken.getType().equals("Sep") && !currentToken.getType().equals("Comma")) {
                error("It was expected \";\" or \"=\". Line: " + lexer.getCurrentLine());
            } else {
                if (!currentToken.getType().equals("Comma")) {
                    if (currentToken.getType().equals("Assign")) {
                        assignment();
                    } else {
                        return;
                    }
                }
            }
        }
        while (currentToken.getType().equals("Comma")) {
            if (!getCertainToken("ID")) {
                error("It was expected identificator. Line: " + lexer.getCurrentLine());
            }
            tokenTable.add(currentToken);

            id = currentToken.getValue();
            if (idTable.get(currentFunc).containsKey(id)) {
                error("Identificator is already exist" + lexer.getCurrentLine());
            }
            idTable.get(currentFunc).put(id, new Identificator(type, "", ""));

            currentToken = lexer.getNextToken();
            tokenTable.add(currentToken);
            if (currentToken.getType().equals("Assign")) {
                currentToken = lexer.getNextToken();
                tokenTable.add(currentToken);
                assignment();
            }
        }
        if (!currentToken.getType().equals("Sep")) {
            error("It was expected \";\". Line: " + lexer.getCurrentLine());
        }
    }

    void whileStatement() throws IOException {
        isCycle = true;
        if (!getCertainToken("LRBracket")) {
            error("It was expected \"(\". Line: " + lexer.getCurrentLine());
        }
        tokenTable.add(currentToken);
        currentToken = lexer.getNextToken();
        tokenTable.add(currentToken);
        expr();
        if (!currentToken.getType().equals("RRBracket")) {
            error("It was expected \")\". Line: " + lexer.getCurrentLine());
        }
//        tokenTable.add(currentToken);
        if (!getCertainToken("LCBracket")) {
            error("It was expected \"{\". Line: " + lexer.getCurrentLine());
        }
        tokenTable.add(currentToken);
        block();
        isCycle = false;
    }

    void ifStatement() throws IOException {
        if (!getCertainToken("LRBracket")) {
            error("It was expected \"(\". Line: " + lexer.getCurrentLine());
        }
        tokenTable.add(currentToken);
        currentToken = lexer.getNextToken();
        tokenTable.add(currentToken);
        expr();
        if (!currentToken.getType().equals("RRBracket")) {
            error("It was expected \")\". Line: " + lexer.getCurrentLine());
        }
        if (!getCertainToken("LCBracket")) {
            error("It was expected \"{\". Line: " + lexer.getCurrentLine());
        }
        tokenTable.add(currentToken);
        block();
        if (getCertainToken("Else")) {
            tokenTable.add(currentToken);
            if (!getCertainToken("LCBracket")) {
                error("It was expected \"{\". Line: " + lexer.getCurrentLine());
            }
            tokenTable.add(currentToken);
            block();
        }
    }

    void printStatement() throws IOException {
        if (!getCertainToken("LRBracket")) {
            error("It was expected \"(\". Line: " + lexer.getCurrentLine());
        }
        tokenTable.add(currentToken);
        currentToken = lexer.getNextToken();
        tokenTable.add(currentToken);
        expr();
        if (!currentToken.getType().equals("RRBracket")) {
            error("It was expected \")\". Line: " + lexer.getCurrentLine());
        }
        if (!getCertainToken("Sep")) {
            error("It was expected \";\". Line: " + lexer.getCurrentLine());
        }
        tokenTable.add(currentToken);
    }

    void scanStatement() throws IOException {
        if (!getCertainToken("LRBracket")) {
            error("It was expected \"(\". Line: " + lexer.getCurrentLine());
        }
        tokenTable.add(currentToken);
        if (!getCertainToken("ID")) {
            error("It was expected identificator. Line: " + lexer.getCurrentLine());
        }
        if (!idTable.get(currentFunc).containsKey(currentToken.getValue())) {
            error("Can not find \"" + currentToken.getValue() + "\". Line: " + lexer.getCurrentLine());
        }
        if (keyWords.contains(currentToken.getValue()) && functionTypes.containsKey(currentToken.getValue())) {
            error("Wrong identificator. Line: " + lexer.getCurrentLine());
        }
        tokenTable.add(currentToken);
        if (!getCertainToken("RRBracket")) {
            error("It was expected \")\". Line: " + lexer.getCurrentLine());
        }
        tokenTable.add(currentToken);
        if (!getCertainToken("Sep")) {
            error("It was expected \";\". Line: " + lexer.getCurrentLine());
        }
        tokenTable.add(currentToken);
    }

    void forStatement() throws IOException {
        isCycle = true;
        if (!getCertainToken("LRBracket")) {
            error("It was expected \"(\". Line: " + lexer.getCurrentLine());
        }
        tokenTable.add(currentToken);
        currentToken = lexer.getNextToken();
        tokenTable.add(currentToken);
        expr();
        if (!currentToken.getType().equals("Sep")) {
            error("It was expected \";\". Line: " + lexer.getCurrentLine());
        }
        currentToken = lexer.getNextToken();
        tokenTable.add(currentToken);
        expr();
        if (!currentToken.getType().equals("Sep")) {
            error("It was expected \";\". Line: " + lexer.getCurrentLine());
        }
        currentToken = lexer.getNextToken();
        tokenTable.add(currentToken);
        expr();
        if (!currentToken.getType().equals("RRBracket")) {
            error("It was expected \")\". Line: " + lexer.getCurrentLine());
        }
        if (!getCertainToken("LCBracket")) {
            error("It was expected \"{\". Line: " + lexer.getCurrentLine());
        }
        tokenTable.add(currentToken);
        block();
        isCycle = false;
    }

    void powStatement() throws IOException {
        if (!getCertainToken("LRBracket")) {
            error("It was expected \"(\". Line: " + lexer.getCurrentLine());
        }
        tokenTable.add(currentToken);
        currentToken = lexer.getNextToken();
        tokenTable.add(currentToken);
        expr();
        if (!currentToken.getType().equals("Comma")) {
            error("It was expected \",\". Line: " + lexer.getCurrentLine());
        }
        currentToken = lexer.getNextToken();
        tokenTable.add(currentToken);
        expr();
        if (!currentToken.getType().equals("RRBracket")) {
            error("It was expected \")\". Line: " + lexer.getCurrentLine());
        }
        if (!getCertainToken("Sep")) {
            error("It was expected \";\". Line: " + lexer.getCurrentLine());
        }
        tokenTable.add(currentToken);
    }

    void sqrtStatement() throws IOException {
        if (!getCertainToken("LRBracket")) {
            error("It was expected \"(\". Line: " + lexer.getCurrentLine());
        }
        tokenTable.add(currentToken);
        currentToken = lexer.getNextToken();
        tokenTable.add(currentToken);
        expr();
        if (!currentToken.getType().equals("RRBracket")) {
            error("It was expected \")\". Line: " + lexer.getCurrentLine());
        }
        if (!getCertainToken("Sep")) {
            error("It was expected \";\". Line: " + lexer.getCurrentLine());
        }
        tokenTable.add(currentToken);
    }

    void returnStatement() throws IOException {
        String exprType;
        currentToken = lexer.getNextToken();
        tokenTable.add(currentToken);
        exprType = expr();
        if (!exprType.equals(functionTypes.get(currentFunc))) {
            error("Wrong type of the expression. It was expected " + functionTypes.get(currentFunc) + ". Line: " + lexer.getCurrentLine());
        }
        if (!currentToken.getType().equals("Sep")) {
            error("It was expected \";\". Line: " + lexer.getCurrentLine());
        }
    }

    void funcCall(boolean isLibFun, String idFunc) throws IOException {
        String exprType;
        ArrayList<Identificator> par = functionTable.get(idFunc);
        int numberOfPar = 0;

        currentToken = lexer.getNextToken();
        tokenTable.add(currentToken);
        if (!currentToken.getType().equals("RRBracket")) {
            exprType = expr();
            if (!isLibFun) {
                if (!exprType.equals(par.get(numberOfPar).getType())) {
                    error("Can not find function with such parameters. Line: " + lexer.getCurrentLine());
                }
            }
            numberOfPar++;
            while (currentToken.getType().equals("Comma")) {
                currentToken = lexer.getNextToken();
                tokenTable.add(currentToken);
                exprType = expr();
                if (!isLibFun) {
                    if (numberOfPar >= par.size() || !exprType.equals(par.get(numberOfPar).getType())) {
                        error("Can not find function with such parameters. Line: " + lexer.getCurrentLine());
                    }
                }
                numberOfPar++;
            }
            if (!currentToken.getType().equals("RRBracket")) {
                error("It was expected \")\". Line: " + lexer.getCurrentLine());
            }
        }
    }

    String expr() throws IOException {
        String exprType = "int";
        exprType = relExpr();
        while (currentToken.getType().equals("And") || currentToken.getType().equals("Or")) {
            currentToken = lexer.getNextToken();
            tokenTable.add(currentToken);
            exprType = relExpr();
        }
        return exprType;
    }

    String relExpr() throws IOException {
        String relExprType = "int";
        relExprType = arithExpr();
        while (currentToken.getType().equals("Equals") || currentToken.getType().equals("Less") || currentToken.getType().equals("Greater")
                || currentToken.getType().equals("LEquals") || currentToken.getType().equals("GEquals")) {
            currentToken = lexer.getNextToken();
            tokenTable.add(currentToken);
            relExprType = arithExpr();
        }
        return relExprType;
    }

    String arithExpr() throws IOException {
        String arithExprType = "int";
        arithExprType = term();
        while(currentToken.getType().equals("Plus") || currentToken.getType().equals("Minus")) {
            currentToken = lexer.getNextToken();
            tokenTable.add(currentToken);
            arithExprType = term();
        }
        return arithExprType;
    }

    String term() throws IOException {
        String termType = "int";
        termType = factor();
        while(currentToken.getType().equals("Mul") || currentToken.getType().equals("Div") || currentToken.getType().equals("Mod")) {
            currentToken = lexer.getNextToken();
            tokenTable.add(currentToken);
            if (termType.equals("string")) {
                error("Wrong type of the expression. It was expected \"int\" or \"double\". Line: " + lexer.getCurrentLine());
            }
            termType = factor();
        }
        return termType;
    }

    String factor() throws IOException {
        String factorType = "int";
        switch (currentToken.getType()) {
            case "Plus":
                currentToken = lexer.getNextToken();
                tokenTable.add(currentToken);
                factorType = factor();
                if (factorType.equals("string")) {
                    error("Wrong type of the expression. It was expected \"int\" or \"double\". Line: " + lexer.getCurrentLine());
                }
                break;
            case "Minus":
                currentToken = lexer.getNextToken();
                tokenTable.add(currentToken);
                factorType = factor();
                if (factorType.equals("string")) {
                    error("Wrong type of the expression. It was expected \"int\" or \"double\". Line: " + lexer.getCurrentLine());
                }
                break;
            case "LRBracket":
                currentToken = lexer.getNextToken();
                tokenTable.add(currentToken);
                factorType = expr();
                if (!currentToken.getType().equals("RRBracket")) {
                    error("It was expected \")\". Line: " + lexer.getCurrentLine());
                }
                currentToken = lexer.getNextToken();
                tokenTable.add(currentToken);
                break;
            case "ConstInt":
                currentToken = lexer.getNextToken();
                tokenTable.add(currentToken);
                break;
            case "ConstDouble":
                currentToken = lexer.getNextToken();
                tokenTable.add(currentToken);
                factorType = "double";
                break;
            case "ConstString":
                currentToken = lexer.getNextToken();
                tokenTable.add(currentToken);
                factorType = "string";
                break;
            case "Pow":
            case "Sqrt":
            case "Scan":
            case "Print":
            case "ID":
                boolean isLibFun;
                String idFunc;
                if (!idTable.get(currentFunc).containsKey(currentToken.getValue()) && !keyWords.contains(currentToken.getValue()) &&
                        !functionTypes.containsKey(currentToken.getValue())) {
                    error("Can not find \"" + currentToken.getValue() + "\". Line: " + lexer.getCurrentLine());
                }
                if (!keyWords.contains(currentToken.getValue())) {
                    if (functionTable.containsKey(currentToken.getValue())) {
                        factorType = functionTypes.get(currentToken.getValue());
                    } else {
                        factorType = idTable.get(currentFunc).get(currentToken.getValue()).getType();
                    }
                }
                if (keyWords.contains(currentToken.getValue())) {
                    isLibFun = true;
                } else {
                    isLibFun = false;
                }
                idFunc = currentToken.getValue();
                currentToken = lexer.getNextToken();
                tokenTable.add(currentToken);
                if (currentToken.getType().equals("LRBracket")) {
                    funcCall(isLibFun, idFunc);
                    currentToken = lexer.getNextToken();
                    tokenTable.add(currentToken);
                }
        }
        return factorType;
    }
}
