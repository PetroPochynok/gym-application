package com.epam.gym.crm.util;

import com.epam.gym.crm.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.Locale;

@Component
public class CredentialUtil {

    private final UserRepository userRepository;

    private static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();

    public CredentialUtil(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generateUsername(String firstName, String lastName) {
        Collection<String> existingUsernames = userRepository.findAllUsernames();
        return generateUsernameStatic(firstName, lastName, existingUsernames);
    }

    public String generatePassword() {
        return generatePasswordStatic();
    }

    public static String generateUsernameStatic(String firstName, String lastName, Collection<String> existingUsernames) {

        String base = (firstName + "." + lastName).toLowerCase(Locale.ROOT);

        int maxSuffix = 0;
        boolean baseExists = false;

        for (String username : existingUsernames) {

            if (username.equals(base)) {
                baseExists = true;
            }

            if (username.startsWith(base)) {
                String suffixPart = username.substring(base.length());

                if (suffixPart.matches("\\d+")) {
                    int suffix = Integer.parseInt(suffixPart);
                    if (suffix > maxSuffix) {
                        maxSuffix = suffix;
                    }
                }
            }
        }

        if (!baseExists) {
            return base;
        }

        return base + (maxSuffix + 1);
    }

    public static String generatePasswordStatic() {
        StringBuilder sb = new StringBuilder(10);

        for (int i = 0; i < 10; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }

        return sb.toString();
    }
}
