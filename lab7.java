import java.util.*;
import java.util.stream.*;

public class lab7 {

	// таска 1.
	public static String[] shorterThanAverage(String[] arr) {
		double avg = Arrays.stream(arr).mapToInt(String::length).average().orElse(0);
		return Arrays.stream(arr).filter(s -> s.length() < avg).toArray(String[]::new);
	}
	public static String[] longerThanAverage(String[] arr) {
		double avg = Arrays.stream(arr).mapToInt(String::length).average().orElse(0);
		return Arrays.stream(arr).filter(s -> s.length() > avg).toArray(String[]::new);
	}

	// таска 2. ворд з мінімальною кількістю різних символів (перше якщо декілька)
	public static String[] wordWithMinDistinctChars(String line) {
		String[] words = splitWords(line);
		Optional<String> opt = Arrays.stream(words)
			.min(Comparator.comparingInt(s -> (int) s.chars().distinct().count()));
		return opt.map(s -> new String[]{s}).orElse(new String[0]);
	}

	// таска 3. ворди тільки латинські, серед них ті з рівною кількістю голосних і приголосних
	public static String[] latinWordsWithEqualVowelsAndConsonants(String line) {
		String[] words = splitWords(line);
		return Arrays.stream(words)
			.filter(lab7::isLatinWord)
			.filter(s -> {
				long vowels = s.chars().mapToObj(c -> (char)c)
					.filter(c -> "aeiouAEIOU".indexOf(c) >= 0).count();
				long consonants = s.length() - vowels;
				return vowels == consonants;
			})
			.toArray(String[]::new);
	}

	// таска 4. ворди, символи яких йдуть у порядку зростання кодів (строго)
	public static String[] wordsWithIncreasingCharCodes(String line) {
		String[] words = splitWords(line);
		return Arrays.stream(words)
			.filter(s -> {
				return s.chars().boxed()
					.collect(Collectors.toList())
					.stream()
					.reduce(new int[]{-1, 1}, (acc, c) -> {
						// acc[0]=prev, acc[1]=valid(1/0)
						if (acc[1] == 0) return new int[]{c, 0};
						if (acc[0] == -1) return new int[]{c, 1};
						return new int[]{c, (acc[0] < c) ? 1 : 0};
					}, (a,b)->a)[1] == 1;
			})
			.toArray(String[]::new);
	}

	// таска 5. ворди, що складаються тільки з різних символів
	public static String[] wordsWithAllUniqueChars(String line) {
		String[] words = splitWords(line);
		return Arrays.stream(words)
			.filter(s -> s.chars().distinct().count() == s.length())
			.toArray(String[]::new);
	}

	// таска 6. серед простих <= n знайти таке, в бінарі якого макс кількість одиниць
	public static int primeWithMaxOnesInBinary(int n) {
		return primesUpTo(n).stream()
			.max(Comparator.comparingInt(p -> Integer.bitCount(p)))
			.orElse(-1);
	}

	// таска 7. серед простих <= n знайти таке, в бінарі якого макс кількість нулів
	public static int primeWithMaxZerosInBinary(int n) {
		return primesUpTo(n).stream()
			.max(Comparator.comparingInt(p -> {
				int bits = Integer.toBinaryString(p).length();
				return bits - Integer.bitCount(p);
			}))
			.orElse(-1);
	}

	// таска 8. емаунт надпростих чисел <= 1000 (реверс-парніми) або <= given (<=1000)
	public static int countSuperPrimes(int n) {
		int limit = Math.min(n, 1000);
		return (int) IntStream.rangeClosed(2, limit)
			.filter(p -> isPrime(p) && isPrime(reverseInt(p)))
			.count();
	}

	// таска 9. знайти всі досконалі числа від 1 до n
	public static int[] perfectNumbersUpTo(int n) {
		return IntStream.rangeClosed(2, n)
			.filter(lab7::isPerfect)
			.toArray();
	}

	// ----------------- допоміжні методи -----------------
	private static String[] splitWords(String line) {
		if (line == null || line.trim().isEmpty()) return new String[0];
		return line.trim().split("\\s+");
	}

	private static boolean isLatinWord(String s) {
		return s.matches("[A-Za-z]+");
	}

	private static boolean isPrime(int x) {
		if (x < 2) return false;
		if (x == 2) return true;
		if (x % 2 == 0) return false;
		int r = (int) Math.sqrt(x);
		for (int i = 3; i <= r; i += 2)
			if (x % i == 0) return false;
		return true;
	}

	private static int reverseInt(int x) {
		int rev = 0;
		while (x > 0) {
			rev = rev * 10 + (x % 10);
			x /= 10;
		}
		return rev;
	}

	private static List<Integer> primesUpTo(int n) {
		return IntStream.rangeClosed(2, n).filter(lab7::isPrime).boxed().collect(Collectors.toList());
	}

	private static boolean isPerfect(int x) {
		int sum = 1;
		int r = (int) Math.sqrt(x);
		for (int i = 2; i <= r; ++i) {
			if (x % i == 0) {
				sum += i;
				int j = x / i;
				if (j != i) sum += j;
			}
		}
		return x != 1 && sum == x;
	}

	// ----------------- main для короткої демонстрації -----------------
	public static void main(String[] args) {
		// Приклади використання (можна замінити на власні дані)
		String[] arr = {"one", "three", "four", "sixty", "a", "alphabet"};
		System.out.println("Shorter than avg: " + Arrays.toString(shorterThanAverage(arr)));
		System.out.println("Longer than avg: " + Arrays.toString(longerThanAverage(arr)));

		String line = "hello aaa abcde aabbb cba 123 abba AbBa";
		System.out.println("Min distinct chars word: " + Arrays.toString(wordWithMinDistinctChars(line)));
		System.out.println("Latin with equal vowels/consonants: " + Arrays.toString(latinWordsWithEqualVowelsAndConsonants(line)));
		System.out.println("Increasing char codes: " + Arrays.toString(wordsWithIncreasingCharCodes(line)));
		System.out.println("All unique chars: " + Arrays.toString(wordsWithAllUniqueChars(line)));

		int n = 100;
		System.out.println("Prime with max ones in binary <= " + n + ": " + primeWithMaxOnesInBinary(n));
		System.out.println("Prime with max zeros in binary <= " + n + ": " + primeWithMaxZerosInBinary(n));
		System.out.println("Super-primes <= 1000 (count): " + countSuperPrimes(1000));
		System.out.println("Perfect numbers up to 10000: " + Arrays.toString(perfectNumbersUpTo(10000)));
	}
}