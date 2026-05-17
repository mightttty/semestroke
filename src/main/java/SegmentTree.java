import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Динамическое дерево отрезков с поддержкой добавления в конец,
 * удаления по логическому индексу и поиска значения по индексу.
 * Все операции работают за O(log capacity), где capacity – максимальный размер.
 */
class SegmentTree {
    private int[] treeValues;   // хранит суммы (или просто значения) на отрезках
    private int[] treeCounts;   // хранит количество "живых" элементов на отрезке
    private int capacity;       // максимальная физическая ёмкость (константа)
    private int currentPhysicalSize; // сколько физических ячеек уже занято (для add)
    private int operations;     // счётчик операций для последнего вызова

    /**
     * @param maxCapacity максимальное количество элементов, которое когда-либо будет в дереве
     *                    (начальные + все добавляемые)
     */
    public SegmentTree(int maxCapacity) {
        this.capacity = maxCapacity;
        this.treeValues = new int[4 * maxCapacity];
        this.treeCounts = new int[4 * maxCapacity];
        this.currentPhysicalSize = 0;
        this.operations = 0;
    }

    // Рекурсивное точечное обновление: установить физическую ячейку physIndex в значение value
    // и пометить её как aliveCount (1 — жива, 0 — удалена)
    private void updateSingle(int node, int l, int r, int physIndex, int value, int aliveCount) {
        operations++;
        if (l == r) {
            treeValues[node] = value;
            treeCounts[node] = aliveCount;
            return;
        }
        int mid = (l + r) / 2;
        if (physIndex <= mid) {
            updateSingle(node * 2, l, mid, physIndex, value, aliveCount);
        } else {
            updateSingle(node * 2 + 1, mid + 1, r, physIndex, value, aliveCount);
        }
        treeValues[node] = treeValues[node * 2] + treeValues[node * 2 + 1];
        treeCounts[node] = treeCounts[node * 2] + treeCounts[node * 2 + 1];
    }

    /**
     * Добавляет элемент в конец последовательности.
     * @param value добавляемое целое число
     */
    public void add(int value) {
        if (currentPhysicalSize >= capacity) {
            throw new RuntimeException("Превышена максимальная вместимость дерева. Увеличьте maxCapacity.");
        }
        operations = 0;
        updateSingle(1, 0, capacity - 1, currentPhysicalSize, value, 1);
        currentPhysicalSize++;
    }

    // Рекурсивный поиск k-го (от 0) живого элемента: возвращает его ЗНАЧЕНИЕ
    private int findKthValue(int node, int l, int r, int k) {
        operations++;
        if (l == r) {return treeValues[node];}
        int mid = (l + r) / 2;
        int leftAlive = treeCounts[node * 2];
        if (k < leftAlive) {return findKthValue(node * 2, l, mid, k);}
        else {return findKthValue(node * 2 + 1, mid + 1, r, k - leftAlive);}}

    /**
     * Возвращает значение элемента по его логическому индексу (порядковому номеру, 0‑based).
     * @param index логический индекс
     * @return значение или -1, если индекс некорректен
     */
    public int find(int index) {
        if (index < 0 || index >= getSize()) {
            return -1;
        }
        operations = 0;
        return findKthValue(1, 0, capacity - 1, index);
    }

    // Рекурсивный поиск ФИЗИЧЕСКОГО индекса для k-го живого элемента
    private int findPhysicalIndex(int node, int l, int r, int k) {
        operations++;
        if (l == r) {
            return l;
        }
        int mid = (l + r) / 2;
        int leftAlive = treeCounts[node * 2];
        if (k < leftAlive) {
            return findPhysicalIndex(node * 2, l, mid, k);
        } else {
            return findPhysicalIndex(node * 2 + 1, mid + 1, r, k - leftAlive);
        }
    }

    /**
     * Удаляет элемент по его логическому индексу.
     * @param index логический (0‑based)
     */
    public void remove(int index) {
        if (index < 0 || index >= getSize()) {
            return;
        }
        operations = 0;
        int physIndex = findPhysicalIndex(1, 0, capacity - 1, index);
        updateSingle(1, 0, capacity - 1, physIndex, 0, 0);
    }

    /**
     * Возвращает количество операций, выполненных при последнем вызове
     * add / find / remove.
     */
    public int getOperations() {
        return operations;
    }

    /**
     * Текущее количество "живых" элементов в дереве.
     */
    public int getSize() {
        return treeCounts[1];
    }


    // Сумма первых k живых элементов (k от 0 до getSize())
    public int prefixSum(int k) {
        if (k <= 0) return 0;
        if (k > getSize()) k = getSize();
        operations = 0;
        return prefixSum(1, 0, capacity - 1, k);
    }

    private int prefixSum(int node, int left, int right, int k) {
        operations++;
        if (k == 0) return 0;
        if (left == right) {
            // лист – возвращаем его значение (0 или значение "живого" элемента)
            return treeValues[node];
        }
        int leftCount = treeCounts[node * 2];
        int mid = (left + right) / 2;
        if (k <= leftCount) {
            return prefixSum(node * 2, left, mid, k);
        } else {
            int leftSum = treeValues[node * 2]; // сумма всех "живых" в левом поддереве
            return leftSum + prefixSum(node * 2 + 1, mid + 1, right, k - leftCount);
        }
    }

    // Сумма "живых" элементов с логическими индексами от l до r (включительно)
    public int query(int l, int r) {
        if (l > r || l < 0 || r >= getSize()) return 0;
        return prefixSum(r + 1) - prefixSum(l);
    }
}
