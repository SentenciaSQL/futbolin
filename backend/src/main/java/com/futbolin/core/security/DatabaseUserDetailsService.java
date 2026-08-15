package com.futbolin.core.security;

import com.futbolin.data.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    public DatabaseUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return users.findByEmail(username)
                .or(() -> users.findByUsername(username))
                .map(u -> new UserPrincipal(u.getId(), u.getUsername(), u.getPasswordHash(), u.getRole(), u.isEnabled(), u.isLocked()))
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }
}
