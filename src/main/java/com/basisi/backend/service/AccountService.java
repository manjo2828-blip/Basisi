package com.basisi.backend.service;

import com.basisi.backend.api.account.dto.AccountMeResponse;
import com.basisi.backend.api.account.dto.UpdateEmailRequest;
import com.basisi.backend.api.account.dto.UpdateNameRequest;
import com.basisi.backend.api.account.dto.UpdatePasswordRequest;
import com.basisi.backend.domain.user.User;
import com.basisi.backend.domain.user.UserRepository;
import com.basisi.backend.security.SecurityUtil;
import com.basisi.backend.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AccountService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional(readOnly = true)
    public AccountMeResponse getMe() {
        String email = SecurityUtil.getCurrentUserEmail();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return new AccountMeResponse(user.getId(), user.getEmail(), user.getName(), user.getRole().name(), null);
    }

    @Transactional
    public AccountMeResponse updateName(UpdateNameRequest request) {
        String email = SecurityUtil.getCurrentUserEmail();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (!StringUtils.hasText(request.name())) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }
        user.setName(request.name().trim());
        User saved = userRepository.save(user);
        return new AccountMeResponse(saved.getId(), saved.getEmail(), saved.getName(), saved.getRole().name(), null);
    }

    @Transactional
    public AccountMeResponse updateEmail(UpdateEmailRequest request) {
        String currentEmail = SecurityUtil.getCurrentUserEmail();
        User user = userRepository.findByEmailIgnoreCase(currentEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        String next = request.email() == null ? null : request.email().trim();
        if (!StringUtils.hasText(next)) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }
        // 인증 절차 없이 바로 변경(요구사항). 중복만 방지합니다.
        if (!next.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmailIgnoreCase(next)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        user.setEmail(next);
        User saved = userRepository.save(user);
        // JWT subject가 이메일이므로 이메일 변경 시 토큰을 재발급해 프론트가 즉시 갱신하도록 합니다.
        String newToken = jwtTokenProvider.createAccessToken(saved.getId(), saved.getEmail(), saved.getRole().name());
        return new AccountMeResponse(saved.getId(), saved.getEmail(), saved.getName(), saved.getRole().name(), newToken);
    }

    @Transactional
    public void updatePassword(UpdatePasswordRequest request) {
        String email = SecurityUtil.getCurrentUserEmail();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }
}

