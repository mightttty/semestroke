class ClassicSegTree {
    private int[] tree;   // массив для хранения дерева (размер 4*n)
    private int n;        // размер исходного массива

    // Конструктор: принимает исходный массив
    public ClassicSegTree(int[] arr) {
        n = arr.length;
        tree = new int[4 * n];
        build(arr, 1, 0, n - 1);
    }

    // Построение дерева (рекурсивное)
    private void build(int[] arr, int node, int left, int right) {
        if (left == right) {
            tree[node] = arr[left];  // лист – значение элемента
        } else {
            int mid = (left + right) / 2;
            build(arr, node * 2, left, mid);
            build(arr, node * 2 + 1, mid + 1, right);
            tree[node] = tree[node * 2] + tree[node * 2 + 1]; // сумма детей
        }
    }

    // Обновление элемента: index – индекс в массиве, newVal – новое значение
    public void update(int index, int newVal) {
        update(1, 0, n - 1, index, newVal);
    }

    private void update(int node, int left, int right, int index, int newVal) {
        if (left == right) {
            tree[node] = newVal;
        } else {
            int mid = (left + right) / 2;
            if (index <= mid)
                update(node * 2, left, mid, index, newVal);
            else
                update(node * 2 + 1, mid + 1, right, index, newVal);
            tree[node] = tree[node * 2] + tree[node * 2 + 1];
        }
    }

    // Запрос суммы на отрезке [ql, qr]
    public int query(int ql, int qr) {
        return query(1, 0, n - 1, ql, qr);
    }

    private int query(int node, int left, int right, int ql, int qr) {
        if (qr < left || right < ql) return 0;          // нет пересечения
        if (ql <= left && right <= qr) return tree[node]; // полное покрытие
        int mid = (left + right) / 2;
        return query(node * 2, left, mid, ql, qr) +
                query(node * 2 + 1, mid + 1, right, ql, qr);
    }

    // Пример использования
    public static void main(String[] args) {
        int[] arr = {1, 3, 5, 7, 9, 11};
        ClassicSegTree st = new ClassicSegTree(arr);
        System.out.println(st.query(1, 3)); // 3+5+7 = 15
        st.update(1, 10);                  // массив: 1,10,5,7,9,11
        System.out.println(st.query(1, 3)); // 10+5+7 = 22
    }
}