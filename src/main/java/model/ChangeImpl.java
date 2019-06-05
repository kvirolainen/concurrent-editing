package model;

public class ChangeImpl implements Change {

    private long id;
    private final int startPosition;
    private final Operation operation;
    private final String text;
    private final long userId;

    public ChangeImpl(int startPosition, Operation operation, String text, long userId) {
        this.startPosition = startPosition;
        this.operation = operation;
        this.text = text;
        this.userId = userId;
    }

    public long getId() {
        return id;
    }

    void setId(long id) {
        this.id = id;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public Operation getOperation() {
        return operation;
    }

    public String getText() {
        return text;
    }

    public long getUserId() {
        return userId;
    }

    public int getSymbolsCount() {
        return getText().length();
    }

}