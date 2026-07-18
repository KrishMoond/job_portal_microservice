package com.jobportal.user.service;

import com.jobportal.common.exception.BadRequestException;
import com.jobportal.common.exception.ResourceNotFoundException;
import com.jobportal.user.dto.RegisterRequest;
import com.jobportal.user.dto.UserResponse;
import com.jobportal.user.dto.UserUpdateRequest;
import com.jobportal.user.model.User;
import com.jobportal.user.repository.UserRepository;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;

    @Value("${mail.from.name:HireHub No-Reply}")
    private String mailFromName;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    public UserResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            User existing = userRepository.findByEmail(req.getEmail()).orElse(null);
            if (existing != null && !existing.isEmailVerified()) {
                String otp = generateOtp();
                existing.setVerificationOtp(passwordEncoder.encode(otp));
                existing.setVerificationOtpExpiry(LocalDateTime.now().plusMinutes(10));
                existing.setOtpAttempts(0);
                userRepository.save(existing);
                sendOtpEmail(existing.getEmail(), otp);
                throw new BadRequestException("UNVERIFIED_EMAIL");
            }
            throw new BadRequestException("Email already registered");
        }
        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(req.getRole());
        user.setEmailVerified(false);
        String otp = generateOtp();
        user.setVerificationOtp(passwordEncoder.encode(otp));
        user.setVerificationOtpExpiry(LocalDateTime.now().plusMinutes(10));
        user = userRepository.save(user);
        sendOtpEmail(user.getEmail(), otp);
        return toResponse(user);
    }

    public void verifyEmail(String email, String otp) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BadRequestException("No account found with that email"));
        if (user.isEmailVerified())
            throw new BadRequestException("Email is already verified");
        if (user.getVerificationOtp() == null)
            throw new BadRequestException("No pending OTP for this account");
        if (LocalDateTime.now().isAfter(user.getVerificationOtpExpiry()))
            throw new BadRequestException("OTP has expired. Please request a new OTP");
        if (user.getOtpAttempts() >= 3) {
            user.setVerificationOtp(null);
            user.setVerificationOtpExpiry(null);
            userRepository.save(user);
            throw new BadRequestException("Too many failed attempts. Please request a new OTP");
        }
        if (!passwordEncoder.matches(otp, user.getVerificationOtp())) {
            user.setOtpAttempts(user.getOtpAttempts() + 1);
            userRepository.save(user);
            throw new BadRequestException("Invalid OTP");
        }
        user.setEmailVerified(true);
        user.setVerificationOtp(null);
        user.setVerificationOtpExpiry(null);
        user.setOtpAttempts(0);
        userRepository.save(user);
    }

    public void resendOtp(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BadRequestException("No account found with that email"));
        String otp = generateOtp();
        user.setVerificationOtp(passwordEncoder.encode(otp));
        user.setVerificationOtpExpiry(LocalDateTime.now().plusMinutes(10));
        user.setOtpAttempts(0);
        userRepository.save(user);
        if (!user.isEmailVerified()) {
            sendOtpEmail(user.getEmail(), otp);
        } else {
            sendLoginOtpEmail(user.getEmail(), otp);
        }
    }

    private String generateOtp() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }

    private void sendOtpEmail(String email, String otp) {
        sendEmail(email, "Verify Your Email - HireHub",
            "Your email verification OTP is: " + otp + "\n\nThis OTP will expire in 10 minutes.");
    }

    public void sendLoginOtpEmail(String email, String otp) {
        sendEmail(email, "Your Login OTP - HireHub",
            "Your login OTP is: " + otp + "\n\nThis OTP will expire in 10 minutes.\nIf you did not attempt to log in, please ignore this email.");
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(new InternetAddress(mailFrom, mailFromName));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    public UserResponse getById(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        return toResponse(user);
    }

    public UserResponse update(String userId, RegisterRequest req) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        if (!user.getEmail().equals(req.getEmail()) && userRepository.existsByEmail(req.getEmail()))
            throw new BadRequestException("Email already in use");
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        if (req.getPassword() != null && !req.getPassword().isBlank())
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        return toResponse(userRepository.save(user));
    }

    public UserResponse updateProfile(String userId, UserUpdateRequest req) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        if (req.getName() != null) user.setName(req.getName());
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getLocation() != null) user.setLocation(req.getLocation());
        if (req.getExperienceLevel() != null) user.setExperienceLevel(req.getExperienceLevel());
        if (req.getEducation() != null) user.setEducation(req.getEducation());
        if (req.getPreferredJobTypes() != null) user.setPreferredJobTypes(req.getPreferredJobTypes());
        return toResponse(userRepository.save(user));
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole(),
                               u.getPhone(), u.getLocation(), u.getExperienceLevel(),
                               u.getEducation(), u.getPreferredJobTypes());
    }
}
