package app.krista.extensions.essentials.collaboration.azureauthentication.exception;

import java.io.IOException;

/**
 * The {@code FileOperationException} class is a custom unchecked exception that extends
 * {@link RuntimeException}. It is used to indicate that an error occurred during a file
 * operation, typically when loading or processing a file, and wraps an {@link IOException}
 * as its cause.
 *
 * <p>This exception can be thrown in cases where file I/O operations fail, allowing the caller
 * to handle file-related errors in a more descriptive way without being forced to catch checked
 * exceptions.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * try {
 *     // Code that may throw an IOException
 * } catch (IOException e) {
 *     throw new FileOperationException("Error occurred while loading the page", e);
 * }
 * </pre>
 */
public class FileOperationException extends RuntimeException {

    /**
     * Constructs a new {@code FileOperationException} with the specified detail message
     * and cause.
     *
     * @param errorOccurredWhileLoadingPage the detail message indicating the specific error
     * @param cause                         the cause of the exception, typically an {@link IOException} that triggered this exception
     */
    public FileOperationException(String errorOccurredWhileLoadingPage, IOException cause) {
        super(errorOccurredWhileLoadingPage, cause);
    }
}
