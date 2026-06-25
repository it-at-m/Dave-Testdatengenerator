package de.muenchen.oss.refarch.backend.dave.client;

/**
 * Raised when a call to the DAVe backend fails (transport error or non-2xx response).
 */
public class DaveBackendException extends RuntimeException {

    public DaveBackendException(final String message) {
        super(message);
    }

    public DaveBackendException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
