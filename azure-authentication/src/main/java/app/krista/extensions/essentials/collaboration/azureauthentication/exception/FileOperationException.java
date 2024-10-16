package app.krista.extensions.essentials.collaboration.azureauthentication.exception;

import java.io.IOException;

public class FileOperationException extends RuntimeException {
    public FileOperationException(String errorOccurredWhileLoadingPage, IOException cause) {
        super(errorOccurredWhileLoadingPage, cause);
    }
}
