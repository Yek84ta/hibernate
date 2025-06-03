package aut.ap.service;

import aut.ap.model.LoginHistory;
import aut.ap.model.User;
import aut.ap.repository.HibernateUtil;
import aut.ap.repository.UserRepository;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;

public class AuthService {
    private final UserRepository userRepository = new UserRepository();

    public String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes());
        StringBuilder hexString = new StringBuilder();

        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }

    public boolean register(User user, String password) {
        try {
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                return false; // Email already exists
            }

            if (password.length() < 8) {
                return false; // Weak password
            }

            String hashedPassword = hashPassword(password);
            user.setPasswordHash(hashedPassword);
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Optional<User> login(String email, String password, String ipAddress, String deviceInfo) {
        try {
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isEmpty()) {
                return Optional.empty();
            }

            User user = userOptional.get();
            String hashedInput = hashPassword(password);

            boolean loginSuccess = hashedInput.equals(user.getPasswordHash());
            logLoginAttempt(user, loginSuccess, ipAddress, deviceInfo);

            if (loginSuccess) {
                user.setLastLogin(LocalDateTime.now());
                userRepository.update(user);
                return Optional.of(user);
            }

            return Optional.empty();
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private void logLoginAttempt(User user, boolean success, String ipAddress, String deviceInfo) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            LoginHistory log = new LoginHistory(user, success, ipAddress, deviceInfo);
            session.persist(log);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}