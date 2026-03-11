/*
 * Azure Authentication Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
