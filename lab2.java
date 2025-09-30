// lab 2 by Lomakin Maksym.
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

// клас для представлення домашньої адреси
class Address {
    private String street;
    private String house;
    private String apartment;

    public Address(String street, String house, String apartment) {
        this.street = street;
        this.house = house;
        this.apartment = apartment;
    }

    // геттери
    public String getStreet() { return street; }
    public String getHouse() { return house; }
    public String getApartment() { return apartment; }

    @Override
    public String toString() {
        return String.format("вул. %s, буд. %s, кв. %s", street, house, apartment);
    }
}

// клас для представлення запису в журналі куратора
class StudentRecord {
    private String lastName;
    private String firstName;
    private LocalDate birthDate;
    private String phone;
    private Address address;

    public StudentRecord(String lastName, String firstName, LocalDate birthDate, 
                        String phone, Address address) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.birthDate = birthDate;
        this.phone = phone;
        this.address = address;
    }

    // агейн геттери
    public String getLastName() { return lastName; }
    public String getFirstName() { return firstName; }
    public LocalDate getBirthDate() { return birthDate; }
    public String getPhone() { return phone; }
    public Address getAddress() { return address; }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return String.format(
            "Last name: %s\nFirst name: %s\nBirth date: %s\nPhone: %s\nAddress: %s\n",
            lastName, firstName, birthDate.format(formatter), phone, address
        );
    }
}

// мейн клас програми
public class lab2 {
    private static List<StudentRecord> journal = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?3?8?0\\d{9}$|^\\d{10}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[А-Яа-яІіЇїЄєA-Za-z]+$");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static void main(String[] args) {
        System.out.println("=== Curator's Journal ===\n");
        
        while (true) {
            showMenu();
            int choice = getChoice();
            
            switch (choice) {
                case 1:
                    addStudentRecord();
                    break;
                case 2:
                    displayAllRecords();
                    break;
                case 3:
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice! Please try again.\n");
            }
        }
    }

    private static void showMenu() {
        System.out.println("Select an action:");
        System.out.println("1. Add new record");
        System.out.println("2. Show all records");
        System.out.println("3. Exit");
        System.out.print("Your choice: ");
    }

    private static int getChoice() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void addStudentRecord() {
        System.out.println("\n=== Adding new record ===");
        
        String lastName = getValidatedName("Enter student's last name: ");
        String firstName = getValidatedName("Enter student's first name: ");
        LocalDate birthDate = getValidatedDate("Enter birth date (dd.MM.yyyy): ");
        String phone = getValidatedPhone("Enter phone number: ");
        Address address = getValidatedAddress();
        
        StudentRecord record = new StudentRecord(lastName, firstName, birthDate, phone, address);
        journal.add(record);
        
        System.out.println("Record added successfully!\n");
    }

    private static String getValidatedName(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                System.out.println("Error: Field cannot be empty!");
                continue;
            }
            
            if (!NAME_PATTERN.matcher(input).matches()) {
                System.out.println("Error: Name can contain only letters!");
                continue;
            }
            
            return input;
        }
    }

    private static LocalDate getValidatedDate(String prompt) {
        LocalDate minDate = LocalDate.of(1950, 1, 1);
        LocalDate maxDate = LocalDate.now().minusYears(14);
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            // Check format first
            String[] parts = input.split("\\.");
            if (parts.length != 3) {
                System.out.println("Invalid date format! Use dd.MM.yyyy");
                continue;
            }
            int day, month, year;
            try {
                day = Integer.parseInt(parts[0]);
                month = Integer.parseInt(parts[1]);
                year = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid date format! Use dd.MM.yyyy");
                continue;
            }
            if (month < 1 || month > 12) {
                System.out.println("Invalid month value! Month must be between 1 and 12.");
                continue;
            }
            if (year < 1950) {
                System.out.println("Birth date cannot be earlier than 01.01.1950.");
                continue;
            }
            // Check day validity for month/year
            boolean validDay = true;
            int[] daysInMonth = {31, (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
            if (day < 1 || day > daysInMonth[month - 1]) {
                validDay = false;
            }
            if (!validDay) {
                System.out.printf("Invalid day value for the specified month! Month %d has only %d days.\n", month, daysInMonth[month - 1]);
                continue;
            }
            try {
                LocalDate date = LocalDate.parse(input, DATE_FORMATTER);
                if (date.isAfter(maxDate)) {
                    System.out.println("You must be at least 14 years old.");
                    continue;
                }
                if (date.isBefore(minDate)) {
                    System.out.println("Birth date cannot be earlier than 01.01.1950.");
                    continue;
                }
                return date;
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date! Please check your input.");
            }
        }
    }

    private static String getValidatedPhone(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().replaceAll("[\\s\\-\\(\\)]", "");
            
            if (input.isEmpty()) {
                System.out.println("Error: Phone number cannot be empty!");
                continue;
            }
            
            if (!PHONE_PATTERN.matcher(input).matches()) {
                System.out.println("Error: Invalid phone number format! Use: +380XXXXXXXXX or 0XXXXXXXXX");
                continue;
            }
            
            // Normalize to +380XXXXXXXXX
            if (input.startsWith("0")) {
                input = "+38" + input;
            } else if (input.startsWith("380")) {
                input = "+" + input;
            } else if (input.startsWith("80")) {
                input = "+3" + input;
            } else if (!input.startsWith("+")) {
                input = "+380" + input;
            }
            
            return input;
        }
    }

    private static Address getValidatedAddress() {
        System.out.println("Enter address:");
        
        String street = getValidatedString("Street: ");
        String house = getValidatedString("House: ");
        String apartment = getValidatedString("Apartment: ");
        
        return new Address(street, house, apartment);
    }

    private static String getValidatedString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                System.out.println("Error: Field cannot be empty!");
                continue;
            }
            
            return input;
        }
    }

    private static void displayAllRecords() {
        System.out.println("\n=== All records in the journal ===");
        
        if (journal.isEmpty()) {
            System.out.println("Journal is empty.\n");
            return;
        }
        
        for (int i = 0; i < journal.size(); i++) {
            System.out.printf("--- Record #%d ---\n", i + 1);
            System.out.println(journal.get(i));
        }
    }
}