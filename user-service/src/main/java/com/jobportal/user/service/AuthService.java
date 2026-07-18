package com.jobportal.user.service;

import com.jobportal.common.exception.BadRequestException;
import com.jobportal.user.dto.LoginRequest;
import com.jobportal.user.model.User;
import com.jobportal.user.repository.UserRepository;
import com.jobportal.user.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }
    public String login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
            .orElseThrow(() -> new BadRequestException("Invalid credentials"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword()))
            throw new BadRequestException("Invalid credentials");
        if (!user.isEmailVerified())
            throw new BadRequestException("Please verify your email before logging in");
        String otp = generateOtp();
        user.setVerificationOtp(passwordEncoder.encode(otp));
        user.setVerificationOtpExpiry(LocalDateTime.now().plusMinutes(10));
        user.setOtpAttempts(0);
        userRepository.save(user);
        userService.sendLoginOtpEmail(user.getEmail(), otp);
        return user.getEmail();
    }

    public Map<String, String> verifyLoginOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BadRequestException("Invalid credentials"));
        if (user.getVerificationOtp() == null)
            throw new BadRequestException("No pending OTP. Please login again.");
        if (LocalDateTime.now().isAfter(user.getVerificationOtpExpiry()))
            throw new BadRequestException("OTP has expired. Please login again.");
        if (user.getOtpAttempts() >= 3) {
            user.setVerificationOtp(null);
            user.setVerificationOtpExpiry(null);
            userRepository.save(user);
            throw new BadRequestException("Too many failed attempts. Please login again.");
        }
        if (!passwordEncoder.matches(otp, user.getVerificationOtp())) {
            user.setOtpAttempts(user.getOtpAttempts() + 1);
            userRepository.save(user);
            throw new BadRequestException("Invalid OTP");
        }
        user.setVerificationOtp(null);
        user.setVerificationOtpExpiry(null);
        user.setOtpAttempts(0);
        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getId(), user.getRole().name());
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("role", user.getRole().name());
        return result;
    }

    private String generateOtp() {
        return String.format("%06d", new SecureRandom().nextInt(1_000_000));
    }
}
