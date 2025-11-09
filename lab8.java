import java.util.concurrent.*;
import java.util.concurrent.ThreadLocalRandom;

public class lab8 {

	// порог, поки не ділимо далі
	private static final int THRESHOLD = 20;

	// рекурс. таск для ForkJoin
	private static class SumTask extends RecursiveTask<Long> {
		private final int[] arr;
		private final int lo;
		private final int hi;

		SumTask(int[] arr, int lo, int hi) {
			this.arr = arr;
			this.lo = lo;
			this.hi = hi;
		}

		@Override
		protected Long compute() {
			int len = hi - lo;
			if (len <= THRESHOLD) {
				long s = 0;
				for (int i = lo; i < hi; ++i) s += arr[i];
				return s;
			}
			int mid = lo + len / 2;
			SumTask left = new SumTask(arr, lo, mid);
			SumTask right = new SumTask(arr, mid, hi);
			left.fork();
			long rightSum = right.compute();
			long leftSum = left.join();
			return leftSum + rightSum;
		}
	}

	// запуск завдання у ForkJoinPool
	public static long forkJoinSum(int[] arr) {
		if (arr == null || arr.length == 0) return 0L;
		return ForkJoinPool.commonPool().invoke(new SumTask(arr, 0, arr.length));
	}

	// генерація масиву з n елементів значеннями 0..100
	public static int[] generateRandomArray(int n) {
		int[] a = new int[n];
		ThreadLocalRandom rnd = ThreadLocalRandom.current();
		for (int i = 0; i < n; ++i) a[i] = rnd.nextInt(0, 101);
		return a;
	}

	public static void main(String[] args) {
		final int N = 1_000_000;
		int[] big = generateRandomArray(N);

		long t0 = System.nanoTime();
		long fjSum = forkJoinSum(big);
		long t1 = System.nanoTime();

		long s0 = System.nanoTime();
		long seqSum = 0;
		for (int v : big) seqSum += v;
		long s1 = System.nanoTime();

		System.out.println("ForkJoin sum: " + fjSum + " (" + ((t1 - t0) / 1_000_000) + " ms)");
		System.out.println("Sequential sum: " + seqSum + " (" + ((s1 - s0) / 1_000_000) + " ms)");
		System.out.println("Sums equal: " + (fjSum == seqSum));
	}
}