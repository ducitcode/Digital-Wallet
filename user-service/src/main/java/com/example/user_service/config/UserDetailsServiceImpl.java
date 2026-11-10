package com.example.user_service.config;

import com.example.user_service.entity.User;
import com.example.user_service.exception.BusinessException;
import com.example.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username.toLowerCase()))
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản"));
        if (user.getStatus() != com.example.user_service.enums.UserStatus.ACTIVE)
            throw new BusinessException(HttpStatus.FORBIDDEN, "Tài khoản không hoạt động");
        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPassword()).authorities("ROLE_" + user.getRole().name()).build();
    }
}
