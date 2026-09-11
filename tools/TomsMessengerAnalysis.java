import java.io.*;
import java.util.*;

import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.*;
import ghidra.program.model.symbol.*;
import ghidra.program.model.address.*;
import ghidra.program.model.mem.*;

public class TomsMessengerAnalysis extends GhidraScript {

    private PrintWriter report;
    private PrintWriter stringsReport;
    private PrintWriter functionsReport;

    private final String[] TERMS = {
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

        File programFile =
            new File(currentProgram.getExecutablePath());

        File outputDir =
            new File(System.getProperty("user.dir"), "reports");

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        report = new PrintWriter(
            new File(outputDir, "ghidra-interesting-symbols.txt")
        );

        stringsReport = new PrintWriter(
            new File(outputDir, "ghidra-interesting-strings.txt")
        );

        functionsReport = new PrintWriter(
            new File(outputDir, "ghidra-interesting-functions.txt")
        );

        println("==============================================");
        println("Tom's Messenger 1.1 Ghidra Analysis");
        println("==============================================");
        println("Program: " + currentProgram.getName());
        println("Executable: " + programFile);
        println();

        report.println("Tom's Messenger 1.1");
        report.println("Program: " + currentProgram.getName());
        report.println();

        analyzeMemoryBlocks();
        analyzeDefinedStrings();
        analyzeSymbols();
        analyzeFunctions();
        analyzeReferences();

        report.close();
        stringsReport.close();
        functionsReport.close();

        println();
        println("Analysis complete.");
    }

    private boolean containsInteresting(String text) {

        if (text == null) {
            return false;
        }

        String lower = text.toLowerCase(Locale.ROOT);

        for (String term : TERMS) {
            if (lower.contains(term.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }

        return false;
    }

    private void analyzeMemoryBlocks() {

        report.println("===== MEMORY BLOCKS =====");

        Memory memory = currentProgram.getMemory();

        for (MemoryBlock block : memory.getBlocks()) {

            String name = block.getName();

            if (containsInteresting(name)) {

                String line =
                    block.getName()
                    + " "
                    + block.getStart()
                    + "-"
                    + block.getEnd();

                println("[MEMORY] " + line);
                report.println(line);
            }
        }

        report.println();
    }

    private void analyzeDefinedStrings() {

        report.println("===== DEFINED STRINGS =====");
        stringsReport.println("===== INTERESTING STRINGS =====");

        Listing listing = currentProgram.getListing();

        DataIterator iterator =
            listing.getDefinedData(true);

        while (iterator.hasNext()) {

            Data data = iterator.next();

            Object value = data.getValue();

            if (value == null) {
                continue;
            }

            String text = value.toString();

            if (!containsInteresting(text)) {
                continue;
            }

            String line =
                data.getAddress()
                + " | "
                + data.getDataType()
                + " | "
                + text;

            println("[STRING] " + line);

            report.println(line);
            stringsReport.println(line);
        }

        report.println();
        stringsReport.println();
    }

    private void analyzeSymbols() {

        report.println("===== SYMBOLS =====");

        SymbolTable table =
            currentProgram.getSymbolTable();

        SymbolIterator symbols =
            table.getAllSymbols(true);

        while (symbols.hasNext()) {

            Symbol symbol = symbols.next();

            String name = symbol.getName();

            if (!containsInteresting(name)) {
                continue;
            }

            String line =
                symbol.getAddress()
                + " | "
                + symbol.getSymbolType()
                + " | "
                + name;

            println("[SYMBOL] " + line);
            report.println(line);
        }

        report.println();
    }

    private void analyzeFunctions() {

        functionsReport.println(
            "===== INTERESTING FUNCTIONS ====="
        );

        FunctionIterator functions =
            currentProgram.getFunctionManager()
                         .getFunctions(true);

        while (functions.hasNext()) {

            Function function =
                functions.next();

            String name =
                function.getName();

            if (!containsInteresting(name)) {
                continue;
            }

            String line =
                function.getEntryPoint()
                + " | "
                + name;

            println("[FUNCTION] " + line);
            functionsReport.println(line);
        }

        functionsReport.println();
    }

    private void analyzeReferences() {

        report.println("===== REFERENCES TO INTERESTING STRINGS =====");

        Listing listing = currentProgram.getListing();

        DataIterator iterator =
            listing.getDefinedData(true);

        while (iterator.hasNext()) {

            Data data = iterator.next();

            Object value = data.getValue();

            if (value == null) {
                continue;
            }

            String text = value.toString();

            if (!containsInteresting(text)) {
                continue;
            }

            ReferenceIterator refs =
                currentProgram.getReferenceManager()
                    .getReferencesTo(data.getAddress());

            while (refs.hasNext()) {

                Reference ref = refs.next();

                String line =
                    "STRING: "
                    + text
                    + " | FROM: "
                    + ref.getFromAddress();

                println("[REFERENCE] " + line);
                report.println(line);
            }
        }

        report.println();
    }
          }
