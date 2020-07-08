package interpreter;

import org.omg.PortableInterceptor.INACTIVE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;
import java.util.TreeMap;
import java.util.Scanner;

public class Interpreter {

    SyntaxAnalyzer parser;
    Stack<String> stack;
    private TreeMap<String, ArrayList<String>> prnTable;
    private TreeMap<String, TreeMap<String, Identificator>> idTable;
    private TreeMap<String, ArrayList<Identificator>> functionTable;
    private TreeMap<String, String> functionTypes;

    public Interpreter(SyntaxAnalyzer parser) {
        this.parser = parser;
        prnTable = parser.prnTable;
        idTable = parser.idTable;
        functionTable = parser.functionTable;
        stack = new Stack<String>();
        functionTypes = parser.functionTypes;
    }

    void error(String message) throws IOException {
        throw new IOException(message);
    }

    public void start() throws IOException {
        parser.program();
        if (!idTable.containsKey("main")) {
            error("\"Main\" is not found.");
        }
        execute("main");
    }

    void execute(String currentFunc) throws IOException {
        String returnValue;
        String firstOp, secondOp;
        Scanner in = new Scanner(System.in);
        for ( int ind = 0; ind < prnTable.get(currentFunc).size(); ind++) {
            String currentSymvol = prnTable.get(currentFunc).get(ind);
            if (currentSymvol.equals("pow")) {
                firstOp = stack.pop();
                if (idTable.get(currentFunc).containsKey(firstOp)) {
                    firstOp = idTable.get(currentFunc).get(firstOp).getValue();
                }
                secondOp = stack.pop();
                if (idTable.get(currentFunc).containsKey(secondOp)) {
                    secondOp = idTable.get(currentFunc).get(secondOp).getValue();
                }
                stack.push(String.valueOf(Math.pow(Double.parseDouble(secondOp), Double.parseDouble(firstOp))));
                continue;
            }
            if (currentSymvol.equals("sqrt")) {
                firstOp = stack.pop();
                if (idTable.get(currentFunc).containsKey(firstOp)) {
                    firstOp = idTable.get(currentFunc).get(firstOp).getValue();
                }
                stack.push(String.valueOf(Math.sqrt(Double.parseDouble(firstOp))));
                continue;
            }
            if (currentSymvol.equals("print")) {
                firstOp = stack.pop();
                if (idTable.get(currentFunc).containsKey(firstOp)) {
                    firstOp = idTable.get(currentFunc).get(firstOp).getValue();
                }
                System.out.println(firstOp);
                continue;
            }
            if(currentSymvol.equals("scan")) {
                firstOp = stack.pop();
                if(idTable.get(currentFunc).get(firstOp).getType().equals("int")) {
                    idTable.get(currentFunc).get(firstOp).setValue(Integer.toString(in.nextInt()));
                }
                if(idTable.get(currentFunc).get(firstOp).getType().equals("double")) {
                    idTable.get(currentFunc).get(firstOp).setValue(Double.toString(in.nextDouble()));
                }
                if(idTable.get(currentFunc).get(firstOp).getType().equals("string")) {
                    idTable.get(currentFunc).get(firstOp).setValue(in.nextLine());
                }
                continue;
            }
            if (currentSymvol.equals("u+")) {
                firstOp = stack.pop();
                if (!idTable.get(currentFunc).containsKey(firstOp)) {
                    firstOp = String.valueOf(+Double.parseDouble(firstOp));
                }
                stack.push(firstOp);
                continue;
            }
            if (currentSymvol.equals("u-")) {
                firstOp = stack.pop();
                if (idTable.get(currentFunc).containsKey(firstOp)) {
                    firstOp = idTable.get(currentFunc).get(firstOp).getValue();
                }
                try {
                    firstOp = String.valueOf(-Integer.parseInt(firstOp));
                }
                catch (NumberFormatException e) {
                    firstOp = String.valueOf(-Double.parseDouble(firstOp));
                }
                stack.push(firstOp);
                continue;
            }
            if (currentSymvol.equals("+")) {
                firstOp = stack.pop();
                if (idTable.get(currentFunc).containsKey(firstOp)) {
                    firstOp = idTable.get(currentFunc).get(firstOp).getValue();
                }
                secondOp = stack.pop();
                if (idTable.get(currentFunc).containsKey(secondOp)) {
                    secondOp = idTable.get(currentFunc).get(secondOp).getValue();
                }
                if (idTable.get(currentFunc).containsKey(firstOp) && idTable.get(currentFunc).containsKey(secondOp)) {
                    if (idTable.get(currentFunc).get(firstOp).getType().equals("string") || idTable.get(currentFunc).get(firstOp).getType().equals("string")) {
                        stack.push(firstOp + secondOp);
                    } else {
                        if (idTable.get(currentFunc).get(firstOp).getType().equals("double") || idTable.get(currentFunc).get(firstOp).getType().equals("double")) {
                            stack.push(String.valueOf(Double.parseDouble(firstOp) + Double.parseDouble(secondOp)));
                        } else {
                            stack.push(String.valueOf(Integer.parseInt(firstOp) + Integer.parseInt(secondOp)));
                        }
                    }
                } else {
                        try {
                            stack.push(String.valueOf(Integer.parseInt(firstOp)) + Integer.parseInt(secondOp));
                        }
                        catch (NumberFormatException e) {
                            try {
                                stack.push(String.valueOf(Double.parseDouble(firstOp) + Double.parseDouble(secondOp)));
                            } catch (NumberFormatException eS) {
                                if (firstOp.charAt(0) == '"') {
                                    firstOp = firstOp.substring(1, firstOp.length() - 1);
                                }
                                if (secondOp.charAt(0) == '"') {
                                    secondOp = secondOp.substring(1, secondOp.length() - 1);
                                }
                                stack.push(secondOp + firstOp);
                            }
                        }
                }
                continue;
            }
            if (currentSymvol.equals("-")) {
                firstOp = stack.pop();
                if (idTable.get(currentFunc).containsKey(firstOp)) {
                    firstOp = idTable.get(currentFunc).get(firstOp).getValue();
                }
                secondOp = stack.pop();
                if (idTable.get(currentFunc).containsKey(secondOp)) {
                    secondOp = idTable.get(currentFunc).get(secondOp).getValue();
                }
                if (idTable.get(currentFunc).containsKey(firstOp) && idTable.get(currentFunc).containsKey(secondOp)) {
                    if (idTable.get(currentFunc).get(firstOp).getType().equals("double") || idTable.get(currentFunc).get(firstOp).getType().equals("double")) {
                        stack.push(String.valueOf(Double.parseDouble(secondOp) - Double.parseDouble(firstOp)));
                    } else {
                        stack.push(String.valueOf(Integer.parseInt(secondOp) - Integer.parseInt(firstOp)));
                    }
                } else {
                    try {
                        stack.push(String.valueOf(Integer.parseInt(secondOp) - Integer.parseInt(firstOp)));
                    }
                    catch (NumberFormatException e) {
                        stack.push(String.valueOf(Double.parseDouble(secondOp) - Double.parseDouble(firstOp)));
                    }
                }
                continue;
            }
            if (currentSymvol.equals("*")) {
                firstOp = stack.pop();
                if (idTable.get(currentFunc).containsKey(firstOp)) {
                    firstOp = idTable.get(currentFunc).get(firstOp).getValue();
                }
                secondOp = stack.pop();
                if (idTable.get(currentFunc).containsKey(secondOp)) {
                    secondOp = idTable.get(currentFunc).get(secondOp).getValue();
                }
                if (idTable.get(currentFunc).containsKey(firstOp) && idTable.get(currentFunc).containsKey(secondOp)) {
                    if (idTable.get(currentFunc).get(firstOp).getType().equals("double") || idTable.get(currentFunc).get(firstOp).getType().equals("double")) {
                        stack.push(String.valueOf(Double.parseDouble(firstOp) * Double.parseDouble(secondOp)));
                    } else {
                        stack.push(String.valueOf(Integer.parseInt(firstOp) * Integer.parseInt(secondOp)));
                    }
                } else {
                    try {
                        stack.push(String.valueOf(Integer.parseInt(secondOp) * Integer.parseInt(secondOp)));
                    }
                    catch (NumberFormatException e) {
                        stack.push(String.valueOf(Double.parseDouble(secondOp) * Double.parseDouble(firstOp)));
                    }
                }
                continue;
            }
            if (currentSymvol.equals("/")) {
                firstOp = stack.pop();
                if (idTable.get(currentFunc).containsKey(firstOp)) {
                    firstOp = idTable.get(currentFunc).get(firstOp).getValue();
                }
                secondOp = stack.pop();
                if (idTable.get(currentFunc).containsKey(secondOp)) {
                    secondOp = idTable.get(currentFunc).get(secondOp).getValue();
                }
                stack.push(String.valueOf(Double.parseDouble(secondOp) / Double.parseDouble(firstOp)));
                continue;
            }
            if (currentSymvol.equals("%")) {
                firstOp = stack.pop();
                if (idTable.get(currentFunc).containsKey(firstOp)) {
                    firstOp = idTable.get(currentFunc).get(firstOp).getValue();
                }
                secondOp = stack.pop();
                if (idTable.get(currentFunc).containsKey(secondOp)) {
                    secondOp = idTable.get(currentFunc).get(secondOp).getValue();
                }
                if (Integer.parseInt(secondOp) > 0) {
                    stack.push(String.valueOf(Integer.parseInt(secondOp) % Integer.parseInt(firstOp)));
                } else {
                    stack.push(String.valueOf(Math.floorMod(Integer.parseInt(secondOp), Integer.parseInt(firstOp))));
                }
                continue;
            }
            if (currentSymvol.equals("=")) {
                firstOp = stack.pop();
                secondOp = stack.pop();
                if (idTable.get(currentFunc).containsKey(firstOp)) {
                    firstOp = idTable.get(currentFunc).get(firstOp).getValue();
                }
                idTable.get(currentFunc).get(secondOp).setValue(firstOp);
                continue;
            }
            if (currentSymvol.equals("==")) {
                firstOp = stack.pop();
                if (idTable.get(currentFunc).containsKey(firstOp)) {
                    firstOp = idTable.get(currentFunc).get(firstOp).getValue();
                }
                secondOp = stack.pop();
                if (idTable.get(currentFunc).containsKey(secondOp)) {
                    secondOp = idTable.get(currentFunc).get(secondOp).getValue();
                }
                if (secondOp.equals(firstOp)) {
                    stack.push("1");
                } else {
                    stack.push("0");
                }
                continue;
            }
            if (currentSymvol.equals(">")) {
                firstOp = stack.pop();
                if (idTable.get(currentFunc).containsKey(firstOp)) {
                    firstOp = idTable.get(currentFunc).get(firstOp).getValue();
                }
                secondOp = stack.pop();
                if (idTable.get(currentFunc).containsKey(secondOp)) {
                    secondOp = idTable.get(currentFunc).get(secondOp).getValue();
                }
                try {
                    if (Double.parseDouble(secondOp) > Double.parseDouble(firstOp)) {
                        stack.push("1");
                    } else {
                        stack.push("0");
                    }
                }
                catch (NumberFormatException e) {
                    if (secondOp.compareTo(firstOp) > 0) {
                        stack.push("1");
                    } else {
                        stack.push("0");
                    }
                }
                continue;
            }
            if (currentSymvol.equals("<")) {
                firstOp = stack.pop();
                if (idTable.get(currentFunc).containsKey(firstOp)) {
                    firstOp = idTable.get(currentFunc).get(firstOp).getValue();
                }
                secondOp = stack.pop();
                if (idTable.get(currentFunc).containsKey(secondOp)) {
                    secondOp = idTable.get(currentFunc).get(secondOp).getValue();
                }
                try {
                    if (Double.parseDouble(secondOp) < Double.parseDouble(firstOp)) {
                        stack.push("1");
                    } else {
                        stack.push("0");
                    }
                }
                catch (NumberFormatException e) {
                    if (secondOp.compareTo(firstOp) < 0) {
                        stack.push("1");
                    } else {
                        stack.push("0");
                    }
                }
                continue;
            }
            if (currentSymvol.equals("<=")) {
                firstOp = stack.pop();
                if (idTable.get(currentFunc).containsKey(firstOp)) {
                    firstOp = idTable.get(currentFunc).get(firstOp).getValue();
                }
                secondOp = stack.pop();
                if (idTable.get(currentFunc).containsKey(secondOp)) {
                    secondOp = idTable.get(currentFunc).get(secondOp).getValue();
                }
                try {
                    if (Double.parseDouble(secondOp) <= Double.parseDouble(firstOp)) {
                        stack.push("1");
                    } else {
                        stack.push("0");
                    }
                }
                catch (NumberFormatException e) {
                    if (secondOp.compareTo(firstOp) <= 0) {
                        stack.push("1");
                    } else {
                        stack.push("0");
                    }
                }
                continue;
            }
            if (currentSymvol.equals(">=")) {
                firstOp = stack.pop();
                if (idTable.get(currentFunc).containsKey(firstOp)) {
                    firstOp = idTable.get(currentFunc).get(firstOp).getValue();
                }
                secondOp = stack.pop();
                if (idTable.get(currentFunc).containsKey(secondOp)) {
                    secondOp = idTable.get(currentFunc).get(secondOp).getValue();
                }
                try {
                    if (Double.parseDouble(secondOp) >= Double.parseDouble(firstOp)) {
                        stack.push("1");
                    } else {
                        stack.push("0");
                    }
                }
                catch (NumberFormatException e) {
                    if (secondOp.compareTo(firstOp) >= 0) {
                        stack.push("1");
                    } else {
                        stack.push("0");
                    }
                }
                continue;
            }
            if (currentSymvol.equals("||")) {
                firstOp = stack.pop();
                if (idTable.get(currentFunc).containsKey(firstOp)) {
                    firstOp = idTable.get(currentFunc).get(firstOp).getValue();
                }
                secondOp = stack.pop();
                if (idTable.get(currentFunc).containsKey(secondOp)) {
                    secondOp = idTable.get(currentFunc).get(secondOp).getValue();
                }
                if(firstOp.equals("0") && secondOp.equals("0")) {
                    stack.push("0");
                } else {
                    stack.push("1");
                }
                continue;
            }
            if (currentSymvol.equals("&&")) {
                firstOp = stack.pop();
                if (idTable.get(currentFunc).containsKey(firstOp)) {
                    firstOp = idTable.get(currentFunc).get(firstOp).getValue();
                }
                secondOp = stack.pop();
                if (idTable.get(currentFunc).containsKey(secondOp)) {
                    secondOp = idTable.get(currentFunc).get(secondOp).getValue();
                }
                if(firstOp.equals("0") || secondOp.equals("0")) {
                    stack.push("0");
                } else {
                    stack.push("1");
                }
                continue;
            }
            if (currentSymvol.equals("jmp")) {
                firstOp = stack.pop();
                ind = Integer.parseInt(firstOp);
                ind--;
                continue;
            }
            if (currentSymvol.equals("jz")) {
                firstOp = stack.pop();
                secondOp = stack.pop();
                if (secondOp.equals("0")) {
                    ind = Integer.parseInt(firstOp);
                    ind--;
                    continue;
                }
                continue;
            }
            if (functionTable.containsKey(currentSymvol)) {
                for (int i = functionTable.get(currentSymvol).size() - 1; i >= 0; i--) {
                    firstOp = stack.pop();
                    firstOp = idTable.get(currentFunc).get(firstOp).getValue();
                    idTable.get(currentSymvol).get(functionTable.get(currentSymvol).get(i).getId()).setValue(firstOp);
                }
                execute(currentSymvol);
            }
            else {
                stack.push(currentSymvol);
            }
        }
        returnValue = stack.pop();
        if (!functionTypes.get(currentFunc).equals("void") && idTable.get(currentFunc).containsKey(returnValue)) {
            returnValue = idTable.get(currentFunc).get(returnValue).getValue();
        }
        stack.push(returnValue);
    }
}
