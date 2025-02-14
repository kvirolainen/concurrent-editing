The main interfaces are Storage, Document, Change, Edit. And there is an enum Operation.

The `storage` is just a document repository.
The `document` contains text.
The `change` is user action that is already processed by the server.
The `edit` is user action in the user local version of the document, that is not processed yet.
The `operation` is user action type: insertion or deletion.

A user works with the document, which is needed to be synced with the server.
The user doesn't need to download the entire document to get the update, he has to request just the latest changes from the server.
When editing, the user sends an `edit` to the server from the user local version of the document, and as a response, he receives a list of document changes, including the result of the merge.

Merge rules.
When the user inserts characters, he should see all added characters in a result text.
When the user deletes characters, he deletes these characters only in the local version of the document.
If two users delete the same characters, the server should not delete them twice.

Merge logic.
Main logic is in the function model.Processor::processEdit.
1. Synchronize the document to avoid the parallel merge.
2. Get the list of changes from the server (list of differences between server version and user local version of the document).
3. Transform the user edit according to the list of changes.
4. Return the list of changes including transform result.
