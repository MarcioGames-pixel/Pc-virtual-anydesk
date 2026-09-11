import java.io.File;
import java.io.PrintWriter;
import java.util.Locale;

import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Data;
import ghidra.program.model.listing.DataIterator;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionIterator;
import ghidra.program.model.listing.Listing;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceIterator;
import ghidra.program.model.symbol.Symbol;
import ghidra.program.model.symbol.SymbolIterator;
import ghidra.program.model.symbol.SymbolTable;

public class TomsMessengerAnalysis extends GhidraScript {

    private PrintWriter symbolsReport;
    private PrintWriter stringsReport;
    private PrintWriter functionsReport;
    private PrintWriter referencesReport;

    private static final String[] TERMS = {
        "addon",
        "add-on",
        "add_on",
        "addoncategory",
        "addOnCategory",
        "storeinventory",
        "StoreInventory",
        "inventory",
        "wardrobe",
        "background",
        "backgrounds",
        "gina",
        "angela",
        "tom",
        "ben",
        "pierre",
        "ginger",
        "documents",
        "category",
        "categories",
        "purchase",
        "purchased",
        "unlock",
        "unlocked",
        "plist",
        "store",
        "shop"
    };

    @Override
    public void run() throws Exception {

        File reportDirectory =
            new File(System.getProperty("user.dir"), "reports");

        if (!reportDirectory.exists()) {
            reportDirectory.mkdirs();
        }

        symbolsReport =
            new PrintWriter(
                new File(
                    reportDirectory,
                    "ghidra-interesting-symbols.txt"
                )
            );

        stringsReport =
            new PrintWriter(
                new File(
                    reportDirectory,
                    "ghidra-interesting-strings.txt"
                )
            );

        functionsReport =
            new PrintWriter(
                new File(
                    reportDirectory,
                    "ghidra-interesting-functions.txt"
                )
            );

        referencesReport =
            new PrintWriter(
                new File(
                    reportDirectory,
                    "ghidra-string-references.txt"
                )
            );

        println("====================================");
        println("Tom's Messenger 1.1");
        println("Ghidra analysis");
        println("====================================");
        println();

        println("Program: " + currentProgram.getName());
        println(
            "Executable path: "
            + currentProgram.getExecutablePath()
        );

        println();

        analyzeStrings();
        analyzeSymbols();
        analyzeFunctions();
        analyzeReferences();

        symbolsReport.close();
        stringsReport.close();
        functionsReport.close();
        referencesReport.close();

        println();
        println("Analysis completed successfully.");
    }

    private boolean interesting(String value) {

        if (value == null) {
            return false;
        }

        String lower =
            value.toLowerCase(Locale.ROOT);

        for (String term : TERMS) {

            if (
                lower.contains(
                    term.toLowerCase(Locale.ROOT)
                )
            ) {
                return true;
            }
        }

        return false;
    }

    private void analyzeStrings() {

        Listing listing =
            currentProgram.getListing();

        DataIterator dataIterator =
            listing.getDefinedData(true);

        while (dataIterator.hasNext()) {

            Data data =
                dataIterator.next();

            Object value =
                data.getValue();

            if (value == null) {
                continue;
            }

            String text =
                value.toString();

            if (!interesting(text)) {
                continue;
            }

            String line =
                data.getAddress()
                + " | "
                + data.getDataType()
                + " | "
                + text;

            println("[STRING] " + line);

            stringsReport.println(line);
        }

        stringsReport.flush();
    }

    private void analyzeSymbols() {

        SymbolTable table =
            currentProgram.getSymbolTable();

        SymbolIterator iterator =
            table.getAllSymbols(true);

        while (iterator.hasNext()) {

            Symbol symbol =
                iterator.next();

            String name =
                symbol.getName();

            if (!interesting(name)) {
                continue;
            }

            String line =
                symbol.getAddress()
                + " | "
                + symbol.getSymbolType()
                + " | "
                + name;

            println("[SYMBOL] " + line);

            symbolsReport.println(line);
        }

        symbolsReport.flush();
    }

    private void analyzeFunctions() {

        FunctionIterator iterator =
            currentProgram
                .getFunctionManager()
                .getFunctions(true);

        while (iterator.hasNext()) {

            Function function =
                iterator.next();

            String name =
                function.getName();

            if (!interesting(name)) {
                continue;
            }

            String line =
                function.getEntryPoint()
                + " | "
                + name;

            println("[FUNCTION] " + line);

            functionsReport.println(line);
        }

        functionsReport.flush();
    }

    private void analyzeReferences() {

        Listing listing =
            currentProgram.getListing();

        DataIterator iterator =
            listing.getDefinedData(true);

        while (iterator.hasNext()) {

            Data data =
                iterator.next();

            Object value =
                data.getValue();

            if (value == null) {
                continue;
            }

            String text =
                value.toString();

            if (!interesting(text)) {
                continue;
            }

            ReferenceIterator references =
                currentProgram
                    .getReferenceManager()
                    .getReferencesTo(
                        data.getAddress()
                    );

            while (references.hasNext()) {

                Reference reference =
                    references.next();

                String line =
                    "String: "
                    + text
                    + " | From: "
                    + reference.getFromAddress();

                println(
                    "[REFERENCE] "
                    + line
                );

                referencesReport.println(line);
            }
        }

        referencesReport.flush();
    }
}
