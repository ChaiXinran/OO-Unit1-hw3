import java.util.Scanner;

import ast.Expr;
import ast.func.FuncRegistry;
import output.OutputEntry;
import parser.Lexer;
import parser.Parser;
import parser.Preprocessor;
import polynomial.Poly;

public class MainClass {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        FuncRegistry registry = FuncRegistry.getInstance();

        int n = Integer.parseInt(scanner.nextLine().trim());
        for (int i = 0; i < n; i++) {
            String line = scanner.nextLine().trim();
            line = Preprocessor.process(line);
            Parser.parseAndRegisterFuncDef(line, registry);
        }

        int m = Integer.parseInt(scanner.nextLine().trim());
        for (int i = 0; i < m; i++) {
            String f0Line = Preprocessor.process(scanner.nextLine().trim());
            String f1Line = Preprocessor.process(scanner.nextLine().trim());
            String recLine = Preprocessor.process(scanner.nextLine().trim());
            Parser.parseAndRegisterRecDef(f0Line, registry);
            Parser.parseAndRegisterRecDef(f1Line, registry);
            Parser.parseAndRegisterRecDef(recLine, registry);
        }

        String input = scanner.nextLine();
        scanner.close();

        // Stage 1: parse only
        String preprocessed = Preprocessor.process(input);
        Lexer lexer = new Lexer(preprocessed);
        Parser parser = new Parser(lexer);
        parser.setFuncRegistry(registry);
        Expr expr = parser.parseExpr();

        // Stage 2: reduce into canonical AST, then convert to polynomial
        Poly poly = expr.reduce().toPolynomial();

        // Stage 3: format only
        String output = OutputEntry.format(poly);
        System.out.println(output);
    }
}
