package com.relax.owl.common.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

/** Field-level checks for operations sharing a resource update DTO. */
public final class PermissionChecks {
    private PermissionChecks() {}
    public static boolean has(String permission) {
        var actor = SecurityContextHolder.getContext().getAuthentication();
        return actor != null && actor.getAuthorities().stream()
                .anyMatch(authority -> permission.equals(authority.getAuthority()));
    }
    public static void require(String permission) {
        if (!has(permission)) throw new AccessDeniedException("Missing " + permission);
    }
}
