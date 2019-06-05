package model;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class HashMapStorage implements Storage {

    private static AtomicLong lastDocumentId = new AtomicLong(0);

    private Map<Long, PlainTextDocument> documents = new ConcurrentHashMap<>();

    public PlainTextDocument createDocument(String name) {
        PlainTextDocument document = new PlainTextDocument(lastDocumentId.incrementAndGet(), name);
        documents.put(document.getId(), document);
        return document;
    }

    public PlainTextDocument getDocument(long id) {
        return documents.get(id);
    }

    public PlainTextDocument removeDocument(long id) {
        return documents.remove(id);
    }

}