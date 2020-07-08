import interpreter.*;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class CourseWork {

    public static void main(String[] args) {
        try {
            String text = "";
            String filename;
            Scanner in = new Scanner(System.in);
            System.out.println("Enter file name: ");
            filename = in.next();

            ArrayList<String> list = (ArrayList<String>) Files.readAllLines(Paths.get(filename));
            for (String s : list) {
                text += s + '\n';
            }
            Interpreter interp = new Interpreter(new SyntaxAnalyzer(new LexicalAnalyzer(text)));
            interp.start();
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
//        catch (Exception e) {
//            System.out.println("Runtime error.");
//        }
    }
}
