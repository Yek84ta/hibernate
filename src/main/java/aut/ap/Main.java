package aut.ap;

import aut.ap.repository.HibernateUtil;
import aut.ap.model.User;
import aut.ap.service.AuthService;

import java.util.Optional;
import java.util.Scanner;

public class Main {
    private static final AuthService authService = new AuthService();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            System.out.print("[L]ogin, [S]ign up, [Q]uit: ");
            String choice = scanner.nextLine().trim().toLowerCase();

            switch (choice) {
                case "l":
                case "login":
                    login();
                    break;
                case "s":
                case "signup":
                    signUp();
                    break;
                case "q":
                case "quit":
                    HibernateUtil.shutdown();
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void login() {
        System.out.println("\n--- Login ---");
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        
        String ipAddress = "192.168.1.1";
        String deviceInfo = "Console App";

        Optional<User> user = authService.login(email, password, ipAddress, deviceInfo);

        if (user.isPresent()) {
            System.out.println("Welcome, " + user.get() + "!");
        } else {
            System.out.println("Invalid email or password");
        }
    }

    private static void signUp() {
        System.out.println("\n--- Sign Up ---");
        System.out.print("First Name: ");
        String firstName = scanner.nextLine().trim();

        System.out.print("Last Name: ");
        String lastName = scanner.nextLine().trim();

        int age = 0;
        while (true) {
            System.out.print("Age: ");
            try {
                age = Integer.parseInt(scanner.nextLine());
                break;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number for age.");
            }
        }

        String email;
        while (true) {
            System.out.print("Email: ");
            email = scanner.nextLine().trim();
            if (email.contains("@") && email.contains(".")) {
                break;
            }
            System.out.println("Please enter a valid email address.");
        }

        String password;
        while (true) {
            System.out.print("Password (min 8 chars): ");
            password = scanner.nextLine();
            if (password.length() >= 8) {
                break;
            }
            System.out.println("Password must be at least 8 characters long.");
        }

        User newUser = new User(email, firstName, lastName, age, "");
        boolean success = authService.register(newUser, password);

        if (success) {
            System.out.println("Registration successful!");
        } else {
            System.out.println("Registration failed. Email may already exist.");
        }
    }
}