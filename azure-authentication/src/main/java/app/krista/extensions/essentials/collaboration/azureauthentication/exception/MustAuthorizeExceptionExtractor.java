package app.krista.extensions.essentials.collaboration.azureauthentication.exception;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.model.field.NamedValuedField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static app.krista.extensions.essentials.collaboration.azureauthentication.util.Constants.*;

/**
 * The {@code MustAuthorizeExceptionExtractor} class is responsible for extracting relevant details
 * from a {@link MustAuthorizeException} and providing access to those details through getter methods.
 * It encapsulates information such as extracted fields, the method name, and the authorizer name
 * associated with the exception.
 *
 * <p>This class is typically used to analyze and handle exceptions that require authorization.
 * The extracted fields provide key-value pairs of additional information, and the method and authorizer
 * names provide context for where and by whom the exception was triggered.</p>
 */
public class MustAuthorizeExceptionExtractor implements ExceptionFieldsExtractor {
    private final Map<String, String> fields;
    private final String methodName;
    private final String authorizerName;

    /**
     * Constructs a {@code MustAuthorizeExceptionExtractor} by extracting information from
     * the provided {@link MustAuthorizeException}.
     *
     * <p>This constructor initializes the fields map, the method name, and the authorizer name
     * based on the data available in the given exception.</p>
     *
     * @param mustAuthorizeException the exception from which the details are extracted
     */
    public MustAuthorizeExceptionExtractor(MustAuthorizeException mustAuthorizeException) {
        this.fields = extractFields(mustAuthorizeException);
        this.methodName = mustAuthorizeException.getMethodName();
        this.authorizerName = mustAuthorizeException.getAuthorizerName();
    }

    /**
     * Returns the map of extracted fields from the {@link MustAuthorizeException}.
     *
     * @return a map of fields containing key-value pairs.
     */
    public Map<String, String> getFields() {
        return fields;
    }

    /**
     * Returns the name of the method where the exception occurred.
     *
     * @return the method name
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Returns the name of the authorizer responsible for the authorization process.
     *
     * @return the authorizer name
     */
    public String getAuthorizerName() {
        return authorizerName;
    }

    /**
     * Extracts the fields from the {@link MustAuthorizeException} and returns them as a map
     * of key-value pairs.
     *
     * <p>This method is used internally by the constructor to initialize the fields map,
     * providing additional details about the authorization exception.</p>
     *
     * @param mustAuthorizeException the exception from which the fields are extracted
     * @return a map of key-value pairs containing extracted fields
     */
    private Map<String, String> extractFields(MustAuthorizeException mustAuthorizeException) {
        Map<String, String> fieldsMap = new HashMap<>();
        List<NamedValuedField> exceptionFields = mustAuthorizeException.getDetails();
        for (NamedValuedField namedValuedField : exceptionFields) {
            if (Objects.equals(namedValuedField.getName(), CLIENT_ID)) {
                fieldsMap.put(CLIENT_ID, (String) namedValuedField.getValue());
            }
            if (Objects.equals(namedValuedField.getName(), CLIENT_SECRET)) {
                fieldsMap.put(CLIENT_SECRET, (String) namedValuedField.getValue());
            }
            if (Objects.equals(namedValuedField.getName(), TENANT_ID)) {
                fieldsMap.put(TENANT_ID, (String) namedValuedField.getValue());
            }
        }
        return fieldsMap;
    }
}
