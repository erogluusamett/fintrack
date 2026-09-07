package com.fintrack.security;

import com.fintrack.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Spring Security'nin {@link UserDetails} sözleşmesi ile domain {@link User}
 * entity'si arasındaki adaptör. Controller/service katmanı bu tipi
 * {@code @AuthenticationPrincipal} ile enjekte ederek doğrudan
 * {@link #getUserId()} çağırabilir; domain User'ı Spring Security'ye
 * sızdırmadan.
 */
public class AuthenticatedUser implements UserDetails {

    private final UUID userId;
    private final String email;
    private final String passwordHash;
    private final List<GrantedAuthority> authorities;

    public AuthenticatedUser(User user) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .map(GrantedAuthority.class::cast)
                .toList();
    }

    public AuthenticatedUser(UUID userId, String email, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = null;
        this.authorities = List.copyOf(authorities);
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
