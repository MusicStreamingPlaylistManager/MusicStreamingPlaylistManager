package lib;

import models.Song;

public class MergeSort {

    public static DynamicArrayList sortByTitle(DynamicArrayList source) {
        DynamicArrayList sorted = copy(source);
        if (sorted.size() <= 1) {
            return sorted;
        }
        mergeSort(sorted, 0, sorted.size() - 1);
        return sorted;
    }

    private static void mergeSort(DynamicArrayList list, int left, int right) {
        if (left >= right) return;

        int mid = left + (right - left) / 2;
        mergeSort(list, left, mid);
        mergeSort(list, mid + 1, right);
        merge(list, left, mid, right);
    }

    private static void merge(DynamicArrayList list, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;

        Song[] leftArr = new Song[n1];
        Song[] rightArr = new Song[n2];

        for (int i = 0; i < n1; i++) {
            leftArr[i] = list.get(left + i);
        }
        for (int j = 0; j < n2; j++) {
            rightArr[j] = list.get(mid + 1 + j);
        }

        int i = 0;
        int j = 0;
        int k = left;

        while (i < n1 && j < n2) {
            if (compareTitle(leftArr[i], rightArr[j]) <= 0) {
                list.set(k++, leftArr[i++]);
            } else {
                list.set(k++, rightArr[j++]);
            }
        }

        while (i < n1) {
            list.set(k++, leftArr[i++]);
        }
        while (j < n2) {
            list.set(k++, rightArr[j++]);
        }
    }

    private static int compareTitle(Song a, Song b) {
        return a.getTitle().compareToIgnoreCase(b.getTitle());
    }

    private static DynamicArrayList copy(DynamicArrayList source) {
        DynamicArrayList copy = new DynamicArrayList();
        for (int i = 0; i < source.size(); i++) {
            copy.add(source.get(i));
        }
        return copy;
    }
}
