package com.shivani.notepad.config;

import com.shivani.notepad.entity.User;
import com.shivani.notepad.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;
/**
 * Bridges our own User entity/repository with Spring Security's
 * authentication mechanism.
 *
 * Spring Security calls loadUserByUsername() during the login process;
 * we fetch our custom User entity and wrap it in Spring Security's
 * built-in UserDetails implementation so the framework can compare
 * the submitted password (hashed) against the stored hash.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService{
    private final UserRepository userRepository;
    @Autowired
    public CustomUserDetailsService(UserRepository userRepository){
        this.userRepository = userRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("No user found with username: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEmailVerified(), // enabled
                true,  // accountNonExpired
                true,  // credentialsNonExpired
                true,  // accountNonLocked
                AuthorityUtils.createAuthorityList("ROLE_USER")
        );
    }
}
