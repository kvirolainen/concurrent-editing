package model;

public interface Change {

    long getId();

    int getStartPosition();

    Operation getOperation();

    String getText();

    long getUserId();

    default int getSymbolsCount() {
        return getText().length();
    }

}
