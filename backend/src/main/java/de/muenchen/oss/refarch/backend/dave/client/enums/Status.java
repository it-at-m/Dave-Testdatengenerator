package de.muenchen.oss.refarch.backend.dave.client.enums;

/**
 * Lifecycle status of a Zählung in the DAVe backend.
 */
public enum Status {
    CREATED,
    INSTRUCTED,
    COUNTING,
    ACCOMPLISHED,
    CORRECTION,
    ACTIVE,
    INACTIVE
}
