# YÊU CẦU REFACTOR DỰ ÁN: MUSIC STREAMING PLAYLIST MANAGER (JAVA WEB JSP/SERVLET)

Tôi cần bạn chỉnh sửa (refactor) lại toàn bộ project **MusicStreamingPlaylistManager** hiện tại theo đúng ngữ cảnh, danh sách tính năng giữ lại, loại bỏ và các yêu cầu cấu trúc dữ liệu bên dưới ĐỒNG THỜI sửa lại việc cấu hình kết nối DB vào local

---

## 1. TỔNG QUAN VỀ SỰ THAY ĐỔI & LOGIC CỐT LÕI (CORE LOGIC)

### 🔴 1.1. Thay đổi Cấu trúc Dữ liệu Hàng chờ Phát nhạc (Doubly Linked List):

- **BỎ HOÀN TOÀN `HashMap`** trong danh sách hàng chờ phát nhạc.
- **Xóa hẳn file `IndexedDoublyLinkedList.java`**.
- **Tạo mới / Chuyển thành lớp `DoublyLinkedList.java` (DLL nguyên bản)**:
  - Chỉ bao gồm các trường: `private Node head;`, `private Node tail;` (mỗi `Node` gồm `prev`, `next`, `data`).
  - **Phương thức `jumpTo(int songId)` hoặc `getNodeById(int songId)`**: Phải chạy vòng lặp **Duyệt tuyến tính (Linear Search)** từ `head` đến `tail` để tìm `Node` có `data.getSongId() == songId`. Không được dùng bảng băm (HashMap) để lưu chỉ mục Pointer nữa.
  - Các hàm khác (`append`, `removeNode`, `toSongList`, v.v.) hoạt động theo chuẩn Doubly Linked List nguyên bản.

### 🟡 1.2. Logic Phát nhạc & Tải bài hát (QUAN TRỌNG NHẤT):

- **Tải dữ liệu từ Memory (`DynamicArrayList`), KHÔNG TẢI TỪ DATABASE (DB) KHI PHÁT/NẠP BÀI**:
  - Toàn bộ bài hát đã được pre-load vào bộ nhớ `DynamicArrayList` (thông qua `SongLibraryUtils` / `ServletContextListener`).
  - Khi phát 1 bài từ trang **Home** hay **Search**, hoặc khi tự động nạp thêm 10 bài hát cùng thể loại (genre), hệ thống **PHẢI lấy dữ liệu từ `DynamicArrayList` trong RAM**, tuyệt đối **KHÔNG query vào Database (`SongDAO`)** tại thời điểm phát/nạp bài.
- **Cơ chế khởi tạo và cập nhật DLL khi phát bài**:
  1. Khi người dùng bấm phát **1 bài hát** (từ `Home` hoặc `Search`):
     - Xóa / giải phóng `DoublyLinkedList` cũ đi.
     - Khởi tạo 1 `DoublyLinkedList` **mới hoàn toàn** và thêm duy nhất bài hát đó vào danh sách.
     - Trên giao diện (Playlist / Wait List), danh sách ban đầu **chỉ hiển thị 1 bài hát này**.
  2. Khi bài hát phát hết (Ended) hoặc người dùng nhấn кноп Next (`nextTrack`):
     - Hệ thống tự động lọc ra **10 bài hát cùng thể loại (genre)** từ `DynamicArrayList` trong RAM (loại trừ các bài đã có trong DLL).
     - Append (thêm) 10 bài hát này vào `DoublyLinkedList` hiện tại.
  3. Cứ mỗi khi người dùng bấm chọn một bài hát mới bất kỳ từ `Home` hoặc `Search`, `DoublyLinkedList` cũ lại bị xóa đi và một `DoublyLinkedList` mới lại được tạo lại từ đầu theo quy trình trên.

---

## 2. DANH SÁCH CHI TIẾT CÁC MỤC BỎ (REMOVED) VÀ GIỮ LẠI (KEPT)

### ✅ CÁC CHỨC NĂNG GIỮ LẠI (KEPT):

1. **Các nút điều khiển Player Bar (Footer)**: `Next`, `Previous`, `Repeat All`, `Repeat One`, `Shuffle`.
2. **Chức năng nhảy bài (`jumpToPlay` / `jumpTo`)**: Cho phép click vào bài hát bất kỳ trong Playlist hiện tại để phát ngay (tìm bằng Linear Search).
3. **Tìm kiếm (`Search`)**: Tìm kiếm bài hát theo từ khóa từ `DynamicArrayList` trong bộ nhớ.
4. **Xác thực (`Authen`)**: Đăng nhập (`login.jsp`), Đăng ký (`register.jsp`), Đăng xuất, Lưu Session đăng nhập.
5. **Thông tin Tác giả / Ca sĩ (`Author`)**: Hiển thị tên ca sĩ/tác giả trên bài hát & UI.
6. **Màn hình Now Playing (`nowplaying.jsp`)**: Màn hình phát nhạc chi tiết. **ĐỔI TÊN MỤC "WAIT LIST" THÀNH "PLAYLIST"**.
7. **Giao diện (UI)**: Giữ nguyên phong cách thiết kế hiện tại (màu sắc, layout, player footer), chỉ bỏ bớt các icon/nút theo yêu cầu bên dưới.

### ❌ CÁC MỤC BỎ (REMOVED):

1. **HashMap**: Xóa bỏ hoàn toàn khỏi cấu trúc dữ liệu phát nhạc.
2. **Cấu trúc dữ liệu & Lớp thừa**:
   - Xóa `IndexedDoublyLinkedList.java`.
   - Xóa các Servlet / DAO / Lib không liên quan đến mục "Giữ lại" (ví dụ: các tính năng Playlist lưu Database cũ như `PlaylistDAO.java`, `PlaylistServlet.java`, `FavoritesServlet.java`, `DynamicPlaylistList.java`, `playlist.jsp`, `playlist-detail.jsp` nếu không thuộc danh sách giữ).
3. **Thanh Footer (Player Bar)**:
   - **BỎ ICON TYM (Tym / Heart / Favorite)** hoàn toàn khỏi Footer Player Bar và các thẻ bài hát.
   - Chỉ giữ các icon: `Shuffle`, `Previous`, `Next`, `Repeat`.
4. **Trang Home (`home.jsp`)**:
   - **Bỏ mục "Recently"** (Recently Played).
   - **Bỏ chữ "All"** (Filter / Tab "All" ở trang Home).

---

## 3. CÁC TỆP CẦN CHỈNH SỬA / CẬP NHẬT CHI TIẾT

### 🛠️ 3.1. Package `lib/`:

1. **[XÓA]** `IndexedDoublyLinkedList.java`
2. **[TẠO MỚI] `DoublyLinkedList.java`**:
   - Trường: `Node head`, `Node tail`.
   - Không chứa `HashMap`.
   - Hàm `append(Song song)`: Thêm node vào cuối DLL.
   - Hàm `getNodeById(int songId)` / `jumpTo`: Duyệt vòng lặp `Node current = head; while(current != null)` để so sánh `songId`.
   - Hàm `toSongList()`: Trả về `DynamicArrayList` chứa các bài hát trong DLL để gửi về phía Client JSON.

### 🛠️ 3.2. Package `controllers/` & `models/`:

1. **`AudioPlayEngine.java`**:
   - Đổi `IndexedDoublyLinkedList waitingList` thành `DoublyLinkedList playlist`.
   - Chỉnh sửa `jumpTo(int songId)`: Duyệt linear tìm bài hát trong `DoublyLinkedList`.
   - Chỉnh sửa `autoAppendSongs()`: Lấy bài hát cùng genre từ `DynamicArrayList` (Song Library trong `ServletContext` RAM), **KHÔNG gọi `songDAO`**.
   - Chỉnh sửa `buildQuickPlayQueue(Song selected)`: Tạo `DoublyLinkedList` mới chỉ chứa `selected`.
   - Cập nhật các hàm `nextTrack()`, `previousTrack()`, `shuffleUpcoming()`, `setRepeatAll()`, `setRepeatOne()`.
2. **`PlayerServlet.java`**:
   - Cập nhật lại các API `/api/player/play`, `/api/player/next`, `/api/player/prev`, `/api/player/jump`, `/api/player/shuffle`, `/api/player/loop` tương thích với `DoublyLinkedList` và trả về JSON chuẩn cho client.

### 🛠️ 3.3. Web Pages & JavaScript (`src/main/webapp/`):

1. **`assets/js/app.js`**:
   - Cập nhật state client: đổi `waitList` thành `playlist`.
   - Loại bỏ toàn bộ logic liên quan đến Tym/Favorite/Heart icon.
   - Cập nhật hàm gọi API phát nhạc: Khi bấm phát bài hát mới -> reset `playlist` client.
2. **`home.jsp`**:
   - Xóa phần UI hiển thị danh sách "Recently Played".
   - Xóa nút/tab/thẻ chữ "All".
   - Xóa icon Tym ở các item bài hát.
3. **`nowplaying.jsp`**:
   - Đổi tiêu đề giao diện từ **"WAIT LIST"** (hoặc "Wait List") thành **"PLAYLIST"**.
   - Bỏ icon Tym trên màn hình này.
4. **Player Bar (Footer)**:
   - Xóa icon Tym (`#bar-heart` hoặc `.player-heart`).
   - Giữ lại đầy đủ các nút: Shuffle (`🔀`), Previous (`⏮`), Play/Pause (`▶/⏸`), Next (`⏭`), Repeat (`🔁`).

---

## 4. QUY TRÌNH THỰC HIỆN YÊU CẦU DÀNH CHO AI

1. **Kiểm tra codebase hiện tại**: Đọc kĩ các tệp trong `lib/`, `controllers/`, `webapp/assets/js/app.js`, `home.jsp`, `nowplaying.jsp`.
2. **Xóa & Sửa Code**:
   - Xóa `IndexedDoublyLinkedList.java` và tạo `DoublyLinkedList.java` tuyến tính nguyên bản.
   - Refactor `AudioPlayEngine.java` và `PlayerServlet.java`.
   - Sửa UI `home.jsp`, `nowplaying.jsp` và footer player bar.
   - Bỏ tất cả code/icon liên quan đến Tym (Heart/Favorite) và các trang/chức năng thừa.
3. **Kiểm tra biên dịch & Chạy thử**:
   - Đảm bảo dự án build thành công không bị lỗi tham chiếu symbol cũ (`IndexedDoublyLinkedList` hay `HashMap`).
   - Kiểm tra luồng: Bấm 1 bài -> Tạo DLL chứa 1 bài -> Bấm Next -> Tự động nạp thêm 10 bài cùng genre từ RAM.

👉 **Nếu có bất kỳ chỗ nào chưa rõ về cấu trúc file hoặc logic xử lý, hãy hỏi lại tôi ngay trước khi sửa code!**
