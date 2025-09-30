// lab2Hard.java by Maksym Lomakin
// 1. MyList interface
interface MyList {
    void add(Object e);
    void add(int index, Object element);
    void addAll(Object[] c);
    void addAll(int index, Object[] c);
    Object get(int index);
    Object remove(int index);
    void set(int index, Object element);
    int indexOf(Object o);
    int size();
    Object[] toArray();
}

// Marker interface for random access
interface RandomAccess {}

// 2. MyArrayList implementation
class MyArrayList implements MyList, RandomAccess {
    private Object[] data;
    private int size;

    public MyArrayList() {
        data = new Object[10];
        size = 0;
    }

    public void add(Object e) {
        ensureCapacity(size + 1);
        data[size++] = e;
    }

    public void add(int index, Object element) {
        checkIndexForAdd(index);
        ensureCapacity(size + 1);
        System.arraycopy(data, index, data, index + 1, size - index);
        data[index] = element;
        size++;
    }

    public void addAll(Object[] c) {
        ensureCapacity(size + c.length);
        System.arraycopy(c, 0, data, size, c.length);
        size += c.length;
    }

    public void addAll(int index, Object[] c) {
        checkIndexForAdd(index);
        ensureCapacity(size + c.length);
        System.arraycopy(data, index, data, index + c.length, size - index);
        System.arraycopy(c, 0, data, index, c.length);
        size += c.length;
    }

    public Object get(int index) {
        checkIndex(index);
        return data[index];
    }

    public Object remove(int index) {
        checkIndex(index);
        Object removed = data[index];
        System.arraycopy(data, index + 1, data, index, size - index - 1);
        data[--size] = null;
        return removed;
    }

    public void set(int index, Object element) {
        checkIndex(index);
        data[index] = element;
    }

    public int indexOf(Object o) {
        for (int i = 0; i < size; i++) {
            if (data[i].equals(o)) return i;
        }
        return -1;
    }

    public int size() {
        return size;
    }

    public Object[] toArray() {
        Object[] arr = new Object[size];
        System.arraycopy(data, 0, arr, 0, size);
        return arr;
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity > data.length) {
            int newCapacity = Math.max(data.length * 2, minCapacity);
            Object[] newData = new Object[newCapacity];
            System.arraycopy(data, 0, newData, 0, size);
            data = newData;
        }
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException();
    }

    private void checkIndexForAdd(int index) {
        if (index < 0 || index > size) throw new IndexOutOfBoundsException();
    }
}

// 2. MyLinkedList implementation
class MyLinkedList implements MyList {
    private static class Node {
        Object value;
        Node next;
        Node prev;
        Node(Object value) { this.value = value; }
    }

    private Node head, tail;
    private int size;

    public void add(Object e) {
        Node node = new Node(e);
        if (tail == null) {
            head = tail = node;
        } else {
            tail.next = node;
            node.prev = tail;
            tail = node;
        }
        size++;
    }

    public void add(int index, Object element) {
        checkIndexForAdd(index);
        if (index == size) {
            add(element);
            return;
        }
        Node curr = nodeAt(index);
        Node node = new Node(element);
        node.next = curr;
        node.prev = curr.prev;
        if (curr.prev != null) curr.prev.next = node;
        else head = node;
        curr.prev = node;
        size++;
    }

    public void addAll(Object[] c) {
        for (Object o : c) add(o);
    }

    public void addAll(int index, Object[] c) {
        checkIndexForAdd(index);
        for (int i = 0; i < c.length; i++) {
            add(index + i, c[i]);
        }
    }

    public Object get(int index) {
        checkIndex(index);
        return nodeAt(index).value;
    }

    public Object remove(int index) {
        checkIndex(index);
        Node curr = nodeAt(index);
        if (curr.prev != null) curr.prev.next = curr.next;
        else head = curr.next;
        if (curr.next != null) curr.next.prev = curr.prev;
        else tail = curr.prev;
        size--;
        return curr.value;
    }

    public void set(int index, Object element) {
        checkIndex(index);
        nodeAt(index).value = element;
    }

    public int indexOf(Object o) {
        Node curr = head;
        int idx = 0;
        while (curr != null) {
            if (curr.value.equals(o)) return idx;
            curr = curr.next;
            idx++;
        }
        return -1;
    }

    public int size() {
        return size;
    }

    public Object[] toArray() {
        Object[] arr = new Object[size];
        Node curr = head;
        int idx = 0;
        while (curr != null) {
            arr[idx++] = curr.value;
            curr = curr.next;
        }
        return arr;
    }

    private Node nodeAt(int index) {
        Node curr = head;
        for (int i = 0; i < index; i++) curr = curr.next;
        return curr;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException();
    }

    private void checkIndexForAdd(int index) {
        if (index < 0 || index > size) throw new IndexOutOfBoundsException();
    }
}

// 3. LinkedHashSet implementation
class LinkedHashSet {
    private static class Node {
        Object value;
        Node next;
        Node(Object value) { this.value = value; }
    }
    private Node head, tail;
    private java.util.HashSet<Object> set = new java.util.HashSet<>();

    public boolean add(Object e) {
        if (set.contains(e)) return false;
        Node node = new Node(e);
        if (tail == null) head = tail = node;
        else {
            tail.next = node;
            tail = node;
        }
        set.add(e);
        return true;
    }

    public boolean contains(Object e) {
        return set.contains(e);
    }

    public boolean remove(Object e) {
        if (!set.contains(e)) return false;
        Node prev = null, curr = head;
        while (curr != null) {
            if (curr.value.equals(e)) {
                if (prev != null) prev.next = curr.next;
                else head = curr.next;
                if (curr == tail) tail = prev;
                set.remove(e);
                return true;
            }
            prev = curr;
            curr = curr.next;
        }
        return false;
    }

    public int size() {
        return set.size();
    }

    public Object[] toArray() {
        Object[] arr = new Object[size()];
        Node curr = head;
        int idx = 0;
        while (curr != null) {
            arr[idx++] = curr.value;
            curr = curr.next;
        }
        return arr;
    }
}

// 4. Simple Cache implementation
class Cache<K, V> {
    private static class Entry<K, V> {
        K key;
        V value;
        long expiryTime;
        Entry(K key, V value, long expiryTime) {
            this.key = key;
            this.value = value;
            this.expiryTime = expiryTime;
        }
    }

    private java.util.LinkedHashMap<K, Entry<K, V>> map;
    private int maxSize;
    private long defaultExpiryMillis;

    public Cache(int maxSize, long defaultExpiryMillis) {
        this.maxSize = maxSize;
        this.defaultExpiryMillis = defaultExpiryMillis;
        map = new java.util.LinkedHashMap<K, Entry<K, V>>(16, 0.75f, true) {
            protected boolean removeEldestEntry(java.util.Map.Entry<K, Entry<K, V>> eldest) {
                return size() > Cache.this.maxSize;
            }
        };
    }

    public void put(K key, V value) {
        if (key == null || value == null) throw new NullPointerException();
        long expiry = System.currentTimeMillis() + defaultExpiryMillis;
        map.put(key, new Entry<>(key, value, expiry));
    }

    public V get(K key) {
        if (key == null) throw new NullPointerException();
        Entry<K, V> entry = map.get(key);
        if (entry == null) return null;
        if (System.currentTimeMillis() > entry.expiryTime) {
            map.remove(key);
            return null;
        }
        return entry.value;
    }

    public boolean containsKey(K key) {
        if (key == null) throw new NullPointerException();
        Entry<K, V> entry = map.get(key);
        if (entry == null) return false;
        if (System.currentTimeMillis() > entry.expiryTime) {
            map.remove(key);
            return false;
        }
        return true;
    }

    public int size() {
        return map.size();
    }

    public void remove(K key) {
        if (key == null) throw new NullPointerException();
        map.remove(key);
    }
}

class TestLab2Hard {
    public static void main(String[] args) {
        // MyArrayList
        MyArrayList arrList = new MyArrayList();
        arrList.add("A");
        arrList.add("B");
        arrList.add(1, "C");
        arrList.set(0, "D");
        System.out.println("MyArrayList: " + java.util.Arrays.toString(arrList.toArray()));

        // MyLinkedList
        MyLinkedList linkedList = new MyLinkedList();
        linkedList.add("X");
        linkedList.add("Y");
        linkedList.add(1, "Z");
        linkedList.remove(0);
        System.out.println("MyLinkedList: " + java.util.Arrays.toString(linkedList.toArray()));

        // LinkedHashSet
        LinkedHashSet set = new LinkedHashSet();
        set.add("one");
        set.add("two");
        set.add("one"); // duplicate
        set.remove("two");
        System.out.println("LinkedHashSet: " + java.util.Arrays.toString(set.toArray()));

        // Cache
        Cache<String, String> cache = new Cache<>(2, 1000); // max 2 items, 1s expiry
        cache.put("k1", "v1");
        cache.put("k2", "v2");
        System.out.println("Cache k1: " + cache.get("k1"));
        try { Thread.sleep(1100); } catch (InterruptedException e) {}
        System.out.println("Cache k1 after expiry: " + cache.get("k1"));
        cache.put("k3", "v3"); // triggers eviction
        System.out.println("Cache size after eviction: " + cache.size());
    }
}