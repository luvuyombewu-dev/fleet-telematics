package za.co.fleettelematics.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resourceName, Object id) {
        super(resourceName + " with ID " + id + " was not found.");
    }
}
