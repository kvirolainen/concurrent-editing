package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PlainTextDocument implements Document {

    private final long id;
    private String name;

    private int version = 0;
    private String text = "";
    private final List<Change> changeLog = new ArrayList<>();

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    PlainTextDocument(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getVersion() {
        readWriteLock.readLock().lock();
        try {
            return version;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public String getText() {
        readWriteLock.readLock().lock();
        try {
            return text;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public List<Change> getChanges(int fromVersion) {
        readWriteLock.readLock().lock();
        try {
            if (fromVersion == version) {
                return Collections.emptyList();
            }
            return changeLog.subList(fromVersion, changeLog.size());
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLength(int version) {
        if (version == 0) {
            return 0;
        }
        readWriteLock.readLock().lock();
        try {
            return text.length() - changeLog.stream().skip(version).mapToInt(
                    change -> change.getText().length() * (change.getOperation() == Operation.INSERT ? 1 : -1)
            ).sum();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    void addChangesMany(List<ChangeImpl> changes) {
        readWriteLock.writeLock().lock();
        try {
            changes.forEach(this::processChange);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    void addChange(ChangeImpl change) {
        readWriteLock.writeLock().lock();
        try {
            processChange(change);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    private void processChange(ChangeImpl change) {
        if (change.getOperation() == Operation.INSERT) {
            text = new StringBuilder(text).insert(change.getStartPosition(), change.getText()).toString();
        } else {
            text = new StringBuilder(text).delete(
                    change.getStartPosition(), change.getStartPosition() + change.getText().length()
            ).toString();
        }
        version++;
        change.setId(version);
        changeLog.add(change);
    }

}