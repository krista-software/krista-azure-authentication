package app.krista.extensions.essentials.collaboration.azureauthentication.exception;


import java.util.Map;

public interface ExceptionFieldsExtractor {
    /**
     * Returns the map of extracted fields from the {@link Exception}.
     *
     * @return a map of fields containing key-value pairs.
     */
    Map<String, String> getFields();
}
