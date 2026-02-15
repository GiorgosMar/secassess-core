package org.secassess.core.enums;

/**
 * Enumeration representing the available user roles in the application.
 */
public enum UserRole {
    ADMIN,
    USER,
    AUDITOR;

    public String roleName() {
        return "ROLE_" + this.name();
    }
}