package com.relax.owl.common.security;

import java.util.List;

/** Implemented by the user module; credentials never supply their own authorities. */
public interface AuthorizationProvider {
    record Authorization(long version, boolean enabled, List<String> roles, List<String> permissions) {}
    Authorization load(Long userId);
}
