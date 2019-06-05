package model;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class DocumentChangesTest {

    @Test
    public void testAddChange1() {
        PlainTextDocument document = new PlainTextDocument(0, "Test Document");
        document.addChange(new ChangeImpl(0, Operation.INSERT, "Hello, world!", 0));
        assertEquals("Hello, world!", document.getText());
    }

    @Test
    public void testAddChange2() {
        PlainTextDocument document = new PlainTextDocument(0, "Test Document");
        document.addChange(new ChangeImpl(0, Operation.INSERT, "Hello, world!", 0));
        document.addChange(new ChangeImpl("Hello".length(), Operation.DELETE, ", ", 0));
        assertEquals("Helloworld!", document.getText());
    }

    @Test
    public void testAddChangesMany1() {
        PlainTextDocument document = new PlainTextDocument(0, "Test Document");
        document.addChangesMany(Arrays.asList(
                new ChangeImpl(0, Operation.INSERT, "Hello, world!", 0),
                new ChangeImpl("Hello".length(), Operation.DELETE, ", ", 0)
        ));
        assertEquals("Helloworld!", document.getText());
    }

    @Test
    public void testAddChangesMany2() {
        PlainTextDocument document = new PlainTextDocument(0, "Test Document");
        document.addChangesMany(Arrays.asList(
                new ChangeImpl(0, Operation.INSERT, "Hello, world!", 0),
                new ChangeImpl("Hello, ".length(), Operation.DELETE, "world", 0),
                new ChangeImpl("Hello, ".length(), Operation.INSERT, "universe", 0)
        ));
        assertEquals("Hello, universe!", document.getText());
    }

    @Test
    public void testGetLength1() {
        PlainTextDocument document = new PlainTextDocument(0, "Test Document");
        document.addChangesMany(Arrays.asList(
                new ChangeImpl(0, Operation.INSERT, "Hello, world!", 0),
                new ChangeImpl("Hello, ".length(), Operation.DELETE, "world", 0),
                new ChangeImpl("Hello, ".length(), Operation.INSERT, "universe", 0)
        ));
        assertEquals(0, document.getLength(0));
        assertEquals(13, document.getLength(1));
        assertEquals(8, document.getLength(2));
        assertEquals(16, document.getLength(3));
    }

}