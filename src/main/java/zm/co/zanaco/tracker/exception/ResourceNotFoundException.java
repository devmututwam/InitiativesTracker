package zm.co.zanaco.tracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Long id) {
        super("%s not found with id: %d".formatted(resourceName, id));
    }

    public ResourceNotFoundException(String resourceName, String field, String value) {
        super("%s not found with %s: %s".formatted(resourceName, field, value));
    }
}
