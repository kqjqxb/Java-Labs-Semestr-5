//lab5 by Maksym Lomakin
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;

public class lab5 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n--- Lab 5 Menu ---");
            System.out.println("1. Find line with max words in file");
            System.out.println("2. Save/load object set (Simple OOP)");
            System.out.println("3. Encrypt/Decrypt file");
            System.out.println("4. Count HTML tag frequency from URL");
            System.out.println("0. Exit");
            System.out.print("Choose option: ");
            String choice = sc.nextLine();
            try {
                switch (choice) {
                    case "1":
                        System.out.print("Enter file path: ");
                        String path1 = sc.nextLine();
                        String maxLine = FileHelper.findMaxWordsLine(path1);
                        System.out.println("Line with max words:\n" + maxLine);
                        break;
                    case "2":
                        OOPMenu.run(sc);
                        break;
                    case "3":
                        System.out.print("Enter input file path: ");
                        String inPath = sc.nextLine();
                        System.out.print("Enter output file path: ");
                        String outPath = sc.nextLine();
                        System.out.print("Enter key character: ");
                        char key = sc.nextLine().charAt(0);
                        System.out.print("Encrypt (e) or Decrypt (d)? ");
                        String mode = sc.nextLine();
                        if (mode.equalsIgnoreCase("e")) {
                            FileHelper.encryptFile(inPath, outPath, key);
                            System.out.println("Encrypted.");
                        } else {
                            FileHelper.decryptFile(inPath, outPath, key);
                            System.out.println("Decrypted.");
                        }
                        break;
                    case "4":
                        System.out.print("Enter URL: ");
                        String url = sc.nextLine();
                        Map<String, Integer> freq = FileHelper.countHtmlTags(url);
                        System.out.println("Tags sorted by name:");
                        freq.entrySet().stream()
                            .sorted(Map.Entry.comparingByKey())
                            .forEach(e -> System.out.println(e.getKey() + ": " + e.getValue()));
                        System.out.println("Tags sorted by frequency:");
                        freq.entrySet().stream()
                            .sorted(Map.Entry.comparingByValue())
                            .forEach(e -> System.out.println(e.getKey() + ": " + e.getValue()));
                        break;
                    case "0":
                        return;
                    default:
                        System.out.println("Invalid option.");
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }
}

// --- Helper class for file operations ---
class FileHelper {
    // Task 1
    public static String findMaxWordsLine(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String maxLine = "";
        int maxWords = 0;
        String line;
        while ((line = br.readLine()) != null) {
            int words = line.trim().isEmpty() ? 0 : line.trim().split("\\s+").length;
            if (words > maxWords) {
                maxWords = words;
                maxLine = line;
            }
        }
        br.close();
        return maxLine;
    }

    // Task 3a: Encrypt
    public static void encryptFile(String inPath, String outPath, char key) throws IOException {
        try (
            Reader r = new FileReader(inPath);
            Writer w = new FileWriter(outPath)
        ) {
            int c;
            while ((c = r.read()) != -1) {
                w.write(c + key);
            }
        }
    }

    // Task 3b: Decrypt
    public static void decryptFile(String inPath, String outPath, char key) throws IOException {
        try (
            Reader r = new FileReader(inPath);
            Writer w = new FileWriter(outPath)
        ) {
            int c;
            while ((c = r.read()) != -1) {
                w.write(c - key);
            }
        }
    }

    // Task 4: Count HTML tags
    public static Map<String, Integer> countHtmlTags(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        Pattern p = Pattern.compile("<\\s*([a-zA-Z0-9]+)");
        Matcher m = p.matcher(sb.toString());
        Map<String, Integer> freq = new HashMap<>();
        while (m.find()) {
            String tag = m.group(1).toLowerCase();
            freq.put(tag, freq.getOrDefault(tag, 0) + 1);
        }
        return freq;
    }
}

// --- Simple OOP object set and menu (Task 2) ---
class MyObject implements Serializable {
    String name;
    int value;
    public MyObject(String name, int value) {
        this.name = name;
        this.value = value;
    }
    public String toString() {
        return name + " (" + value + ")";
    }
}

class OOPMenu {
    static List<MyObject> objects = new ArrayList<>();
    public static void run(Scanner sc) {
        while (true) {
            System.out.println("\n--- Object Set Menu ---");
            System.out.println("1. Add object");
            System.out.println("2. List objects");
            System.out.println("3. Save to file");
            System.out.println("4. Load from file");
            System.out.println("0. Back");
            System.out.print("Choose: ");
            String ch = sc.nextLine();
            try {
                switch (ch) {
                    case "1":
                        System.out.print("Name: ");
                        String name = sc.nextLine();
                        System.out.print("Value: ");
                        int value = Integer.parseInt(sc.nextLine());
                        objects.add(new MyObject(name, value));
                        break;
                    case "2":
                        objects.forEach(System.out::println);
                        break;
                    case "3":
                        System.out.print("File path: ");
                        String fp = sc.nextLine();
                        File file = new File(fp);
                        File parent = file.getParentFile();
                        if (parent != null && !parent.exists()) {
                            parent.mkdirs();
                        }
                        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
                        oos.writeObject(objects);
                        oos.close();
                        System.out.println("Saved.");
                        break;
                    case "4":
                        System.out.print("File path: ");
                        String fp2 = sc.nextLine();
                        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fp2));
                        objects = (List<MyObject>) ois.readObject();
                        ois.close();
                        System.out.println("Loaded.");
                        break;
                    case "0":
                        return;
                    default:
                        System.out.println("Invalid.");
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }
}
