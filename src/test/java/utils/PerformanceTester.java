package utils;

import lib.DynamicArrayList;
import lib.HistoryStack;
import lib.IndexedDoublyLinkedList;
import lib.MergeSort;
import lib.SongSearchEngine;
import models.Song;

import java.util.Random;
import java.util.UUID;

public class PerformanceTester {

    private static final int NUM_SONGS = 50000;
    private static final Random random = new Random();

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("Starting performance and algorithm complexity tests...");
        System.out.println("Number of simulated data elements: " + NUM_SONGS);
        System.out.println("==================================================");

        // Sinh dữ liệu thô ngẫu nhiên ban đầu
        DynamicArrayList rawLibrary = generateMockData(NUM_SONGS);

        // 1. Kiểm thử thuật toán sắp xếp và lấy ra mảng đã sắp xếp thực sự
        DynamicArrayList sortedLibrary = testMergeSort(rawLibrary);

        // 2. Khởi tạo engine tìm kiếm với mảng ĐÃ SẮP XẾP chuẩn BST
        SongSearchEngine searchEngine = new SongSearchEngine(sortedLibrary);
        testBinarySearchExact(searchEngine, sortedLibrary);
        testSearchByTitleContains(searchEngine);

        // 3. Kiểm thử các cấu trúc dữ liệu lưu trữ
        testIndexedDoublyLinkedList(sortedLibrary);
        testDynamicArrayList();
        testHistoryStack(sortedLibrary);
    }

    private static DynamicArrayList generateMockData(int count) {
        DynamicArrayList list = new DynamicArrayList();
        for (int i = 0; i < count; i++) {
            int id = i + 1;
            String title = "Song " + UUID.randomUUID().toString().substring(0, 8);
            String artist = "Artist " + random.nextInt(100);
            String genre = (i % 2 == 0) ? "Pop" : "Rock";
            int duration = 120 + random.nextInt(180);
            list.add(new Song(id, title, artist, genre, duration));
        }
        return list;
    }

    private static DynamicArrayList testMergeSort(DynamicArrayList library) {
        System.out.println("\n[1] Merge Sort Algorithm (Theoretical complexity: O(N log N))");
        long startTime = System.nanoTime();
        DynamicArrayList sorted = MergeSort.sortByTitle(library);
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;
        System.out.println(" -> Sorting " + library.size() + " songs took: " + durationMs + " ms");
        return sorted; // Trả về mảng đã sắp xếp để các hàm sau sử dụng
    }

    private static void testBinarySearchExact(SongSearchEngine searchEngine, DynamicArrayList library) {
        System.out.println("\n[2] Binary Search Algorithm - Exact Search (Theoretical complexity: O(log N))");
        // Lấy bài hát ở giữa mảng đã sắp xếp để test
        Song target = library.get(NUM_SONGS / 2);

        long startTime = System.nanoTime();
        Song found = searchEngine.binarySearchExactTitle(target.getTitle());
        long endTime = System.nanoTime();
        long durationNs = (endTime - startTime);
        System.out.println(" -> Searching for song '" + target.getTitle() + "' took: " + durationNs + " ns");
        System.out.println(" -> Result status: " + (found != null ? "SUCCESS (Found)" : "FAILED"));
    }

    private static void testSearchByTitleContains(SongSearchEngine searchEngine) {
        System.out.println("\n[3] Linear Search Algorithm - Relative Search (Theoretical complexity: O(N))");
        String keyword = "Song";
        long startTime = System.nanoTime();
        DynamicArrayList results = searchEngine.searchByTitle(keyword);
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;
        System.out.println(" -> Searching with keyword '" + keyword + "' took: " + durationMs + " ms");
        System.out.println(" -> Total matching results: " + results.size());
    }

    private static void testIndexedDoublyLinkedList(DynamicArrayList library) {
        System.out.println("\n[4] Indexed Doubly Linked List Data Structure (Theoretical complexity: O(1))");
        IndexedDoublyLinkedList playlist = new IndexedDoublyLinkedList();

        long startTime = System.nanoTime();
        for (int i = 0; i < library.size(); i++) {
            playlist.append(library.get(i));
        }
        long endTime = System.nanoTime();
        long addDurationMs = (endTime - startTime) / 1_000_000;
        System.out
                .println(" -> Sequentially adding " + library.size() + " songs to playlist took: " + addDurationMs + " ms");

        // Đo thao tác dịch chuyển nút bằng HashMap định vị địa chỉ ô nhớ trong O(1)
        int moveId = library.get(NUM_SONGS - 1).getSongId();
        int targetId = library.get(0).getSongId();
        startTime = System.nanoTime();
        playlist.moveSongAfter(moveId, targetId);
        endTime = System.nanoTime();
        System.out.println(" -> Drag and drop reordering (Reorder) took: " + (endTime - startTime) + " ns");

        // Đo thao tác xóa trực tiếp nút trong O(1)
        int removeId = library.get(NUM_SONGS / 4).getSongId();
        startTime = System.nanoTime();
        playlist.removeById(removeId);
        endTime = System.nanoTime();
        System.out.println(" -> Removing any node by ID (Remove) took: " + (endTime - startTime) + " ns");
    }

    private static void testDynamicArrayList() {
        System.out.println("\n[5] Dynamic Array List Data Structure (Amortized complexity: O(1))");
        DynamicArrayList list = new DynamicArrayList();

        long startTime = System.nanoTime();
        for (int i = 0; i < NUM_SONGS; i++) {
            list.add(new Song(i, "Title", "Artist", "Pop", 200));
        }
        long endTime = System.nanoTime();
        long addDurationMs = (endTime - startTime) / 1_000_000;
        System.out.println(
                " -> Automatically loading and resizing array with " + NUM_SONGS + " elements took: " + addDurationMs + " ms");

        startTime = System.nanoTime();
        Song s = list.get(NUM_SONGS / 2);
        endTime = System.nanoTime();
        System.out.println(
                " -> Random access of element by index (Random Access) took: " + (endTime - startTime) + " ns");
    }

    private static void testHistoryStack(DynamicArrayList library) {
        System.out.println("\n[6] History Stack Data Structure (Theoretical complexity: O(1))");
        HistoryStack stack = new HistoryStack();

        long startTime = System.nanoTime();
        for (int i = 0; i < 30; i++) { // Giới hạn đúng 30 để đạt O(1) không dính vòng lặp giải phóng bộ nhớ
            stack.push(library.get(i));
        }
        long endTime = System.nanoTime();
        System.out.println(" -> Pushing 30 consecutive songs to Stack took: " + (endTime - startTime) + " ns");

        startTime = System.nanoTime();
        stack.pop();
        endTime = System.nanoTime();
        System.out
                .println(" -> Popping 1 element from the top of the Stack (Rollback) took: " + (endTime - startTime) + " ns");
    }
}