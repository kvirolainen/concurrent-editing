package model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class EditImpl implements Edit {

    @Min(0)
    private int fromVersion;

    @Min(0)
    private int startPosition;

    @NotNull
    private Operation operation;

    @Min(0)
    private int symbolsCount;

    @NotNull
    private String symbols;

    private EditImpl() {
    }

    private EditImpl(int fromVersion, int startPosition, Operation operation, int symbolsCount, String symbols) {
        this.fromVersion = fromVersion;
        this.startPosition = startPosition;
        this.operation = operation;
        this.symbolsCount = symbolsCount;
        this.symbols = symbols;
    }

    public static EditImpl constructInsert(int fromVersion, int startPosition, String symbols) {
        return new EditImpl(fromVersion, startPosition, Operation.INSERT, symbols.length(), symbols);
    }

    public static EditImpl constructDelete(int fromVersion, int startPosition, int symbolsCount) {
        return new EditImpl(fromVersion, startPosition, Operation.DELETE, symbolsCount, "");
    }

    public int getFromVersion() {
        return fromVersion;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public void shiftStartPosition(int count) {
        startPosition += count;
    }

    public Operation getOperation() {
        return operation;
    }

    public int getSymbolsCount() {
        return symbolsCount;
    }

    public String getSymbols() {
        return symbols;
    }

}