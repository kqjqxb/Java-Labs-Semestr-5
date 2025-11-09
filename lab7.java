import java.util.*;

public class lab7 {
	enum Color { RED, BLACK }

	static class Node {
		int key;
		Color color;
		Node left, right, parent;
		Node(int key, Color color, Node nil) {
			this.key = key;
			this.color = color;
			this.left = nil;
			this.right = nil;
			this.parent = nil;
		}
		@Override
		public String toString() {
			return key + "(" + (color == Color.RED ? "R" : "B") + ")";
		}
	}

	static class RBTree {
		private final Node NIL;
		private Node root;

		RBTree() {
			NIL = new Node(0, Color.BLACK, null);
			NIL.left = NIL.right = NIL.parent = NIL;
			root = NIL;
		}

		public void insert(int key) {
			Node z = new Node(key, Color.RED, NIL);
			Node y = NIL;
			Node x = root;
			while (x != NIL) {
				y = x;
				if (z.key < x.key) x = x.left;
				else x = x.right;
			}
			z.parent = y;
			if (y == NIL) root = z;
			else if (z.key < y.key) y.left = z;
			else y.right = z;
			z.left = NIL;
			z.right = NIL;
			z.color = Color.RED;
			insertFixup(z);
		}

		private void insertFixup(Node z) {
			while (z.parent.color == Color.RED) {
				if (z.parent == z.parent.parent.left) {
					Node y = z.parent.parent.right;
					if (y.color == Color.RED) {
						z.parent.color = Color.BLACK;
						y.color = Color.BLACK;
						z.parent.parent.color = Color.RED;
						z = z.parent.parent;
					} else {
						if (z == z.parent.right) {
							z = z.parent;
							leftRotate(z);
						}
						z.parent.color = Color.BLACK;
						z.parent.parent.color = Color.RED;
						rightRotate(z.parent.parent);
					}
				} else {
					Node y = z.parent.parent.left;
					if (y.color == Color.RED) {
						z.parent.color = Color.BLACK;
						y.color = Color.BLACK;
						z.parent.parent.color = Color.RED;
						z = z.parent.parent;
					} else {
						if (z == z.parent.left) {
							z = z.parent;
							rightRotate(z);
						}
						z.parent.color = Color.BLACK;
						z.parent.parent.color = Color.RED;
						leftRotate(z.parent.parent);
					}
				}
			}
			root.color = Color.BLACK;
		}

		private void leftRotate(Node x) {
			Node y = x.right;
			x.right = y.left;
			if (y.left != NIL) y.left.parent = x;
			y.parent = x.parent;
			if (x.parent == NIL) root = y;
			else if (x == x.parent.left) x.parent.left = y;
			else x.parent.right = y;
			y.left = x;
			x.parent = y;
		}

		private void rightRotate(Node x) {
			Node y = x.left;
			x.left = y.right;
			if (y.right != NIL) y.right.parent = x;
			y.parent = x.parent;
			if (x.parent == NIL) root = y;
			else if (x == x.parent.right) x.parent.right = y;
			else x.parent.left = y;
			y.right = x;
			x.parent = y;
		}

		public void inorder() {
			inorder(root);
			System.out.println();
		}
		private void inorder(Node x) {
			if (x == NIL) return;
			inorder(x.left);
			System.out.print(x.key + " ");
			inorder(x.right);
		}

		public void preorder() {
			preorder(root);
			System.out.println();
		}
		private void preorder(Node x) {
			if (x == NIL) return;
			System.out.print(x.key + " ");
			preorder(x.left);
			preorder(x.right);
		}

		public void display() {
			if (root == NIL) {
				System.out.println("(empty tree)");
				return;
			}
			printSubtree(root, "", true);
		}

		private void printSubtree(Node node, String indent, boolean last) {
			if (node == NIL) return;
			System.out.print(indent);
			if (last) {
				System.out.print("└── ");
				indent += "    ";
			} else {
				System.out.print("├── ");
				indent += "│   ";
			}
			System.out.println(node.toString());
			boolean hasLeft = node.left != NIL;
			boolean hasRight = node.right != NIL;
			if (hasLeft || hasRight) {
				if (hasLeft) printSubtree(node.left, indent, false);
				if (hasRight) printSubtree(node.right, indent, true);
			}
		}
	}

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		System.out.println("Red-Black Tree demo");
		System.out.println("Виберіть режим додавання елементів:");
		System.out.println("1 - випадкові числа");
		System.out.println("2 - впорядковано (зростання)");
		System.out.println("3 - ручний ввід (введіть числа через пробіл)");

		int choice = 0;
		try {
			choice = Integer.parseInt(sc.nextLine().trim());
		} catch (Exception e) {
			System.out.println("Невірний ввід, використано режим 1.");
			choice = 1;
		}

		List<Integer> values = new ArrayList<>();
		Random rnd = new Random();

		if (choice == 1) {
			System.out.print("Скільки випадкових чисел згенерувати? ");
			int n = Integer.parseInt(sc.nextLine().trim());
			for (int i = 0; i < n; i++) values.add(rnd.nextInt(100));
			System.out.println("Порядок додавання: " + values);
		} else if (choice == 2) {
			System.out.print("Скільки чисел згенерувати і впорядкувати? ");
			int n = Integer.parseInt(sc.nextLine().trim());
			for (int i = 0; i < n; i++) values.add(rnd.nextInt(100));
			Collections.sort(values);
			System.out.println("Порядок (зростання): " + values);
		} else {
			System.out.println("Введіть числа через пробіл:");
			String line = sc.nextLine().trim();
			if (!line.isEmpty()) {
				for (String s : line.split("\\s+")) {
					try { values.add(Integer.parseInt(s)); } catch (Exception ignored) {}
				}
			}
			System.out.println("Порядок додавання: " + values);
		}

		RBTree tree = new RBTree();
		for (int v : values) tree.insert(v);

		System.out.println("\nОбходи дерева:");
		System.out.print("In-order: ");
		tree.inorder();
		System.out.print("Pre-order: ");
		tree.preorder();

		System.out.println("\nВізуалізація дерева:");
		tree.display();

		System.out.println("\nБажаєте видалити значення? (y/n)");
		String ans = sc.nextLine().trim().toLowerCase();
		if (ans.equals("y") || ans.equals("так") || ans.equals("t")) {
			System.out.println("Введіть числа для видалення через пробіл (всі входження):");
			String line = sc.nextLine().trim();
			Set<Integer> toRemove = new HashSet<>();
			if (!line.isEmpty()) {
				for (String s : line.split("\\s+")) {
					try { toRemove.add(Integer.parseInt(s)); } catch (Exception ignored) {}
				}
			}
			if (!toRemove.isEmpty()) {
				// Варіативний підхід до видалення: перебудова дерева з елементів, що залишились
				List<Integer> survivors = new ArrayList<>();
				for (int v : values) if (!toRemove.contains(v)) survivors.add(v);
				RBTree newTree = new RBTree();
				for (int v : survivors) newTree.insert(v);
				System.out.println("\nПісля видалення:");
				System.out.print("In-order: ");
				newTree.inorder();
				System.out.println("\nВізуалізація дерева:");
				newTree.display();
			} else {
				System.out.println("Немає дійсних значень для видалення.");
			}
		}

		System.out.println("\nГотово.");
		sc.close();
	}
}
