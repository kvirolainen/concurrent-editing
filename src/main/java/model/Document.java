package model;

import java.util.List;

public interface Document {

    long getId();

    String getName();

    int getVersion();

    String getText();

    List<Change> getChanges(int fromVersion);

}
