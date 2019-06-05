package model;

public interface Edit {

    int getFromVersion();

    int getStartPosition();

    void shiftStartPosition(int count);

    Operation getOperation();

    int getSymbolsCount();

    String getSymbols();

}
