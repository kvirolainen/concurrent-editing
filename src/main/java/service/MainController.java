package service;

import model.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
public class MainController {

    private final HashMapStorage storage;
    private final Processor processor;

    public MainController(HashMapStorage storage, Processor processor) {
        this.storage = storage;
        this.processor = processor;
    }

    @RequestMapping(value = "/documents/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity getDocument(
            @PathVariable("id") long id,
            HttpServletRequest request
    ) {
        Document document = storage.getDocument(id);
        if (document == null) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            return new ResponseEntity<>(new ApiError(status, "Document not found", request), status);
        }
        return new ResponseEntity<>(document, HttpStatus.OK);
    }

    @RequestMapping(value = "/documents/{id}/changes", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity getDocumentChanges(
            @PathVariable("id") long id,
            @RequestParam int fromVersion,
            HttpServletRequest request
    ) {
        Document document = storage.getDocument(id);
        if (document == null) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            return new ResponseEntity<>(new ApiError(status, "Document not found", request), status);
        }

        if (fromVersion < 0 || fromVersion > document.getVersion()) {
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return new ResponseEntity<>(new ApiError(status, "Unknown document version", request), status);
        }

        return new ResponseEntity<>(document.getChanges(fromVersion), HttpStatus.OK);
    }

    @RequestMapping(value = "/documents", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity createDocument(
            @RequestParam String name
    ) {
        return new ResponseEntity<>(storage.createDocument(name), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/documents/{id}/name", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity renameDocument(
            @PathVariable("id") long id,
            @RequestParam String name,
            HttpServletRequest request
    ) {
        PlainTextDocument document = storage.getDocument(id);
        if (document == null) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            return new ResponseEntity<>(new ApiError(status, "Document not found", request), status);
        } else {
            document.setName(name);
            return new ResponseEntity<>(document, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/documents/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity deleteDocument(
            @PathVariable("id") long id,
            HttpServletRequest request
    ) {
        PlainTextDocument document = storage.removeDocument(id);
        if (document == null) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            return new ResponseEntity<>(new ApiError(status, "Document not found", request), status);
        }
        return new ResponseEntity<>(document, HttpStatus.OK);
    }


    @RequestMapping(value = "/documents/{id}/changes", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity editDocument(
            @PathVariable("id") long id,
            @Valid @RequestBody EditImpl edit,
            HttpServletRequest request
    ) {
        PlainTextDocument document = storage.getDocument(id);
        if (document == null) {
            HttpStatus status = HttpStatus.NOT_FOUND;
            return new ResponseEntity<>(new ApiError(status, "Document not found", request), status);
        }
        long userId = RequestContextHolder.currentRequestAttributes().getSessionId().hashCode();
        List<Change> resultChanges;
        try {
            resultChanges = processor.processEdit(document, edit, userId);
        } catch (EditValidationException e) {
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return new ResponseEntity<>(new ApiError(status, e.getMessage(), request), status);
        }
        return new ResponseEntity<>(resultChanges, HttpStatus.OK);
    }

}