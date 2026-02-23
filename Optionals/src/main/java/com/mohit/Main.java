package com.mohit;

import java.util.Optional;

public class Main {

    static class User {
        private String name;
        private String email;

        public User(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        // INTERVIEW TRICK: A getter that returns an Optional
        // This is exactly when you are forced to use flatMap()
        public Optional<String> getEmail() {
            return Optional.ofNullable(email);
        }
    }

    // 2. Simulating a Database Repository
    static class UserRepository {
        public Optional<User> findById(int id) {
            if (id == 1) {
                return Optional.of(new User("Alice", "alice@example.com"));
            } else if (id == 2) {
                return Optional.of(new User("Bob", null)); // Bob exists, but has no email
            }
            return Optional.empty(); // User not found (Database miss)
        }
    }
    public static void main(String[] args) {
        UserRepository repository = new UserRepository();

        System.out.println("--- 1. Basic Extraction ---");

        // Scenario A: User exists
        User user1 = repository.findById(1).orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("Found User 1: " + user1.getName());

        // Scenario B: User is missing, provide a fallback
        User defaultUser = repository.findById(99).orElse(new User("Guest Worker", null));
        System.out.println("Missing User 99 defaults to: " + defaultUser.getName());


        System.out.println("\n--- 2. map() vs flatMap() ---");

        Optional<User> optAlice = repository.findById(1);

        // map() is used when the method returns a raw value (String)
        // optAlice.map(User::getName) returns Optional<String>
        String alicesName = optAlice.map(User::getName).orElse("Unknown");
        System.out.println("Alice's Name via map(): " + alicesName);

        // flatMap() is used when the method ALREADY returns an Optional<String>.
        // If you used map(User::getEmail), you would get Optional<Optional<String>> (a nested nightmare).
        // flatMap "flattens" it into a single Optional<String>.
        String alicesEmail = optAlice.flatMap(User::getEmail).orElse("No Email Provided");
        System.out.println("Alice's Email via flatMap(): " + alicesEmail);


        System.out.println("\n--- 3. Handling Null Data Gracefully ---");

        // Bob exists, but his email is null in the database.
        // Instead of crashing with a NullPointerException, Optional protects us.
        String bobsEmail = repository.findById(2)
                .flatMap(User::getEmail)
                .orElse("default@company.com");

        System.out.println("Bob's Email (Fallback triggered): " + bobsEmail);


        System.out.println("\n--- 4. The Clean Pipeline (No if-statements) ---");

        // This is how a Senior writes business logic.
        // "Find user 1, get their email, check if it contains 'example', and if so, print it."
        repository.findById(1)
                .flatMap(User::getEmail)
                .filter(email -> email.contains("@example.com"))
                .ifPresent(email -> System.out.println("Valid Email Verified: " + email));

    }
}


/*
Output:
--- 1. Basic Extraction ---
Found User 1: Alice
Missing User 99 defaults to: Guest Worker

--- 2. map() vs flatMap() ---
Alice's Name via map(): Alice
Alice's Email via flatMap(): alice@example.com

--- 3. Handling Null Data Gracefully ---
Bob's Email (Fallback triggered): default@company.com

--- 4. The Clean Pipeline (No if-statements) ---
Valid Email Verified: alice@example.com

 */