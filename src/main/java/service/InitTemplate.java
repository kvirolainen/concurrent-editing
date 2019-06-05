package service;

import model.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class InitTemplate implements InitializingBean {

    private final HashMapStorage storage;

    private final Processor processor;

    public InitTemplate(HashMapStorage storage, Processor processor) {
        this.storage = storage;
        this.processor = processor;
    }

    @Override
    public void afterPropertiesSet() throws EditValidationException {
        PlainTextDocument document = storage.createDocument("First Document");
        processor.processEdit(
                document,
                EditImpl.constructInsert(document.getVersion(), document.getText().length(), "Hello, "),
                0
        );
        processor.processEdit(
                document,
                EditImpl.constructInsert(document.getVersion(), document.getText().length(), "world!"),
                0
        );
    }

}