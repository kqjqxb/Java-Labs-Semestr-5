// completed by Maksym Lomakin
public class lab1 {
    public static void main(String[] args) {
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        System.out.println("Виберіть режим: simple або hard");
        String mode = scanner.nextLine().trim().toLowerCase();
        if (mode.equals("simple")) {
            System.out.println("Введіть рядок зі словами:");
            String input = scanner.nextLine();
            String result = findWordWithMinUniqueChars(input);
            System.out.println("Слово з мінімальною кількістю різних символів: " + result);
        } else if (mode.equals("hard")) {
            System.out.println("--- DEMO: Custom Class Loader ---");
            System.out.println("Внесіть зміни у TestModule.java та збережіть файл.");
            System.out.println("Class loader буде перевантажувати клас при зміні файлу.");
            try {
                CustomClassReloader demo = new CustomClassReloader();
                demo.runDemo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Невідомий режим. Введіть simple або hard.");
        }
        scanner.close();
    }

    public static String findWordWithMinUniqueChars(String input) {
        if (input == null || input.isEmpty()) return "";
        String[] words = input.trim().split("\\s+");
        int min = Integer.MAX_VALUE;
        String res = "";
        for (String w : words) {
            java.util.Set<Character> set = new java.util.HashSet<>();
            for (char c : w.toCharArray()) set.add(c);
            if (set.size() < min) {
                min = set.size();
                res = w;
            }
        }
        return res;
    }

    // --- HARD TASK ---
    public static class CustomClassReloader extends ClassLoader {
        public Class<?> reloadClass(String className, String classPath) throws Exception {
            java.io.File classFile = new java.io.File(classPath);
            byte[] classData = java.nio.file.Files.readAllBytes(classFile.toPath());
            return defineClass(className, classData, 0, classData.length);
        }
        public void runDemo() throws Exception {
            String className = "TestModule";
            String javaPath = "./TestModule.java";
            String classPath = "./TestModule.class";
            long lastModified = 0;
            while (true) {
                java.io.File javaFile = new java.io.File(javaPath);
                java.io.File classFile = new java.io.File(classPath);
                if (javaFile.exists() && javaFile.lastModified() != lastModified) {
                    lastModified = javaFile.lastModified();
                    System.out.println("Компіляція TestModule.java...");
                    Process compile = Runtime.getRuntime().exec("javac " + javaPath);
                    compile.waitFor();
                    if (!classFile.exists()) {
                        System.out.println("Помилка компіляції TestModule.java");
                        Thread.sleep(2000);
                        continue;
                    }
                    CustomClassReloader loader = new CustomClassReloader();
                    Class<?> clazz = loader.reloadClass(className, classPath);
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    System.out.println("Оновлений клас: " + instance);
                }
                Thread.sleep(2000); // check 2 secs
            }
        }
    }
}
