package macroprocessor;

import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gerencia a pilha de argumentos para suportar macros aninhadas
 * e realiza a substituição posicional (?1, ?2...).
 */
public class ArgTab {

    // Pilha de contextos: cada array guarda os argumentos reais da macro em expansão
    private final Stack<String[]> contextStack;

    // Padrão Regex para identificar parâmetros posicionais (ex: ?1, ?10)
    private static final Pattern POSITIONAL_PATTERN = Pattern.compile("\\?(\\d+)");

    public ArgTab() {
        this.contextStack = new Stack<>();
    }

    // Empilha os argumentos recebidos de callLine.getOperands()
    public void pushContext(String[] arguments) {
        if (arguments == null) {
            contextStack.push(new String[0]);
            return;
        }
        String[] trimmed = new String[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            trimmed[i] = arguments[i] != null ? arguments[i].trim() : "";
        }
        contextStack.push(trimmed);
    }

    // Sobrecarga caso os argumentos sejam passados como List
    public void pushContext(List<String> arguments) {
        pushContext(arguments != null ? arguments.toArray(String[]::new) : null);
    }

    // Remove o contexto atual quando a expansão da macro termina (MEND)
    public void popContext() {
        if (!contextStack.isEmpty()) {
            contextStack.pop();
        }
    }

    public boolean isExpanding() {
        return !contextStack.isEmpty();
    }

    // Varre a linha e substitui os padrões ?n pelos valores reais da macro atual
    public String substitute(String line) {
        if (line == null || line.isEmpty() || contextStack.isEmpty()) {
            return line;
        }

        String[] activeArgs = contextStack.peek();
        Matcher matcher = POSITIONAL_PATTERN.matcher(line);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            int argIndex = Integer.parseInt(matcher.group(1));

            if (argIndex < 1 || argIndex > activeArgs.length) {
                throw new IllegalArgumentException(
                    "Erro de expansão: parâmetro posicional ?" + argIndex + " não fornecido."
                );
            }

            String replacement = activeArgs[argIndex - 1];
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    public void clear() {
        contextStack.clear();
    }
}