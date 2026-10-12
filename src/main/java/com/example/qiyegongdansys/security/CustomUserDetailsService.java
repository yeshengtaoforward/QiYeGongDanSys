package com.example.qiyegongdansys.security;

import com.example.qiyegongdansys.entity.Role;
import com.example.qiyegongdansys.entity.User;
import com.example.qiyegongdansys.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
        return new UserPrincipal(user, resolveRoleCode(user));
    }

    public UserPrincipal loadByUserId(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + id));
        return new UserPrincipal(user, resolveRoleCode(user));
    }

    private String resolveRoleCode(User user) {
        return user.getRoles().stream()
                .map(Role::getCode)
                .sorted()
                .findFirst()
                .orElse("CUSTOMER");
    }
}