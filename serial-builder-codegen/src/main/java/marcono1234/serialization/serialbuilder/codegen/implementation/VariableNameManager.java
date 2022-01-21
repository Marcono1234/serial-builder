package marcono1234.serialization.serialbuilder.codegen.implementation;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for generating non-conflicting variable names.
 */
public class VariableNameManager {
    private final Map<String, Integer> lastCountMap;

    public VariableNameManager() {
        this(new HashMap<>());
    }

    private VariableNameManager(Map<String, Integer> lastCountMap) {
        this.lastCountMap = lastCountMap;
    }

    public VariableNameManager copy() {
        return new VariableNameManager(new HashMap<>(lastCountMap));
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    public String getName(String variableName) {
        // Disallow trailing digit because it could clash with variable name with count suffix
        if (variableName.isEmpty() || isDigit(variableName.charAt(variableName.length() - 1))) {
            throw new IllegalArgumentException("Invalid variable name: " + variableName);
        }

        int count = lastCountMap.merge(variableName, 1, Integer::sum);
        if (count == 1) {
            return variableName;
        } else {
            return variableName + count;
        }
    }

    public void markNameUsed(String variableName) {
        if (variableName.isEmpty()) {
            throw new IllegalArgumentException("Empty variable name is not supported");
        }

        int nameEndIndex = variableName.length() - 1;
        while (nameEndIndex >= 0 && isDigit(variableName.charAt(nameEndIndex))) {
            nameEndIndex--;
        }

        if (nameEndIndex == 0 && isDigit(variableName.charAt(0))) {
            throw new IllegalArgumentException("Variable name only consists of digits: " + variableName);
        }

        String name = variableName.substring(0, nameEndIndex + 1);
        int count;
        if (nameEndIndex == variableName.length() - 1) {
            count = 1;
        } else {
            count = Integer.parseInt(variableName.substring(nameEndIndex + 1));
        }

        Integer oldValue = lastCountMap.put(name, count);
        if (oldValue != null && oldValue >= count) {
            throw new IllegalArgumentException("Variable name conflicts with used name: " + variableName);
        }
    }
}
