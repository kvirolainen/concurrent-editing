package model;

public interface Storage {

    Document createDocument(String name);

    Document getDocument(long id);

    Document removeDocument(long id);

}