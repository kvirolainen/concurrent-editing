package model;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class ProcessorTest {

    private Processor processor;
    private PlainTextDocument document;

    /*
     * version 1: Hello, world!
     * version 2: Hello, !
     * version 3: Hello, universe!
     */
    @Before
    public void setUp() {
        processor = new Processor();
        document = new HashMapStorage().createDocument("Test Document");
        document.addChangesMany(Arrays.asList(
                new ChangeImpl(0, Operation.INSERT, "Hello, world!", 0),
                new ChangeImpl("Hello, ".length(), Operation.DELETE, "world", 0),
                new ChangeImpl("Hello, ".length(), Operation.INSERT, "universe", 0)
        ));

    }

    @Test
    public void testInsert1() throws EditValidationException {
        processor.processEdit(document, EditImpl.constructInsert(1, 0, "He-"), 0);
        assertEquals("He-Hello, universe!", document.getText());
    }

    @Test
    public void testInsert2() throws EditValidationException {
        processor.processEdit(document, EditImpl.constructInsert(1, "Hello, world".length(), "!!"), 0);
        assertEquals("Hello, universe!!!", document.getText());
    }

    @Test
    public void testInsert3() throws EditValidationException {
        processor.processEdit(document, EditImpl.constructInsert(1, "Hello, wo".length(), "-o-o"), 0);
        assertEquals("Hello, universe-o-o!", document.getText());
    }

    @Test
    public void testInsert4() throws EditValidationException {
        document.addChange(new ChangeImpl("Hell".length(), Operation.DELETE, "o, universe!", 0));
        processor.processEdit(document, EditImpl.constructInsert(3, "Hello, universe!".length(), "!!!"), 0);
        assertEquals("Hell!!!", document.getText());
    }

    @Test
    public void testDelete1() throws EditValidationException {
        processor.processEdit(document, EditImpl.constructDelete(1, 0, "Hello, world".length()), 0);
        assertEquals("universe!", document.getText());
    }

    @Test
    public void testDelete2() throws EditValidationException {
        processor.processEdit(document, EditImpl.constructDelete(1, "Hello".length(), ", ".length()), 0);
        assertEquals("Hellouniverse!", document.getText());
    }

    @Test
    public void testDelete3() throws EditValidationException {
        processor.processEdit(document, EditImpl.constructDelete(1, "Hello".length(), ", world".length()), 0);
        assertEquals("Hellouniverse!", document.getText());
    }

    @Test
    public void testDelete4() throws EditValidationException {
        processor.processEdit(document, EditImpl.constructInsert(2, "Hello,".length(), " dear"), 0);
        assertEquals("Hello, dear universe!", document.getText());
    }

    @Test
    public void testDelete5() throws EditValidationException {
        processor.processEdit(document, EditImpl.constructDelete(1, 0, "Hello, world!".length()), 0);
        assertEquals("universe", document.getText());
    }

    @Test
    public void testDelete6() throws EditValidationException {
        processor.processEdit(document, EditImpl.constructDelete(2, "Hello".length(), ", !".length()), 0);
        assertEquals("Hellouniverse", document.getText());
    }

    @Test
    public void testSequence1() throws EditValidationException {
        processor.processEdit(document, EditImpl.constructDelete(1, 0, "Hello".length()), 0);
        processor.processEdit(document, EditImpl.constructInsert(1, 0, "Hi"), 0);
        assertEquals("Hi, universe!", document.getText());
    }

    @Test
    public void testSequence2() throws EditValidationException {
        processor.processEdit(document, EditImpl.constructDelete(1, "H".length(), "ello".length()), 0);
        processor.processEdit(document, EditImpl.constructInsert(1, "H".length(), "i"), 0);
        assertEquals("Hi, universe!", document.getText());
    }

    @Test
    public void testSequence3() throws EditValidationException {
        processor.processEdit(document, EditImpl.constructDelete(1, "Hello, ".length(), "world".length()), 0);
        processor.processEdit(document, EditImpl.constructInsert(1, "Hello, ".length(), "planet"), 0);
        assertEquals("Hello, universeplanet!", document.getText());
    }

    @Test
    public void testSequence4() throws EditValidationException {
        PlainTextDocument document = new HashMapStorage().createDocument("Test Document");
        processor.processEdit(document, EditImpl.constructInsert(0, 0, "Hello"), 0);
        processor.processEdit(document, EditImpl.constructInsert(1, 1, "w"), 0);
        processor.processEdit(document, EditImpl.constructInsert(1, 2, "o"), 0);
        processor.processEdit(document, EditImpl.constructInsert(1, 3, "r"), 0);
        processor.processEdit(document, EditImpl.constructInsert(1, 4, "l"), 0);
        processor.processEdit(document, EditImpl.constructInsert(1, 5, "d"), 0);
        assertEquals("Hweolrllod", document.getText());

        processor.processEdit(document, EditImpl.constructDelete(1, 0, "Hello".length()), 0);
        assertEquals("world", document.getText());
    }

    @Test
    public void testSequence5() throws EditValidationException {
        processor.processEdit(document, EditImpl.constructInsert(3, "Hello, universe!".length(), "!!"), 0);
        processor.processEdit(document, EditImpl.constructInsert(3, "Hello, universe!".length(), "?"), 0);
        assertEquals("Hello, universe!!!?", document.getText());
    }

    @Test
    public void testSequence6() throws EditValidationException {
        processor.processEdit(document, EditImpl.constructDelete(3, 0, "Hello, universe".length()), 0);
        processor.processEdit(document, EditImpl.constructDelete(3, "Hello, ".length(), "uni".length()), 0);
        assertEquals("!", document.getText());
    }

}