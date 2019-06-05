package model;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class Processor {

    /*
     * Edit is a user action from a user local version of document that is not processed yet.
     * List of new changes is a diff between user local version and server version of document.
     * List of new changes is changes, that were processed by the server, but not seen by editing user.
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public List<Change> processEdit(PlainTextDocument document, Edit edit, long userId) throws EditValidationException {
        validateEdit(document, edit);
        synchronized (document) {
            if (edit.getOperation() == Operation.INSERT) {
                processInsert(document, edit, userId);
            } else {
                processDelete(document, edit, userId);
            }
            return document.getChanges(edit.getFromVersion());
        }
    }

    /*
     * Validation of the `edit` from the user local version of the document.
     * If the validation is passed, this `edit` will always be correct for this version.
     * All next changes increment the document version. Changing version history is not possible.
     */
    private void validateEdit(PlainTextDocument document, Edit edit) throws EditValidationException {
        int documentLength = document.getLength(edit.getFromVersion());
        if (edit.getStartPosition() < 0 || edit.getStartPosition() > documentLength) {
            throw new EditValidationException("Start position is incorrect");
        }
        if (edit.getFromVersion() < 0 || edit.getFromVersion() > document.getVersion()) {
            throw new EditValidationException("Document version is unknown");
        }
        if (edit.getOperation() == Operation.DELETE &&
                edit.getStartPosition() + edit.getSymbolsCount() > documentLength) {
            throw new EditValidationException("Delete interval is out of bounds");
        }
        if (edit.getSymbolsCount() < 0 || edit.getSymbolsCount() == 0 && edit.getOperation() == Operation.DELETE) {
            throw new EditValidationException("No symbols to delete");
        }
        if (edit.getOperation() == Operation.INSERT && edit.getSymbols().isEmpty()) {
            throw new EditValidationException("No symbols to insert");
        }
    }

    /*
     * This method processes user insertion.
     *
     * The basic idea is to move the cursor position according to the new changes.
     *
     * If characters are deleted from the left of the cursor, the cursor shifts to the left.
     * If characters are inserted from the left of the cursor, the cursor shifts to the right.
     * If characters are added from the same position, the cursor also shifts to the right.
     *
     * Example
     * Initial text: Hello, universe!
     * Concurrent change (already processed by the server, but not seen by the user): Hello, universe! -> Hello, universe!!!
     * User change: Hello, universe! -> Hello, universe!?
     * Merge result: Hello, universe!!!?
     */
    private void processInsert(PlainTextDocument document, Edit edit, long userId) {
        for (Change change : document.getChanges(edit.getFromVersion())) {
            if (change.getOperation() == Operation.INSERT && change.getStartPosition() <= edit.getStartPosition()) {
                edit.shiftStartPosition(change.getText().length());
            } else if (change.getOperation() == Operation.DELETE && change.getStartPosition() < edit.getStartPosition()) {
                int shift = Math.min(edit.getStartPosition() - change.getStartPosition(), change.getText().length());
                edit.shiftStartPosition(-shift);
            }
        }
        ChangeImpl addedChange = new ChangeImpl(edit.getStartPosition(), Operation.INSERT, edit.getSymbols(), userId);
        document.addChange(addedChange);
    }

    /*
     * This method processes user deletion.
     *
     * The logic is the same as for insertion, except the situation when user deletion conflicts with the list of new changes.
     *
     * If new changes contain insertion to the same interval, `edit` divides in two.
     * If new changes contain deletion from the same interval, this deletion removes collision part from the `edit`
     * in order not to delete the same characters twice. As a result `edit` can be splitted, decreased or disappeared.
     */
    private void processDelete(PlainTextDocument document, Edit userEdit, long userId) {
        List<Edit> edits = new ArrayList<>();
        edits.add(userEdit);

        for (Change change : document.getChanges(userEdit.getFromVersion())) {
            ListIterator<Edit> it = edits.listIterator();
            while (it.hasNext()) {
                Edit edit = it.next();
                if (change.getStartPosition() >= edit.getStartPosition() + edit.getSymbolsCount()) {
                    continue;
                }
                if (change.getOperation() == Operation.INSERT &&
                        change.getStartPosition() <= edit.getStartPosition()) {
                    edit.shiftStartPosition(change.getText().length());
                } else if (change.getOperation() == Operation.DELETE &&
                        change.getStartPosition() + change.getSymbolsCount() <= edit.getStartPosition()) {
                    edit.shiftStartPosition(change.getSymbolsCount());
                } else {
                    it.remove();
                    transformDeleteOnConflict(edit, change).forEach(it::add);
                }
            }
        }

        final String documentText = document.getText();
        List<ChangeImpl> changes = edits.stream().map(currentEdit -> new ChangeImpl(
                currentEdit.getStartPosition(),
                Operation.DELETE,
                documentText.substring(
                        currentEdit.getStartPosition(),
                        currentEdit.getStartPosition() + currentEdit.getSymbolsCount()),
                userId
        )).sorted((e1, e2) -> e2.getStartPosition() - e1.getStartPosition()).collect(Collectors.toList());
        document.addChangesMany(changes);
    }

    private List<Edit> transformDeleteOnConflict(Edit edit, Change change) {
        int leftSymbolsCount = change.getStartPosition() - edit.getStartPosition();
        Edit left = EditImpl.constructDelete(
                edit.getFromVersion(),
                edit.getStartPosition(),
                leftSymbolsCount
        );
        if (change.getOperation() == Operation.INSERT) {
            Edit right = EditImpl.constructDelete(
                    edit.getFromVersion(),
                    change.getStartPosition() + change.getSymbolsCount(),
                    edit.getSymbolsCount() - leftSymbolsCount
            );
            return Arrays.asList(right, left);
        } else {
            Edit right = EditImpl.constructDelete(
                    edit.getFromVersion(),
                    change.getStartPosition(),
                    edit.getSymbolsCount() - leftSymbolsCount - change.getSymbolsCount()
            );
            List<Edit> transformedEdits = new ArrayList<>();
            if (right.getSymbolsCount() > 0) {
                transformedEdits.add(right);
            }
            if (left.getSymbolsCount() > 0) {
                transformedEdits.add(left);
            }
            return transformedEdits;
        }
    }

}