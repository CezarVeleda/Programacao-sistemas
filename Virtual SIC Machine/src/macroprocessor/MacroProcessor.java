package macroprocessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class MacroProcessor {

    private boolean expanding;
    private NamTab namTab;
    private DefTab defTab;
    private ArgTab argTab;
    private Stack<Integer> defTabPointers;

    public MacroProcessor() {
        this.expanding = false;
        this.namTab = new NamTab();
        this.defTab = new DefTab();
        this.argTab = new ArgTab();
        this.defTabPointers = new Stack<>();
    }

    public void define(BufferedReader input, ParsedLine initialLine) throws IOException {
        String macroName = initialLine.getLabel();
        if (macroName == null || macroName.isBlank()) {
            throw new IllegalArgumentException("Erro: Diretiva MACRO sem nome (label) definido.");
        }

        int startIndex = defTab.getCurrentSize();
        namTab.addMacro(macroName, startIndex);

        Map<String, String> formalParamsMap = buildFormalParamsMap(initialLine.getOperands());
        int level = 1;

        while (level > 0) {
            String rawLine = input.readLine();
            if (rawLine == null) {
                throw new IllegalStateException("Erro: Fim de arquivo inesperado antes do MEND da macro " + macroName);
            }

            ParsedLine currentLine = new ParsedLine(rawLine);
            String opcode = currentLine.getOpcode();

            if ("MACRO".equalsIgnoreCase(opcode)) {
                level++;
            } else if ("MEND".equalsIgnoreCase(opcode)) {
                level--;
            }

            String parameterizedLine = replaceFormalParameters(rawLine, formalParamsMap);
            defTab.addLine(parameterizedLine);
        }

        int endIndex = defTab.getCurrentSize() - 1;
        namTab.setEndIndex(macroName, endIndex);
    }

    public void expand(ParsedLine callLine) {
        this.expanding = true;
        argTab.pushContext(callLine.getOperands());

        String macroName = callLine.getOpcode();
        NamTab.MacroInfo info = namTab.getMacro(macroName);
        if (info == null) {
            throw new IllegalArgumentException("Erro: Macro não encontrada na NamTab: " + macroName);
        }

        defTabPointers.push(info.startIndex);
    }

    private Map<String, String> buildFormalParamsMap(String[] operands) {
        Map<String, String> paramMap = new HashMap<>();
        if (operands == null) {
            return paramMap;
        }

        for (int i = 0; i < operands.length; i++) {
            String param = operands[i].trim();
            if (!param.isEmpty()) {
                String key = param.startsWith("&") ? param : "&" + param;
                paramMap.put(key, "?" + (i + 1));
            }
        }
        return paramMap;
    }

    private String replaceFormalParameters(String line, Map<String, String> paramMap) {
        if (paramMap.isEmpty()) {
            return line;
        }

        List<String> keys = new ArrayList<>(paramMap.keySet());
        keys.sort(Comparator.comparingInt(String::length).reversed());

        String result = line;
        for (String param : keys) {
            result = result.replace(param, paramMap.get(param));
        }
        return result;
    }
}