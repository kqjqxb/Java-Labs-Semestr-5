import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

/*
 * Lab 10: reflection, logging (Log4j2), and internationalization in a single file.
 */
public class lab10 {
	// --- Logging (use java.util.logging so no external dependency required) ---
	private static final java.util.logging.Logger jlogger;

	static {
		jlogger = java.util.logging.Logger.getLogger(lab10.class.getName());
		// Configure console handler (INFO+)
		java.util.logging.ConsoleHandler ch = new java.util.logging.ConsoleHandler();
		ch.setLevel(java.util.logging.Level.INFO);
		ch.setFormatter(new java.util.logging.SimpleFormatter());
		jlogger.addHandler(ch);

		// Configure file handler (FINE/DEBUG+)
		try {
			// Ensure logs directory may exist externally; FileHandler will create file if possible.
			java.util.logging.FileHandler fh = new java.util.logging.FileHandler("logs/app.log", true);
			fh.setLevel(java.util.logging.Level.FINE);
			fh.setFormatter(new java.util.logging.SimpleFormatter());
			jlogger.addHandler(fh);
		} catch (Exception e) {
			// If file handler cannot be created, log to console only.
			jlogger.warning("Could not create file handler for logging: " + e.toString());
		}

		// Set logger level to FINE so both INFO and FINE are processed (filters on handlers control visibility)
		jlogger.setLevel(java.util.logging.Level.FINE);
		jlogger.info("Logging initialized (java.util.logging).");
	}

	// Convenience logging methods
	private static void info(String fmt, Object... args) {
		jlogger.info(String.format(fmt, args));
	}
	private static void debug(String fmt, Object... args) {
		jlogger.fine(String.format(fmt, args));
	}
	private static void warn(String fmt, Object... args) {
		jlogger.warning(String.format(fmt, args));
	}
	private static void error(String fmt, Object... args) {
		jlogger.severe(String.format(fmt, args));
	}

	// --- Account and Bank as in previous lab (deadlock-free transfer) ---
	static class Account {
		final int id;
		private int balance;
		final ReentrantLock lock = new ReentrantLock();

		Account(int id, int initial) {
			this.id = id;
			this.balance = initial;
		}

		int getBalance() { return balance; }

		// caller must hold lock
		boolean withdrawIfPossible(int amount) {
			if (amount <= 0) return false;
			if (balance >= amount) {
				balance -= amount;
				return true;
			}
			return false;
		}

		// caller must hold lock
		void deposit(int amount) {
			if (amount <= 0) return;
			balance += amount;
		}
	}

	static class Bank {
		void transfer(Account a, Account b, int amount) {
			if (a == b || amount <= 0) return;
			Account first = a.id < b.id ? a : b;
			Account second = a.id < b.id ? b : a;

			first.lock.lock();
			try {
				second.lock.lock();
				try {
					if (a.withdrawIfPossible(amount)) {
						b.deposit(amount);
					}
				} finally {
					second.lock.unlock();
				}
			} finally {
				first.lock.unlock();
			}
		}
	}

	// --- Ring buffer for producer-consumer ---
	static class RingBuffer {
		private final String[] items;
		private int head = 0, tail = 0, count = 0;
		private final ReentrantLock lock = new ReentrantLock();
		private final Condition notEmpty = lock.newCondition();
		private final Condition notFull = lock.newCondition();

		RingBuffer(int capacity) {
			if (capacity <= 0) throw new IllegalArgumentException("capacity>0");
			items = new String[capacity];
		}

		void put(String s) throws InterruptedException {
			lock.lock();
			try {
				while (count == items.length) {
					notFull.await();
				}
				items[tail] = s;
				tail = (tail + 1) % items.length;
				count++;
				notEmpty.signal();
				debug("Buffer put (size=%d): %s", count, s);
			} finally {
				lock.unlock();
			}
		}

		String take() throws InterruptedException {
			lock.lock();
			try {
				while (count == 0) {
					notEmpty.await();
				}
				String s = items[head];
				items[head] = null;
				head = (head + 1) % items.length;
				count--;
				notFull.signal();
				debug("Buffer take (size=%d): %s", count, s);
				return s;
			} finally {
				lock.unlock();
			}
		}
	}

	// --- Reflection demo (Task 1) ---
	private static void runReflectionDemo(Scanner sc, ResourceBundle rb) {
		System.out.println(rb.getString("prompt.enterString"));
		String typed = sc.nextLine();
		String literal = "OriginalLiteral";

		System.out.println(MessageFormat.format(rb.getString("reflection.before"), literal));
		System.out.println(MessageFormat.format(rb.getString("reflection.before"), typed));

		System.out.println(rb.getString("prompt.enterReplacement"));
		String replacement = sc.nextLine();
		if (replacement == null) replacement = "";

		try {
			Field valueField = null;
			try {
				valueField = String.class.getDeclaredField("value");
				valueField.setAccessible(true);
			} catch (NoSuchFieldException e) {
				error("Field 'value' not found: %s", e.toString());
			}
			if (valueField != null) {
				// modify both strings
				modifyStringInternal(literal, valueField, replacement);
				modifyStringInternal(typed, valueField, replacement);
			}
		} catch (Throwable ex) {
			error("Reflection failure: %s", ex.toString());
			System.out.println(MessageFormat.format(rb.getString("reflection.error"), ex.toString()));
		}

		System.out.println(MessageFormat.format(rb.getString("reflection.after"), literal));
		System.out.println(MessageFormat.format(rb.getString("reflection.after"), typed));
	}

	private static void modifyStringInternal(String target, Field valueField, String replacement) throws IllegalAccessException {
		Object internal = valueField.get(target);
		if (internal instanceof char[]) {
			char[] arr = (char[]) internal;
			char[] repl = replacement.toCharArray();
			int len = Math.min(arr.length, repl.length);
			for (int i = 0; i < len; i++) arr[i] = repl[i];
			for (int i = repl.length; i < arr.length; i++) arr[i] = '\0';
			debug("Replaced char[] in String");
		} else if (internal instanceof byte[]) {
			byte[] arr = (byte[]) internal;
			byte[] repl = replacement.getBytes(StandardCharsets.ISO_8859_1);
			int len = Math.min(arr.length, repl.length);
			System.arraycopy(repl, 0, arr, 0, len);
			if (repl.length < arr.length) Arrays.fill(arr, repl.length, arr.length, (byte)0);
			debug("Replaced byte[] in String (compact string mode)");
		} else {
			warn("Unknown String internal representation: %s", internal == null ? "null" : internal.getClass().getName());
		}
	}

	// --- Bank test (Task 2 logging demonstration) ---
	private static void runBankTest(ResourceBundle rb) throws InterruptedException {
		final int ACCS = 100;
		final int MAX_INIT = 1000;
		final Random rnd = new Random();
		Account[] ac = new Account[ACCS];
		for (int i = 0; i < ACCS; i++) ac[i] = new Account(i, rnd.nextInt(MAX_INIT));
		long initialSum = 0;
		for (Account a : ac) initialSum += a.getBalance();
		info("Initial total: %d", initialSum);
		System.out.println(rb.getString("bank.initial") + " " + initialSum);

		Bank bank = new Bank();
		int TASKS = 20000;
		ExecutorService ex = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		for (int i = 0; i < TASKS; i++) {
			ex.submit(() -> {
				int from = rnd.nextInt(ACCS);
				int to = rnd.nextInt(ACCS);
				if (from == to) return;
				int amount = rnd.nextInt(200);
				debug("Attempt transfer %d -> %d amount %d", from, to, amount);
				bank.transfer(ac[from], ac[to], amount);
			});
		}
		ex.shutdown();
		ex.awaitTermination(1, TimeUnit.MINUTES);

		long finalSum = 0;
		for (Account a : ac) finalSum += a.getBalance();
		info("Final total: %d", finalSum);
		System.out.println(rb.getString("bank.final") + " " + finalSum);
		System.out.println(rb.getString("bank.result") + " " + (initialSum == finalSum ? rb.getString("bank.passed") : rb.getString("bank.failed")));
	}

	// --- Producer-consumer demo (Task 2) ---
	private static void runProducerConsumer(ResourceBundle rb) throws InterruptedException {
		final RingBuffer buf1 = new RingBuffer(50);
		final RingBuffer buf2 = new RingBuffer(50);
		final int PRODUCERS = 5;
		final int TRANSFERS = 2;
		final Random rnd = new Random();

		for (int i = 0; i < PRODUCERS; i++) {
			final int idx = i;
			Thread t = new Thread(() -> {
				int msg = 0;
				try {
					while (true) {
						String s = MessageFormat.format(rb.getString("producer.message"), idx, msg++);
						buf1.put(s);
						debug("Producer %d produced: %s", idx, s);
						Thread.sleep(rnd.nextInt(10));
					}
				} catch (InterruptedException ignored) {
				}
			}, "producer-" + i);
			t.setDaemon(true);
			t.start();
		}

		for (int i = 0; i < TRANSFERS; i++) {
			final int idx = i;
			Thread t = new Thread(() -> {
				try {
					while (true) {
						String s = buf1.take();
						String out = MessageFormat.format(rb.getString("transfer.message"), idx, s);
						buf2.put(out);
						debug("Transfer %d forwarded: %s", idx, out);
					}
				} catch (InterruptedException ignored) {
				}
			}, "transfer-" + i);
			t.setDaemon(true);
			t.start();
		}

		for (int i = 0; i < 100; i++) {
			String m = buf2.take();
			info("MAIN read: %s", m);
			System.out.println(MessageFormat.format(rb.getString("main.read"), m));
		}
	}

	// --- ResourceBundles in-code (ListResourceBundle) to avoid external files ---
	private static ResourceBundle getBundleForLocale(Locale locale) {
		if (locale != null && "uk".equals(locale.getLanguage())) {
			return new ListResourceBundle() {
				protected Object[][] getContents() {
					return new Object[][] {
						{"menu.title", "=== Меню Лабораторна 9 ==="},
						{"menu.option.reflection", "1. Демонстрація рефлексії (зміна внутрішнього представлення String)"},
						{"menu.option.bank", "2. Запустити тест переказів банку (з логуванням)"},
						{"menu.option.producer", "3. Демо виробник-споживач (з логуванням)"},
						{"menu.option.exit", "4. Вихід (або Q)"},
						{"prompt.enterString", "Введіть рядок (з клавіатури):"},
						{"prompt.enterReplacement", "Введіть текст для заміни:"},
						{"reflection.before", "До: {0}"},
						{"reflection.after", "Після: {0}"},
						{"reflection.error", "Помилка рефлексії: {0}"},
						{"bank.initial", "Початковий загальний баланс:"},
						{"bank.final", "Кінцевий загальний баланс:"},
						{"bank.result", "Результат тесту банку:"},
						{"bank.passed", "PASSED"},
						{"bank.failed", "FAILED"},
						{"producer.message", "Потік No {0} згенерував повідомлення {1}"},
						{"transfer.message", "Потік No {0} переклав повідомлення {1}"},
						{"main.read", "MAIN read: {0}"}
					};
				}
			};
		} else {
			// default English
			return new ListResourceBundle() {
				protected Object[][] getContents() {
					return new Object[][] {
						{"menu.title", "=== Lab9 Menu ==="},
						{"menu.option.reflection", "1. Reflection demo (change String internals)"},
						{"menu.option.bank", "2. Run bank transfers test (with logging)"},
						{"menu.option.producer", "3. Run producer-consumer demo (with logging)"},
						{"menu.option.exit", "4. Exit (or Q)"},
						{"prompt.enterString", "Enter a string (keyboard input):"},
						{"prompt.enterReplacement", "Enter replacement text:"},
						{"reflection.before", "Before: {0}"},
						{"reflection.after", "After: {0}"},
						{"reflection.error", "Reflection error: {0}"},
						{"bank.initial", "Initial total:"},
						{"bank.final", "Final total:"},
						{"bank.result", "Bank test"},
						{"bank.passed", "PASSED"},
						{"bank.failed", "FAILED"},
						{"producer.message", "Producer No {0} generated message {1}"},
						{"transfer.message", "Transfer No {0} translated message {1}"},
						{"main.read", "MAIN read: {0}"}
					};
				}
			};
		}
	}

	// --- Main menu and program entry ---
	public static void main(String[] args) throws InterruptedException {
		Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8.name());

		System.out.println("Select language / Виберіть мову:");
		System.out.println("1) English");
		System.out.println("2) Українська");
		String langChoice = sc.nextLine();
		Locale locale = "2".equals(langChoice) ? new Locale("uk") : Locale.ENGLISH;
		ResourceBundle rb = getBundleForLocale(locale);

		while (true) {
			System.out.println();
			System.out.println(rb.getString("menu.title"));
			System.out.println(rb.getString("menu.option.reflection"));
			System.out.println(rb.getString("menu.option.bank"));
			System.out.println(rb.getString("menu.option.producer"));
			System.out.println(rb.getString("menu.option.exit"));
			System.out.print("> ");
			String cmd = sc.nextLine();
			if ("1".equals(cmd)) {
				runReflectionDemo(sc, rb);
			} else if ("2".equals(cmd)) {
				System.out.println(rb.getString("menu.option.bank"));
				runBankTest(rb);
			} else if ("3".equals(cmd)) {
				System.out.println(rb.getString("menu.option.producer"));
				runProducerConsumer(rb);
			} else if ("4".equals(cmd) || "q".equalsIgnoreCase(cmd)) {
				System.out.println("Goodbye.");
				break;
			} else {
				System.out.println("Unknown option.");
			}
		}
		sc.close();
	}
}
