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
            "Прізвище: %s\nІм'я: %s\nДата народження: %s\nТелефон: %s\nАдреса: %s\n",
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
        System.out.println("=== Журнал куратора ===\n");
        
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
                    System.out.println("До побачення!");
                    return;
                default:
                    System.out.println("Невірний вибір! Спробуйте ще раз.\n");
            }
        }
    }

    private static void showMenu() {
        System.out.println("Виберіть дію:");
        System.out.println("1. Додати новий запис");
        System.out.println("2. Показати всі записи");
        System.out.println("3. Вийти");
        System.out.print("Ваш вибір: ");
    }

    private static int getChoice() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void addStudentRecord() {
        System.out.println("\n=== Додавання нового запису ===");
        
        String lastName = getValidatedName("Введіть прізвище студента: ");
        String firstName = getValidatedName("Введіть ім'я студента: ");
        LocalDate birthDate = getValidatedDate("Введіть дату народження (dd.MM.yyyy): ");
        String phone = getValidatedPhone("Введіть номер телефону: ");
        Address address = getValidatedAddress();
        
        StudentRecord record = new StudentRecord(lastName, firstName, birthDate, phone, address);
        journal.add(record);
        
        System.out.println("Запис успішно додано!\n");
    }

    private static String getValidatedName(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                System.out.println("Помилка: Поле не може бути порожнім!");
                continue;
            }
            
            if (!NAME_PATTERN.matcher(input).matches()) {
                System.out.println("Помилка: Ім'я може містити тільки літери!");
                continue;
            }
            
            return input;
        }
    }

    private static LocalDate getValidatedDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            
            try {
                LocalDate date = LocalDate.parse(input, DATE_FORMATTER);
                
                if (date.isAfter(LocalDate.now())) {
                    System.out.println("Помилка: Дата народження не може бути в майбутньому!");
                    continue;
                }
                
                if (date.isBefore(LocalDate.now().minusYears(100))) {
                    System.out.println("Помилка: Дата народження занадто давня!");
                    continue;
                }
                
                return date;
            } catch (DateTimeParseException e) {
                System.out.println("Помилка: Невірний формат дати! Використовуйте dd.MM.yyyy");
            }
        }
    }

    private static String getValidatedPhone(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().replaceAll("[\\s\\-\\(\\)]", "");
            
            if (input.isEmpty()) {
                System.out.println("Помилка: Номер телефону не може бути порожнім!");
                continue;
            }
            
            if (!PHONE_PATTERN.matcher(input).matches()) {
                System.out.println("Помилка: Невірний формат номера телефону! " +
                                 "Використовуйте формат: +380XXXXXXXXX або 0XXXXXXXXX");
                continue;
            }
            
            // нормалізація номера до формату +380XXXXXXXXX
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
        System.out.println("Введіть адресу:");
        
        String street = getValidatedString("Вулиця: ");
        String house = getValidatedString("Будинок: ");
        String apartment = getValidatedString("Квартира: ");
        
        return new Address(street, house, apartment);
    }

    private static String getValidatedString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                System.out.println("Помилка: Поле не може бути порожнім!");
                continue;
            }
            
            return input;
        }
    }

    private static void displayAllRecords() {
        System.out.println("\n=== Всі записи в журналі ===");
        
        if (journal.isEmpty()) {
            System.out.println("Журнал порожній.\n");
            return;
        }
        
        for (int i = 0; i < journal.size(); i++) {
            System.out.printf("--- Запис #%d ---\n", i + 1);
            System.out.println(journal.get(i));
        }
    }
}