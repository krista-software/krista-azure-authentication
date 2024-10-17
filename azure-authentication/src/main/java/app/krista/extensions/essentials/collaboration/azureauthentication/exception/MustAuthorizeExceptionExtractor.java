package app.krista.extensions.essentials.collaboration.azureauthentication.exception;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.model.field.NamedValuedField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static app.krista.extensions.essentials.collaboration.azureauthentication.util.Constants.*;

public class MustAuthorizeExceptionExtractor implements ExceptionFieldsExtractor {
    private final Map<String, String> fields;
    private final String methodName;
    private final String authorizerName;

    public MustAuthorizeExceptionExtractor(MustAuthorizeException mustAuthorizeException) {
        this.fields = extractFields(mustAuthorizeException);
        this.methodName = mustAuthorizeException.getMethodName();
        this.authorizerName = mustAuthorizeException.getAuthorizerName();
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getAuthorizerName() {
        return authorizerName;
    }

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
