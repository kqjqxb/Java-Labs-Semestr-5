import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class lab9 {
	// Account with explicit lock and id
	static class Account {
		final int id;
		private int balance;
		final ReentrantLock lock = new ReentrantLock();

		Account(int id, int initial) {
			this.id = id;
			this.balance = initial;
		}

		int getBalance() {
			return balance;
		}

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

	// Bank with deadlock-free transfer (lock ordering by account id)
	static class Bank {
		void transfer(Account a, Account b, int amount) {
			if (a == b || amount <= 0) return;
			Account first = a.id < b.id ? a : b;
			Account second = a.id < b.id ? b : a;

			first.lock.lock();
			try {
				second.lock.lock();
				try {
					// under both locks: check and perform
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

	// Thread-safe circular buffer for Strings
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
				return s;
			} finally {
				lock.unlock();
			}
		}
	}

	// Task 1: bank test
	static void runBankTest() throws InterruptedException {
		final int ACCS = 100;
		final int MAX_INIT = 1000;
		final Random rnd = new Random();
		Account[] ac = new Account[ACCS];
		for (int i = 0; i < ACCS; i++) ac[i] = new Account(i, rnd.nextInt(MAX_INIT));
		long initialSum = 0;
		for (Account a : ac) initialSum += a.getBalance();
		System.out.println("Initial total: " + initialSum);

		Bank bank = new Bank();
		int TASKS = 20000;
		ExecutorService ex = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		for (int i = 0; i < TASKS; i++) {
			ex.submit(() -> {
				int from = rnd.nextInt(ACCS);
				int to = rnd.nextInt(ACCS);
				if (from == to) return;
				int amount = rnd.nextInt(200); // up to 199
				bank.transfer(ac[from], ac[to], amount);
			});
		}
		ex.shutdown();
		ex.awaitTermination(1, TimeUnit.MINUTES);

		long finalSum = 0;
		for (Account a : ac) finalSum += a.getBalance();
		System.out.println("Final total: " + finalSum);
		System.out.println("Bank test " + (initialSum == finalSum ? "PASSED" : "FAILED"));
	}

	// Task 2: producer-consumer with two ring buffers
	static void runProducerConsumer() throws InterruptedException {
		final RingBuffer buf1 = new RingBuffer(50);
		final RingBuffer buf2 = new RingBuffer(50);
		final int PRODUCERS = 5;
		final int TRANSFERS = 2;
		final Random rnd = new Random();

		// producers (daemon)
		for (int i = 0; i < PRODUCERS; i++) {
			final int idx = i;
			Thread t = new Thread(() -> {
				int msg = 0;
				try {
					while (true) {
						String s = "Потік No " + idx + " згенерував повідомлення " + (msg++);
						buf1.put(s);
						// small pause
						Thread.sleep(rnd.nextInt(10));
					}
				} catch (InterruptedException ignored) {
				}
			}, "producer-" + i);
			t.setDaemon(true);
			t.start();
		}

		// transfer threads: take from buf1, write to buf2 after formatting
		for (int i = 0; i < TRANSFERS; i++) {
			final int idx = i;
			Thread t = new Thread(() -> {
				try {
					while (true) {
						String s = buf1.take();
						String out = "Потік No " + idx + " переклав повідомлення " + s;
						buf2.put(out);
					}
				} catch (InterruptedException ignored) {
				}
			}, "transfer-" + i);
			t.setDaemon(true);
			t.start();
		}

		// main thread reads 100 messages from buf2 and prints
		for (int i = 0; i < 100; i++) {
			String m = buf2.take();
			System.out.println("MAIN read: " + m);
		}
		// program will exit; daemon threads will be stopped
	}

	public static void main(String[] args) throws InterruptedException {
		// Run Task 1
		System.out.println("=== Task 1: Bank transfers test ===");
		runBankTest();

		// Run Task 2
		System.out.println("\n=== Task 2: Producer-Consumer with ring buffers ===");
		runProducerConsumer();
	}
}
