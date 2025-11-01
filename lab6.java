// lab6 by Maksym Lomakin
import java.util.*;

public class lab6 {
	// implementation of a red-black tree for int keys with insertion, deletion, and output.
	static class RBTree {
		private static final boolean RED = true;
		private static final boolean BLACK = false;

		class Node {
			int key;
			boolean color;
			Node left, right, parent;
			Node(int key, boolean color, Node nil) {
				this.key = key;
				this.color = color;
				this.left = nil;
				this.right = nil;
				this.parent = nil;
			}
		}

		private final Node NIL = new Node(0, BLACK, null);
		private Node root = NIL;

		public RBTree() {
			NIL.left = NIL.right = NIL.parent = NIL;
			root = NIL;
		}

		// left rotate x
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

		// right rotate x
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

		public void insert(int key) {
			Node z = new Node(key, RED, NIL);
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
			z.left = NIL; z.right = NIL; z.color = RED;
			insertFixup(z);
		}

		private void insertFixup(Node z) {
			while (z.parent.color == RED) {
				if (z.parent == z.parent.parent.left) {
					Node y = z.parent.parent.right;
					if (y.color == RED) {
						z.parent.color = BLACK;
						y.color = BLACK;
						z.parent.parent.color = RED;
						z = z.parent.parent;
					} else {
						if (z == z.parent.right) {
							z = z.parent;
							leftRotate(z);
						}
						z.parent.color = BLACK;
						z.parent.parent.color = RED;
						rightRotate(z.parent.parent);
					}
				} else {
					Node y = z.parent.parent.left;
					if (y.color == RED) {
						z.parent.color = BLACK;
						y.color = BLACK;
						z.parent.parent.color = RED;
						z = z.parent.parent;
					} else {
						if (z == z.parent.left) {
							z = z.parent;
							rightRotate(z);
						}
						z.parent.color = BLACK;
						z.parent.parent.color = RED;
						leftRotate(z.parent.parent);
					}
				}
			}
			root.color = BLACK;
		}

		// transplant u with v
		private void transplant(Node u, Node v) {
			if (u.parent == NIL) root = v;
			else if (u == u.parent.left) u.parent.left = v;
			else u.parent.right = v;
			v.parent = u.parent;
		}

		private Node minimum(Node x) {
			while (x.left != NIL) x = x.left;
			return x;
		}

		public boolean delete(int key) {
			Node z = root;
			while (z != NIL && z.key != key) {
				if (key < z.key) z = z.left;
				else z = z.right;
			}
			if (z == NIL) return false;
			Node y = z;
			boolean yOriginalColor = y.color;
			Node x;
			if (z.left == NIL) {
				x = z.right;
				transplant(z, z.right);
			} else if (z.right == NIL) {
				x = z.left;
				transplant(z, z.left);
			} else {
				y = minimum(z.right);
				yOriginalColor = y.color;
				x = y.right;
				if (y.parent == z) x.parent = y;
				else {
					transplant(y, y.right);
					y.right = z.right;
					y.right.parent = y;
				}
				transplant(z, y);
				y.left = z.left;
				y.left.parent = y;
				y.color = z.color;
			}
			if (yOriginalColor == BLACK) deleteFixup(x);
			return true;
		}

		private void deleteFixup(Node x) {
			while (x != root && x.color == BLACK) {
				if (x == x.parent.left) {
					Node w = x.parent.right;
					if (w.color == RED) {
						w.color = BLACK;
						x.parent.color = RED;
						leftRotate(x.parent);
						w = x.parent.right;
					}
					if (w.left.color == BLACK && w.right.color == BLACK) {
						w.color = RED;
						x = x.parent;
					} else {
						if (w.right.color == BLACK) {
							w.left.color = BLACK;
							w.color = RED;
							rightRotate(w);
							w = x.parent.right;
						}
						w.color = x.parent.color;
						x.parent.color = BLACK;
						w.right.color = BLACK;
						leftRotate(x.parent);
						x = root;
					}
				} else {
					Node w = x.parent.left;
					if (w.color == RED) {
						w.color = BLACK;
						x.parent.color = RED;
						rightRotate(x.parent);
						w = x.parent.left;
					}
					if (w.right.color == BLACK && w.left.color == BLACK) {
						w.color = RED;
						x = x.parent;
					} else {
						if (w.left.color == BLACK) {
							w.right.color = BLACK;
							w.color = RED;
							leftRotate(w);
							w = x.parent.left;
						}
						w.color = x.parent.color;
						x.parent.color = BLACK;
						w.left.color = BLACK;
						rightRotate(x.parent);
						x = root;
					}
				}
			}
			x.color = BLACK;
		}

		// traversals
		public List<Integer> inorder() {
			List<Integer> res = new ArrayList<>();
			inorderRec(root, res);
			return res;
		}
		private void inorderRec(Node n, List<Integer> r) {
			if (n == NIL) return;
			inorderRec(n.left, r);
			r.add(n.key);
			inorderRec(n.right, r);
		}

		public List<Integer> preorder() {
			List<Integer> res = new ArrayList<>();
			preorderRec(root, res);
			return res;
		}
		private void preorderRec(Node n, List<Integer> r) {
			if (n == NIL) return;
			r.add(n.key);
			preorderRec(n.left, r);
			preorderRec(n.right, r);
		}

		public List<Integer> levelOrder() {
			List<Integer> res = new ArrayList<>();
			if (root == NIL) return res;
			Queue<Node> q = new LinkedList<>();
			q.add(root);
			while (!q.isEmpty()) {
				Node cur = q.poll();
				res.add(cur.key);
				if (cur.left != NIL) q.add(cur.left);
				if (cur.right != NIL) q.add(cur.right);
			}
			return res;
		}

		// pretty print (rotated: root at left, right subtree above)
		public void printTree() {
			printRec(root, "", true);
		}
		private void printRec(Node node, String indent, boolean last) {
			if (node == NIL) return;
			System.out.print(indent);
			if (last) {
				System.out.print("R----");
				indent += "     ";
			} else {
				System.out.print("L----");
				indent += "|    ";
			}
			System.out.println(node.key + "(" + (node.color==RED?"R":"B") + ")");
			printRec(node.right, indent, false);
			printRec(node.left, indent, true);
		}
	}

	// app console
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		RBTree tree = new RBTree();
		System.out.println("Red-Black Tree console app");
		while (true) {
			System.out.println("\nMenu:");
			System.out.println("1) Fill with random numbers");
			System.out.println("2) Fill with sorted numbers (ascending)");
			System.out.println("3) Enter numbers manually");
			System.out.println("4) Print traversals and tree");
			System.out.println("5) Delete a key");
			System.out.println("6) Clear tree");
			System.out.println("0) Exit");
			System.out.print("Choose option: ");
			String opt = sc.nextLine().trim();
			if (opt.equals("0")) break;
			switch (opt) {
				case "1": {
					System.out.print("How many numbers? ");
					int n = readInt(sc, 10);
					System.out.print("Max value (exclusive)? ");
					int max = readInt(sc, 100);
					int[] arr = new int[n];
					Random rnd = new Random();
					for (int i=0;i<n;i++) arr[i] = rnd.nextInt(Math.max(1, max));
					System.out.println("Insertion order: " + Arrays.toString(arr));
					for (int v: arr) tree.insert(v);
					System.out.println("Inserted " + n + " random numbers.");
					break;
				}
				case "2": {
					System.out.print("How many numbers? ");
					int n = readInt(sc, 10);
					int start = 0;
					int[] arr = new int[n];
					for (int i=0;i<n;i++) arr[i] = start + i;
					System.out.println("Insertion order (sorted): " + Arrays.toString(arr));
					for (int v: arr) tree.insert(v);
					System.out.println("Inserted sorted sequence.");
					break;
				}
				case "3": {
					System.out.println("Enter integers separated by spaces:");
					String line = sc.nextLine();
					String[] parts = line.trim().split("\\s+");
					List<Integer> vals = new ArrayList<>();
					for (String p: parts) {
						if (p.isEmpty()) continue;
						try { vals.add(Integer.parseInt(p)); } catch (NumberFormatException ex) {}
					}
					System.out.println("Insertion order: " + vals);
					for (int v: vals) tree.insert(v);
					break;
				}
				case "4": {
					System.out.println("In-order: " + tree.inorder());
					System.out.println("Pre-order: " + tree.preorder());
					System.out.println("Level-order: " + tree.levelOrder());
					System.out.println("Tree:");
					tree.printTree();
					break;
				}
				case "5": {
					System.out.print("Enter key to delete: ");
					String k = sc.nextLine().trim();
					try {
						int key = Integer.parseInt(k);
						boolean ok = tree.delete(key);
						System.out.println(ok ? "Deleted " + key : "Key not found: " + key);
					} catch (NumberFormatException ex) {
						System.out.println("Invalid integer.");
					}
					break;
				}
				case "6": {
					tree = new RBTree();
					System.out.println("Tree cleared.");
					break;
				}
				default:
					System.out.println("Unknown option.");
			}
		}
		System.out.println("Bye.");
		sc.close();
	}

	private static int readInt(Scanner sc, int def) {
		String s = sc.nextLine().trim();
		if (s.isEmpty()) return def;
		try { return Integer.parseInt(s); } catch (NumberFormatException ex) { return def; }
	}
}
