---
description: A description of your rule
---



FPT UNIVERSITY
	*	
  
Music Streaming Playlist Manager
 
 
Supervisor                    	:  Do Duc Hao
Class                             	    	:  SE2037
Team Members            	:  
Ho Le Thien An - SE201566
Huynh Huu Phuc - SE201562
Nguyen Hoang Khoi - SE201600
Pham Tuan Trien - SE201477
TABLE OF CONTENTS
TABLE OF CONTENTS	2
PROJECT CONTEXT	4
1. Project Context	4
2. Problem Statement	4
3. Functional Requirements	5
3.1. User Module:	5
3.2. Admin Module:	5
4. Project Scope	6
4.1. In Scope	6
4.2. Out of Scope	6
5. Development Environment & Tools	6
5.1. Core Technologies:	6
5.2. Development Tools:	6
REPORT 1	7
1. Research Question	7
2. System Decomposition	8
2.1 Functional Module Decomposition	8
2.2 Data / ERD Decomposition	9
2.2.1 Core Entities	9
2.2.2 Entity Relationships	12
2.3 Layered Storage and Memory Decomposition	13
2.3.1 Persistent Storage Layer (SQL Server)	13
2.3.2 In-Memory Data Structure Layer (RAM / Servlet Scope)	14
3. Pattern Recognition	15
3.1 Doubly Linked List (DLL) — Core Playback Navigation and Playlist Management	15
3.2 Stack — Playback History Log (User Lookup)	15
3.3 Contiguous Arrays & Binary Search — High-Speed Song Retrieval	16
4. Visualizing the Initial Logic	17
4.1 System Mindmap	17
4.2 Functional Flowcharts	18
5. AI Reflection & Audit Log	20
6. Conclusion of Report 1	23
Report 2	24
1. Class diagram	24
2. Abstraction	25
2.1. Optimized Song Model	25
2.2. Node API	25
2.3. Indexed Doubly Linked List	26
2.3.1. System Method Specification	26
2.3.2. Java 8 Implementation Source Code	27
2.4. History Stack	28
2.4.1. System Method Specification	28
2.4.2. Java 8 Implementation Source Code	30
2.5. Dynamic ArrayList	30
2.5.1. System Method Specification	30
2.5.2. Java 8 Implementation Source Code	32
3. Process/Algorithms	33
3.1 Play Operation Flow	33
3.2 Next Track Algorithm	35
3.3 Previous Track Algorithm	37
3.4 Repeat One Algorithm	39
3.5 Repeat All Algorithm	41
3.6 Shuffle Playback Algorithm	43
3.7 Automatic Queue Expansion Algorithm	45
4. Manual Trace	47
4.1. Test Data	47
4.2. Test Cases	47
4.3. Note	50
5. AI vs. Personal Implementation Comparison	52

 
PROJECT CONTEXT
1. Project Context
In the digital era, music streaming platforms are essential for modern entertainment. Users expect a seamless and highly personalized experience, including custom playlists, continuous playback, shuffle, and easy navigation between tracks.
For traditional Server-Side Rendering (SSR) web applications, handling these continuous and complex operations poses a significant resource management challenge. Seamlessly switching between a massive global music database and personalized playback streams for individual user sessions requires a tightly designed system starting right from its storage foundation.
2. Problem Statement
The core challenge in building a music player lies in optimizing time and space complexity during real-time data manipulation. Specifically, the project must address the following technical hurdles:
●	Database Bottleneck: Relational databases (SQL) are optimized for long-term persistent storage, not real-time read/write operations. Triggering physical disk queries for every minor user action (e.g., "Next", "Previous", or searching) would cause high latency and disrupt the listening experience.
●	Real-time State Management: The system needs an ultra-fast response mechanism to manage the Waiting List and track the current playback position. Additionally, it must maintain a temporary memory of the playback history to support seamless "rollback" features without modifying the original data.
●	High Data Shifting Costs: Features like infinite repeat, shuffle, or drag-and-drop playlist reordering require constant structural modifications. Using traditional sequential storage methods would result in massive computational costs for shifting elements, wasting memory and severely reducing server response time.
 
3. Functional Requirements
3.1. User Module:
●	Authentication: Allows registration and login to manage personal music spaces (via database querying).
●	Music Browsing: Keyword-based song search and metadata viewing, requiring an optimized algorithm (e.g., Binary Search).
●	Playlist Management:
○	Categorization: Manage two types of lists: "Favourite List" (persistent) and "Waiting List" (temporary).
○	Data Update: Add/remove songs and edit playlist details.
○	Save & Reorder: Convert a "Waiting List" to a "Favourite List" and rearrange track order (Applying Doubly Linked List to optimize node insertion/deletion).
●	Core Player Engine:
○	Playback Control: Play, Next, and Previous operations (Applying Doubly Linked List to navigate seamlessly using both next and prev node pointers).
○	Playback Modes: Shuffle, Repeat One, and Repeat All (Dynamically converting the Doubly Linked List into a Doubly Circular Linked List by temporarily linking the tail node back to the head node).
○	Auto-Queue Expansion: If a playlist ends without Repeat enabled, the system automatically queries the database to append 10 songs of the same genre.
○	Quick Play: Selecting a single track auto-generates a "Waiting List" containing that track plus 10 similar-genre songs for continuous playback.
○	History: Stores and displays recently played tracks (Applying a Stack data structure to maintain a chronological log of playback sessions). 
3.2. Admin Module:
●	Music Data Management: Full control over source data, including Adding, Editing, and Deleting songs from the system's core database.
 
4. Project Scope
4.1. In Scope
●	Developing a Server-Side Rendering (SSR) web application using Java Servlet and PostgreSQL.
●	Designing the relational database (ERD) and layered architecture (Persistent Cloud DB vs. In-memory RAM).
●	Building core functionalities: Authentication, music library management, playlist handling, and playback controls.
●	Technical Focus: Applying Data Structures (Linked Lists, Stacks) and Algorithms (Binary Search, Merge/Quick Sort) to optimize in-memory operations like switching tracks, shuffling, and searching.
4.2. Out of Scope
●	Cross-platform applications (Mobile/Desktop).
●	AI/Machine Learning for smart music recommendations.
●	Payment gateways, ads, or premium subscriptions.
●	Content Delivery Networks (CDN) for large media files.
5. Development Environment & Tools
5.1. Core Technologies:
●	Java 8: Backend logic and in-memory data structures.
●	JSP & Servlet: SSR handling and session management.
●	PostgreSQL: Persistent data storage and relational database management.
●	Supabase Cloud: Cloud infrastructure for hosting the PostgreSQL database and managing media assets (audio files and images).
5.2. Development Tools:
●	Apache NetBeans 13: Primary IDE.
●	pgAdmin 4 / Supabase Dashboard: Database management, schema design, and data inspection.
●	GitHub: Version control and collaboration.

 
REPORT 1
1. Research Question
1.1 Research Question Statement
Based on the analyzed context and technical barriers, the research focus of this project within the scope of the Data Structures and Algorithms (CSD) course is defined by the following question:
"How does maintaining an in-memory (RAM) Doubly Linked List with dynamic pointer manipulation optimize time complexity O(1) for bidirectional navigation, loop toggling, and real-time playlist reordering compared to executing heavy I/O operations directly on a physical persistent database?"
1.2 Research Objectives
The above research question is established to address the following specific objectives:
●	Latency & Complexity Measurement: Compare the execution time (in milliseconds) and theoretical time complexity of track-switching navigation (Next/Previous) and list modifications when modifying pointer references in RAM versus executing physical structural queries to a remote persistent database.
●	Space vs. Time Trade-off: Demonstrate the efficiency of allocating a minor portion of application session memory to hold dynamic pointer-based structures (Doubly Linked List and Stack) in exchange for instantaneous, near-O(1) operational access speed.
●	Algorithmic Adaptability: Validate how a single Doubly Linked List can dynamically shift its structural boundaries (connecting or disconnecting the head and tail pointers) to natively support complex media features like infinite loops, shuffles, and safe structural updates without data fragmentation.
1.3 Hypothesis
Applying a pointer-based Doubly Linked List in RAM for session queue management—capable of morphing into a bidirectional circular configuration for playback modes—alongside a Stack for chronological log caching, will drastically mitigate database bottleneck constraints. Consequently, the system's execution latency for all runtime user actions will achieve O(1) efficiency, eliminating buffering delays and proving that dynamic in-memory data structures are far superior to reliance on persistent database queries for real-time applications.

2. System Decomposition
2.1 Functional Module Decomposition
Decomposition is the process of breaking a complex system into smaller and manageable functional components. In the Music Streaming Playlist Manager, the overall problem of handling music playback, playlist management, and real-time user interaction is divided into independent processing modules and data management layers.
This decomposition helps to:
●	Reduce overall system complexity
●	Improve clarity in system architecture and data flow
●	Increase maintainability and scalability
●	Minimize tight coupling between playback operations and persistent storage
●	Optimize real-time performance through efficient in-memory data structures
Based on the proposed architecture and implementation design, the system is divided into the following main modules:
●	Authentication Module
Responsible for:
○	User registration and login validation
○	Session initialization
○	Role verification (Admin/User)
This module interacts directly with the USER entity in the database.
●	Music Library Module
Responsible for:
○	Loading song data from persistent storage
○	Searching and filtering songs
○	Managing metadata display
An ArrayList combined with Binary Search is used to optimize read-heavy operations.
●	Playlist Management Module
Responsible for:
○	Creating and updating playlists
○	Managing Waiting List and Favorite List
○	Handling song ordering inside playlists
A Doubly Linked List structure is applied to optimize insertion, deletion, and rearrangement operations.
●	Playback Engine Module
Responsible for:
○	Playback navigation
○	Repeat and Shuffle logic
○	Auto queue expansion
○	Current playback state management
○	Storing playback history for user lookup 
A Doubly Linked List structure is applied to optimize bidirectional navigation via forward and backward pointers, while a Stack structure is utilized exclusively for maintaining a history log of played songs. 
●	Admin Management Module
Responsible for:
○	CRUD operations for songs
○	Maintaining the central music repository
○	Updating persistent storage
2.2 Data / ERD Decomposition 
2.2.1 Core Entities
Based on the latest Functional Requirements, the system is designed around 4 core entities. Special attention has been given to new attributes added to support specific business and data structure logic. 
●	USER: Stores account information and user permissions.
Attribute	Data Type	Key Type	Description / Logic
UserID	INT	Primary Key	Auto-incremented unique identifier.
Username	VARCHAR	Unique	User's unique login name.
Password	VARCHAR	—	Stores the securely encrypted password.
Role	VARCHAR	—	Account permission level (Values: 'Admin' or 'User').
CreatedAt	DATETIME	—	Account creation timestamp.

●	SONG: The central music repository managed exclusively by Administrators.
Attribute	Data Type	Key Type	Description / Logic
SongID	INT	Primary Key	Auto-incremented unique identifier.
Title	NVARCHAR	—	Title of the song.
Artist	NVARCHAR	—	Name of the performing artist.
Genre	NVARCHAR	—	Critical Attribute: Required to query data for the "Auto-add 10 songs of the same genre" feature.
Duration	INT	—	Total playback length of the track (calculated in seconds).
FilePath	VARCHAR	—	Storage path or URL to the audio file (.mp3) on the server.
CoverPath	VARCHAR	—	Storage path or URL to the song's album art image.

●	PLAYLIST: Acts as the header wrapper containing metadata for a playlist created by a user.
Attribute	Data Type	Key Type	Description / Logic
PlaylistID	INT	Primary Key	Auto-incremented unique identifier.
UserID	INT	Foreign Key	References the USER who owns this playlist.
Name	NVARCHAR	—	Display name of the playlist (e.g., "Weekend Chill").
Type	VARCHAR	—	Classifies the list (Values: 'Favourite' or 'Waiting'). This allows the system to execute the "Convert Waiting List to Favorite List" requirement via a single, simple UPDATE query.
CreatedAt	DATETIME	—	Playlist creation timestamp.
IsDefault	BOOLEAN 	—	Default flag: auto-adds liked songs to the user's primary favourite list. 

●	PLAYLIST_SONG_DETAIL: A Junction Table used to resolve the many-to-many (N-N) relationship between playlists and songs. A single playlist contains multiple songs, and a single song can exist in multiple playlists.
Attribute	Data Type	Key Type	Description / Logic
PlaylistID	INT	Primary Key, Foreign Key	References the associated PLAYLIST.
SongID	INT	Primary Key, Foreign Key	References the associated SONG.
OrderIndex	INT	—	Key Data Structure Attribute: Stores the sequential order of the song within that specific playlist. When a user rearranges songs, this column is updated. When loading data into RAM to populate the Doubly Linked List, the query will execute ORDER BY OrderIndex ASC.

2.2.2 Entity Relationships
To accurately construct the Entity-Relationship Diagram (ERD), the system defines the following three core relationships:

●	USER (1) — (N) PLAYLIST (One-to-Many Relationship)
○	A User can create an unlimited number of Playlists (either a Favorite List or a Waiting List).
○	A Playlist must strictly belong to exactly one User.
●	PLAYLIST (1) — (N) PLAYLIST_SONG_DETAIL (One-to-Many Relationship)
○	A Playlist can contain multiple detailed records (linking to multiple songs).
●	SONG (1) — (N) PLAYLIST_SONG_DETAIL (One-to-Many Relationship)
○	A single Song from the central repository can appear in many different playlists across various users.
2.3 Layered Storage and Memory Decomposition 
To optimize both persistent storage efficiency and real-time playback performance, the system architecture is divided into two core processing layers: the Persistent Storage Layer and the In-Memory Processing Layer.
2.3.1 Persistent Storage Layer (SQL Server)
This layer is responsible for storing permanent system data, user identities, configurations, and long-term playlists.
The following tables are maintained inside the relational database:
●	Users (UserID, Username, Password, Role)
●	Songs (SongID, Title, Artist, Genre, Duration, FilePath)
●	Playlists (PlaylistID, UserID, Name)
 → Only Favorite Lists are permanently stored in the database. Temporary Waiting Lists are maintained entirely in memory.
●	Playlist_Songs (PlaylistID, SongID, OrderIndex)
 → Junction table used to store the ordered list of songs inside each Favorite Playlist.
Database write operations (INSERT, UPDATE, DELETE) occur during the following scenarios:
●	User Registration: INSERT operation into the Users table.
●	Music Repository Management by Admin: INSERT, UPDATE, and DELETE operations directly on the Songs table.
●	Favorite Playlist Management: INSERT and DELETE operations on the Playlists table when users create, remove, or rename playlists.
●	Playlist Song Management: INSERT, DELETE, and UPDATE operations on the Playlist_Songs table when users add/remove tracks or rearrange song positions through the OrderIndex attribute.
●	Waiting List Conversion: When a user converts a Waiting List into a Favorite List, the system traverses the in-memory linked list structure and performs batch INSERT operations into both Playlists and Playlist_Songs tables.
2.3.2 In-Memory Data Structure Layer (RAM / Servlet Scope)
This layer is responsible for handling all real-time playback logic, searching operations, and temporary runtime data structures directly in RAM to minimize database I/O latency.
Application Scope (ServletContext)
During server startup, the system loads the entire Songs dataset into a dynamic ArrayList structure stored in the global application scope.
Purpose:
●	Accelerate song searching using Binary Search
●	Support sorting algorithms such as Merge Sort
●	Reduce repetitive database queries shared across all active users
This shared memory structure allows the server to perform high-frequency read operations without continuously reconnecting to the database.
Session Scope (HttpSession)
Each authenticated user session initializes independent runtime data structures after successful login.
●	History Rollback (Stack Structure)
Operation:
○	Maintains the most recent playback history entirely in RAM (up to 30 recently played tracks).
○	Whenever a song finishes or the user presses Next, the current song object is pushed onto the Stack.
○	This structure is utilized exclusively to maintain a history log, allowing users to review, track, or search through their recently listened songs at any time. 
Characteristics:
○	No database interaction is required during rollback operations.
○	The Stack structure is automatically released when the user logs out or the browser session expires.
●	Waiting List & Playback Engine (Doubly Linked Lists)
Operation:
○	When a user selects a single song or when the current playlist reaches its end, the Servlet queries approximately 10 songs of the same genre from the database.
○	These songs are dynamically loaded into RAM and initialized as a Doubly Linked List (DLL) structure. 
Characteristics:
○	All immediate playback operations such as Next and Previous, Shuffle, adding/removing songs, and drag-and-drop reordering are executed smoothly by manipulating node references (next, prev) directly in memory.
○	Navigating to the previous track is handled efficiently using the backward pointer (prev) of the DLL.
○	This design ensures near-instantaneous response time while minimizing physical database access during active playback sessions.
3. Pattern Recognition 
Pattern Recognition is the process of identifying repeated structures, rules, and similarities in the system in order to standardize solutions and improve reusability. Several common patterns were identified during the development of this system.
By recognizing the recurring behavioral patterns of users—such as navigating back and forth through queues, shuffling tracks, loops, and looking up music data—the system maps these behaviors to optimal data structures to maximize performance (O(1) time complexity where possible) and minimize memory overhead.
3.1 Doubly Linked List (DLL) — Core Playback Navigation and Playlist Management 
●	Identified Pattern: Users frequently perform bidirectional navigation (pressing "Next" to advance or "Previous" to return to the preceding track) within a queue. Additionally, they often perform structural modifications on playlists, such as inserting a new song into a specific position, deleting a track, or dragging and dropping songs to rearrange their order. 
●	Justification: While a standard Array or Vector requires shifting elements (O(n) time complexity) during insertion or deletion, a Doubly Linked List handles these operations seamlessly in O(1) time once the target node is located. Because each node contains pointers to both the next and prev tracks, DLL inherently supports seamless bidirectional traversal. Moving to the previous track is achieved instantly by following the backward pointer (prev), eliminating the need for complex tracking structures. Continuous looping configurations ("Repeat All") can also be handled efficiently via boundary pointer updates. 
3.2 Stack — Playback History Log (User Lookup) 
●	Identified Pattern: The process of logging recently played tracks follows a strict Last-In, First-Out (LIFO) behavioral pattern. The song that just finished playing or was recently skipped is always the most relevant, immediate entry added to the top of the user's chronological listening timeline. 
●	Justification: A Stack data structure naturally mirrors this operational flow for historical tracking. Every time a track finishes or the user skips to another song, the current song object is pushed onto the stack. If the user wishes to review or search through their recent listening history, the system can display or pop elements from the top of the stack, guaranteeing immediate O(1) access to the most recently played songs without interfering with active queue navigation. 
3.3 Contiguous Arrays & Binary Search — High-Speed Song Retrieval
●	Identified Pattern: Users expect instantaneous results when searching for a song or filtering by keywords. Once the central music library is loaded from the database, it behaves as a static or rarely modified dataset on the user's end.
●	Justification: For data that is frequently read but rarely altered, a sorted Array is the ideal candidate. Because elements are stored contiguously in memory, we can leverage Binary Search algorithms instead of slow linear scans. This reduces the search time complexity from O(n) to O(log n), keeping the platform responsive even as the central repository expands to tens of thousands of tracks.
 
4. Visualizing the Initial Logic 
4.1 System Mindmap
The System Mindmap provides a high-level overview of the Music Streaming Playlist Manager's architecture. It visually connects core functional modules and the dual-layered storage strategy with their corresponding data structures. This diagram acts as a structural blueprint, simplifying system complexity before diving into the detailed logic of the subsequent flowcharts. 
 Picture 1.1: System Mindmap

As illustrated above, the mindmap successfully establishes the boundaries of the system's modules and maps them to their respective data structures. Building upon this structural foundation, the following flowcharts will detail the step-by-step execution logic of these components. 
 
4.2 Functional Flowcharts 
To effectively translate the proposed architecture into actionable designs, visualizing the business logic is a crucial step before diving into detailed algorithmic implementation. For optimal clarity, the overall user interaction lifecycle has been decomposed into three distinct flowcharts:
●	Authentication Flow: Outlines the security checkpoints for accessing the personal workspace, from registration to login validation.
Picture 1.2: Authentication Flow 
●	Playlist Initialization Flow: Illustrates how data is dynamically routed from physical storage into runtime memory based on user search or playlist selection.
Picture 1.3: Playlist Initialization Flow 
●	Music Player Operations Flow: Details the real-time handling of user navigation events within the memory space, including track switching, shuffling, and automated queue expansion.
 
Picture 1.4: Music Player Operations Flow
 
5. AI Reflection & Audit Log
During the decomposition and algorithmic design phases, the development team actively utilized AI as an advisory tool. However, to ensure the system strictly adhered to real-world business logic and the Data Structures and Algorithms (DSA) course requirements, AI suggestions were critically evaluated. The table below highlights the "Initial Sketch vs. AI Suggestion" comparison, demonstrating the team's critical thinking and justifications for accepting, modifying, or rejecting AI proposals.

Feature / Module	Team's Initial Sketch	AI Prompt Used	AI Suggestion	Action	AI Reflection & Justification
Data Storage Architecture (Decomposition)	Store all user queues, playback histories, and playlists directly in the SQL database to prevent data loss.	"Design a high-level layered data architecture, clearly define what stays in the database and what lives in RAM."	AI proposed a 2-layer design: Persistent Layer (Database for Users, Songs, Playlists) and In-Memory Layer (RAM for ArrayList, Doubly Linked List, Stack).	Accept	The functional split fits the project perfectly. However, the team added a strict business rule to minimize database I/O: "Waiting Lists" and "History" exist purely in RAM (Session Scope) and are never persisted. Only explicitly saved "Favorite Lists" are written to the database.
Song Search Mechanism (Algorithms)	Use a standard ArrayList and sort the data to prepare for search operations.	"Guide me to use ArrayList and Binary Search algorithm to implement song search function."	AI suggested sorting the ArrayList by SongID to apply the Binary Search algorithm.	Reject	AI hallucinated user behavior. In real-world applications, users search by song title or keywords, never by a database SongID. Sorting by ID makes Binary Search useless for the UI. The team rejected this and chose to sort by Title, implementing a custom Binary Search that supports partial matching.
Continuous Playback & Repeat Logic (Pattern Recognition)	Use a standard Linked List and manually check if the next node is null to loop back to the beginning.	"The player must continue from the last song back to the first song in Repeat All mode. What data structure naturally represents this pattern?"	AI suggested using a Circular Linked List because the tail node points back to the head node, implying it should always loop continuously.	Reject	AI misunderstood the context: a standalone Circular Linked List forces a permanent loop, breaking normal linear playback. Instead, the team utilized a Doubly Linked List (DLL) for native bidirectional navigation (next/prev), which dynamically mutates into a circular state (Tail.next = Head) only when the Repeat flag is enabled.  
Quick Play / Auto-Queue Expansion (Abstraction)	When a single song finishes, the player simply stops, or a random track is played.	"If a user plays a single song with no playlist, what happens when it ends? Should we add an all songs browsing page?"	AI suggested fetching 10-20 random tracks via ORDER BY NEWID() from the database and loading them into a Doubly Linked List.	Modify	While random selection works, serving unrelated songs creates a poor listening experience. The team replaced the random fetch with a "same-genre" query using the existing Genre attribute, loading 10 similar tracks into the Waiting List to ensure seamless and relevant continuous playback.
Playlist Editor Complexity (Algorithms)	Use a Doubly Linked List (DLL) to rearrange and delete songs efficiently.	"Explain the strengths of the DLL structure in terms of Big O time complexity."	AI claimed that with a Doubly Linked List, all operations to add, delete, or change the song order achieve O(1) time complexity.	Modify	AI oversimplified the complexity. The detach/insert operation is O(1) only if the system already holds a direct pointer to the target node. Without it, locating the node still costs O(n). The team fixed this logic gap by integrating a HashMap<SongID, Node> to retrieve the node in O(1) before performing DLL pointer manipulations.
 
6. Conclusion of Report 1
In conclusion, Report 1 has successfully established the structural and logical foundation for the Music Streaming Playlist Manager. By analyzing the performance bottlenecks of traditional database-driven media players, the system was strategically decomposed into a dual-layered architecture that separates persistent SQL storage from high-speed, in-memory processing. Through careful pattern recognition, user behaviors were mapped to optimal data structures—specifically leveraging Doubly Linked Lists for playlist manipulation, Circular Linked Lists for seamless playback loops, and Stacks for history rollback. The operational workflows were further solidified through comprehensive mindmaps and flowcharts, while critical AI audits ensured the design remains academically rigorous and practically feasible. This thorough system analysis provides a clear and robust roadmap for Report 2, where these high-level architectural concepts will be translated into detailed abstractions, pseudocode, and Big O complexity evaluations to definitively validate the project's research hypothesis.
 
Report 2
1. Class diagram
This Class Diagram illustrates the object-oriented architecture of the music streaming playback engine, highlighting the structural relationships and lifecycle dependencies between runtime components. The AudioPlayEngine serves as the central controller, maintaining a composition over the HistoryStack for playback history and an aggregation with the IndexedDoublyLinkedList which acts as the core runtime queue. Individual tracks are encapsulated within Node objects that support bi-directional navigation via reflexive prev and next pointers, while an automated songIndex map ensures constant-time lookup. Additionally, a DynamicArrayList is utilized to provide highly efficient contiguous storage for caching and shuffle operations, establishing a robust, scalable system architecture. 
 
Picture 2.1: Class diagram
 
2. Abstraction
This section outlines the abstraction of core data structures designed to optimize performance and memory management within the music streaming system. By utilizing streamlined data models and specialized structures such as the Indexed Doubly Linked List, History Stack, and Dynamic ArrayList, the system achieves optimal time complexity for runtime operations. This architectural separation ensures stability and scalability, enabling the player to efficiently handle large-scale music data. 
2.1. Optimized Song Model
The Song class is streamlined to store only core navigational metadata. Excluding static resource paths reduces memory-copy overhead when the dynamic array resizes. 
public class Song {
    private int songId;      
    private String title; 
    private String artist;   
    private String genre; 
    private int duration; 

    public Song(int songId, String title, String artist, String genre, int duration) {
        this.songId = songId;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.duration = duration;
    }

    public int getSongId() { return this.songId; }
    public String getTitle() { return this.title; }
    public String getArtist() { return this.artist; }
    public String getGenre() { return this.genre; }
    public int getDuration() { return this.duration; }
}
2.2. Node API
Act as the basic building blocks of the doubly linked list and stack structures. 
public class Node {
    public Song data;
    public Node prev;
    public Node next;

    public Node(Song data) {
        this.data = data;
        this.prev = null;
        this.next = null;
    }    
public Node(Song data, Node prev, Node next) {
        this.data = data;
        this.prev = prev;
        this.next = next;
    }
}
2.3. Indexed Doubly Linked List
This solution thoroughly eliminates the O(n) performance bottleneck when users edit the list structure in the Playlist UI. By adding a HashMap<Integer, Node> as an index table that maps each ID to the memory address (pointer) of its corresponding node in RAM, the system reduces all insert, delete, and reorder operations to constant-time O(1). 
2.3.1. System Method Specification
Method name	Parameter	Return	Logic
append	Song song	void	Insert the song at the tail of the list using the tail pointer, and record the Node address in the HashMap. 
removeById	int songId	boolean	Directly tracing the memory address through the HashMap takes O(1), then disconnecting and reconnecting the surrounding pointers frees the element without a linear traversal. 
moveSongAfter	int songIdToMove, int targetSongId	boolean	Supports the drag-and-drop feature. It quickly locates the two Nodes in O(1), detaches the old Node, and re-establishes the links after the target Node. 
2.3.2. Java 8 Implementation Source Code
import java.util.HashMap;
import java.util.Map;

public class IndexedDoublyLinkedList {
    private Node head = null;
    private Node tail = null;
    private Map<Integer, Node> songIndex = new HashMap<>(); 
    public void append(Song song) {
        if (songIndex.containsKey(song.getSongId())) return;
        
        Node newNode = new Node(song);
        if (head == null) {
            head = newNode; 
            tail = newNode;
        } else {
            tail.next = newNode; 
            newNode.prev = tail; 
            tail = newNode;
        }
        songIndex.put(song.getSongId(), newNode);
    }

    public boolean removeById(int songId) {
        Node targetNode = songIndex.get(songId);
        if (targetNode == null) return false;

        if (targetNode == head) {
            head = targetNode.next;
            if (head != null) head.prev = null;
        } else {
            targetNode.prev.next = targetNode.next;
        }

        if (targetNode == tail) {
            tail = targetNode.prev;
            if (tail != null) tail.next = null;
        } else {
            targetNode.next.prev = targetNode.prev;
        }

        songIndex.remove(songId);
        return true;
    }

    public boolean moveSongAfter(int songIdToMove, int targetSongId) {
        Node nodeToMove = songIndex.get(songIdToMove);
        Node targetNode = songIndex.get(targetSongId);
        if (nodeToMove == null || targetNode == null || songIdToMove == targetSongId) return false;

        if (nodeToMove == head) {
            head = nodeToMove.next;
            if (head != null) head.prev = null;
        } else {
            nodeToMove.prev.next = nodeToMove.next;
        }
        if (nodeToMove == tail) {
            tail = nodeToMove.prev;
            if (tail != null) tail.next = null;
        } else {
            nodeToMove.next.prev = nodeToMove.prev;
        }

        Node nextNodeOfTarget = targetNode.next;
        targetNode.next = nodeToMove;
        nodeToMove.prev = targetNode;

        if (nextNodeOfTarget == null) {
            tail = nodeToMove;
            nodeToMove.next = null;
        } else {
            nodeToMove.next = nextNodeOfTarget;
            nextNodeOfTarget.prev = nodeToMove;
        }
        return true;
    }

    public Node getHead() { return this.head; }
    public Node getTail() { return this.tail; }
}
2.4. History Stack
Manages the list of recently played songs using a LIFO (Last-In, First-Out) mechanism. To prevent RAM leaks, the stack size is strictly capped at 30 elements. When the limit is exceeded, the oldest bottom element is automatically discarded. 
2.4.1. System Method Specification
Method name	Parameter	Return	Logic
HistoryStack (Constructor)	N/A	N/A (Khởi tạo)	Initialize an empty stack with the top pointer set to null and the size counter set to 0. 
push	Song song	void	Wrap the song in a new Node and insert it at the top in O(1). If size exceeds MAX_SIZE (30 songs), the system triggers a loop to traverse down to the oldest bottom element, disconnects its pointer, and automatically frees RAM. 
pop	N/A	Song	Check whether top == null; if so, return null. Otherwise, retrieve the song data at the top, move the top pointer down to the node below, decrement size, and return the Song object for rollback. 
isEmpty	N/A	boolean	Quickly check the stack state by comparing the top pointer with null to protect the system from invalid function calls. 
2.4.2. Java 8 Implementation Source Code
public class HistoryStack {
    private Node top = null;
    private int size = 0;
    private final int MAX_SIZE = 30;

    public void push(Song song) {
        Node newNode = new Node(song);
        if (top == null) {
            top = newNode;
        } else {
            newNode.next = top; 
            top = newNode;
        }
        size++;

        if (size > MAX_SIZE) {
            Node current = top;
            while (current.next != null && current.next.next != null) {
                current = current.next;
            }
            current.next = null;
            size = MAX_SIZE;
        }
    }

    public Song pop() {
        if (top == null) return null;
        Song poppedData = top.data;
        top = top.next;
        size--;
        return poppedData;
    }

    public boolean isEmpty() { return top == null; }
}
2.5. Dynamic ArrayList
It is initialized centrally in the application scope as soon as the server starts successfully. This structure preloads raw data from the database into RAM, provides O(1) random access by index, and serves as the foundation for high-frequency quick sort and binary search operations. 
2.5.1. System Method Specification
Method name	Parameter	Return	Logic
DynamicArrayList (Constructor)	N/A	N/A	Allocate an initial static Song[] array with a capacity of 10 and set the tracking variable currentSize to 0. 
add	Song song	void	Add the element at position currentSize. If the array is full (currentSize == capacity), the system automatically doubles the capacity, creates a new array, and uses System.arraycopy to move the old data to the new memory region, achieving amortized O(1). 
get	int index	Song	Perform a bounds check 
(0≤index<currentSize)
(0≤index<currentSize). Then return the song object directly from the corresponding memory slot via random access in O(1). 
size	N/A	int	Return the actual number of songs currently present in the buffer list, allowing the Binary Search loop to determine the exact search range. 
2.5.2. Java 8 Implementation Source Code 
public class DynamicArrayList {
    private Song[] array = new Song[10];
    private int capacity = 10;
    private int currentSize = 0;

    public void add(Song song) {
        if (currentSize == capacity) {
            capacity *= 2;
            Song[] newArray = new Song[capacity];
            System.arraycopy(array, 0, newArray, 0, currentSize);
            array = newArray;
        }
        array[currentSize++] = song;
    }

    public Song get(int index) {
        if (index < 0 || index >= currentSize) {
            throw new IndexOutOfBoundsException("Out of index");
        }
        return array[index];
    }

    public int size() { return this.currentSize; }
}
 
3. Process/Algorithms
This section details the algorithms for managing playback sessions, including queue handling, playback history, and various repeat modes. The system operates through dynamic pointer updates and coordinated data structures to handle advanced features such as shuffle and automatic queue expansion. These algorithms ensure a continuous, seamless listening experience while optimizing response times throughout the playback process. 
3.1 Play Operation Flow
Flowchart:
 
Picture 2.2: Play operation flowchart
Algorithm:
The playback engine is initialized through the constructor:
public AudioPlayEngine(IndexedDoublyLinkedList waitingList) {
    this.waitingList = waitingList;
    this.currentTrackPointer = waitingList.getHead();
    this.playbackHistory = new HistoryStack();
}
If the user selects a Playlist or Favourite List:
this.currentTrackPointer = waitingList.getHead();
→ the first song in the Wait List will be played first.
If the user selects a song directly from the Home Page or Search Result:
currentTrackPointer.data
→ the playback engine will play the song corresponding to the currently selected node in the Wait List.
playbackHistory is initialized to store playback history during the runtime session.
 
3.2 Next Track Algorithm
Flowchart:
 
Picture 2.3: Next track flowchart
Algorithm:
Check the current playback state:
if (currentTrackPointer == null) {
    return;
}
Check Repeat One mode:
if (repeatOne) {
    System.out.println("Repeat One đang bật. Phát lại: "
            + currentTrackPointer.data.getTitle());
    return;
}
If Repeat One is disabled, the current song is stored in playback history:
playbackHistory.push(currentTrackPointer.data);
Move to the next node in the Doubly Linked List:
if (currentTrackPointer.next != null) {

    currentTrackPointer = currentTrackPointer.next;

    System.out.println("Đang phát bài tiếp theo: "
            + currentTrackPointer.data.getTitle());
}
If the playback reaches the end of the Wait List and Repeat All is enabled:
currentTrackPointer = waitingList.getHead();
→ the playback engine returns to the first song in the runtime playlist.
If Repeat All is disabled:
autoAppendSongs();
→ the system automatically loads 10 additional songs with the same genre from the database.
After new songs are appended successfully:
if (currentTrackPointer.next != null) {

    currentTrackPointer = currentTrackPointer.next;

    System.out.println("Đang phát bài mới được tải thêm: "
            + currentTrackPointer.data.getTitle());
}
→ the playback engine continues playing the newly appended songs in the Wait List runtime.
3.3 Previous Track Algorithm
Flowchart:
 
Picture 2.4: Previous track flowchart

 
Algorithm:
Check playback history:
if (playbackHistory.isEmpty()) {
    System.out.println("Không có lịch sử bài hát trước đó.");
    return;
}
Retrieve the most recent song from HistoryStack:
Song previousSong = playbackHistory.pop();
The playback engine restores the previous song:
System.out.println("Trở lại bài hát trong lịch sử: "
        + previousSong.getTitle());
The algorithm does not use:
currentTrackPointer.prev
to return to the previous song because:
the playback runtime order may change after Shuffle
the previously played song in actual listening history may differ from the previous node in the Doubly Linked List
HistoryStack helps the system maintain the correct playback order during the runtime session.
 
3.4 Repeat One Algorithm
Flowchart:
 
Picture 2.5: Repeat one flowchart
Algorithm:
Repeat One mode is enabled through:
public void setRepeatOne(boolean status) {
    this.repeatOne = status;
}
Inside nextTrack(), the playback engine checks the Repeat One state:
if (repeatOne) {
    System.out.println("Repeat One đang bật. Phát lại: "
            + currentTrackPointer.data.getTitle());
    return;
}
The playback engine uses:
currentTrackPointer.data
to replay the current song.
During Repeat One mode:
currentTrackPointer does not move to the next node
playbackHistory.push() is not called
the runtime playback order remains unchanged
 
3.5 Repeat All Algorithm
Flowchart:
 
Picture 2.6: Repeat all flowchart

 
Algorithm:
Repeat All mode is enabled through:
public void setRepeatAll(boolean status) {
    this.isRepeatAllEnabled = status;
}
Inside nextTrack(), the playback engine checks the end of the Wait List:
if (currentTrackPointer.next != null) {
    currentTrackPointer = currentTrackPointer.next;
}
If currentTrackPointer.next == null, the playback engine checks the Repeat All state:
if (isRepeatAllEnabled) {
    currentTrackPointer = waitingList.getHead();
    System.out.println("Vòng lặp kích hoạt! Quay về bài đầu tiên: "
            + currentTrackPointer.data.getTitle());
}
The playback engine uses:
waitingList.getHead()
to return to the first node in the Wait List runtime.
After the pointer is reset:
the playlist starts again from the beginning
the playback session continues running
the Wait List runtime structure remains unchanged
 
3.6 Shuffle Playback Algorithm
Flowchart:
 
Picture 2.7: Shuffle playback flowchart
Algorithm:
Check whether there are enough songs to shuffle:
if (currentTrackPointer == null
        || currentTrackPointer.next == null) {
    System.out.println("Không đủ bài hát để shuffle.");
    return;
}
Export future songs after the current song into DynamicArrayList:
DynamicArrayList songs = new DynamicArrayList();
Node temp = currentTrackPointer.next;
while (temp != null) {
    songs.add(temp.data);
    temp = temp.next;
}
The playback engine applies the Fisher-Yates Shuffle algorithm:
Random random = new Random();
for (int i = songs.size() - 1; i > 0; i--) {
    int j = random.nextInt(i + 1);
    Song tempSong = songs.get(i);
    songs.set(i, songs.get(j));
    songs.set(j, tempSong);
}
After shuffling is completed, the shuffled data is written back into the Wait List runtime:
temp = currentTrackPointer.next;
int index = 0;
while (temp != null) {
    temp.data = songs.get(index);
    temp = temp.next;
    index++;
}
Throughout the process:
currentTrackPointer remains unchanged
the currently playing song is preserved
only future songs are shuffled
the playback runtime continues normally
 
3.7 Automatic Queue Expansion Algorithm
Flowchart:
 
Picture 2.8: Automatic Queue Expansion flowchart

Algorithm:
When the playback engine reaches the end of the Wait List:
if (currentTrackPointer.next == null)
and Repeat All is disabled:
autoAppendSongs();
The playback engine retrieves the genre of the current song:
String genre = currentTrackPointer.data.getGenre();
The system queries the database through songDAO:
songDAO dao = new songDAO();

DynamicArrayList songs = dao.getSongsByGenre(genre, 10);
Songs are iterated one by one to check for duplicates:
if (!waitingList.contains(song.getSongId())) {
If the song does not already exist in the Wait List runtime:
waitingList.append(song);
→ the song will be appended to the end of the playback queue.
During the queue expansion process:
the Wait List runtime remains continuous
the playback session is not interrupted
duplicate songs are removed
the playback engine can continue playing newly appended songs

 
4. Manual Trace
This section provides a rigorous validation of the proposed playback algorithms through manual step-by-step tracing of key operations, including initialization, navigation, and queue manipulation. By simulating the internal state changes across the IndexedDoublyLinkedList, HistoryStack, and DynamicArrayList, these test cases ensure that pointer movements and data consistency align with the design specifications. This logical verification serves as a precursor to formal implementation, guaranteeing that edge cases such as shuffle, repeat modes, and queue expansion are handled robustly. 
4.1. Test Data
Field	Value
Type	Manual test cases.
Songs	Song A, Song B, Song C, Song D, Song E. SongId values are 1, 2, 3, 4, and 5.
Initial List	Song A <-> Song B <-> Song C <-> Song D <-> Song E
Pointers	head = Song A, tail = Song E, currentTrackPointer = Song A after AudioPlayEngine initialization.
 
4.2. Test Cases
TC-01: Initialize Playback From Playlist
Field	Detail
Scenario	Initialize Playback From Playlist
Precondition	Wait List contains Song A <-> Song B <-> Song C <-> Song D <-> Song E. HistoryStack is empty.
Steps	- Select the sample playlist.
- Initialize AudioPlayEngine(waitingList).
Expected	- currentTrackPointer points to waitingList.getHead(), which is Song A.
- head = Song A and tail = Song E.
- HistoryStack is initialized as empty.
- The player starts playing Song A.
 
TC-02: Next Track When a Next Node Exists
Field	Detail
Scenario	Next Track When a Next Node Exists
Precondition	currentTrackPointer = Song A. Repeat One = off. Repeat All = off.
Steps	- Click the Next button once.
Expected	- Song A is pushed into HistoryStack.
- currentTrackPointer moves from Song A to Song B through A.next.
- The Wait List order remains A <-> B <-> C <-> D <-> E.
- The player plays Song B.
 
TC-03: Next at the Last Song With Repeat Off
Field	Detail
Scenario	Next at the Last Song With Repeat Off
Precondition	currentTrackPointer = Song E. Song E.next = null. Repeat All = off. Song E genre = Pop.
Steps	- Click Next while Song E is playing.
Expected	- Song E is pushed into HistoryStack.
- The system detects currentTrackPointer.next == null.
- autoAppendSongs() is triggered.
- Up to 10 Pop songs are loaded from the database.
- Non-duplicate songs are appended to the tail of the Wait List.
- Playback continues with the first newly appended song if available.
 
TC-04: Next at the Last Song With Repeat All On
Field	Detail
Scenario	Next at the Last Song With Repeat All On
Precondition	currentTrackPointer = Song E. Song E.next = null. Repeat All = on.
Steps	- Click Next while Song E is playing.
Expected	- Song E is pushed into HistoryStack.
- currentTrackPointer is reset to waitingList.getHead(), which is Song A.
- Auto-Queue is not triggered.
- The player starts again from Song A.
  
TC-05: Previous After Next Was Used
Field	Detail
Scenario	Previous After Next Was Used
Precondition	The player started at Song A, then Next was clicked to play Song B. HistoryStack top = Song A.
Steps	- Click the Previous button.
Expected	- playbackHistory.pop() returns Song A.
- The player plays Song A again.
- HistoryStack size decreases by one.
- Previous uses actual playback history instead of relying only on currentTrackPointer.prev.
  
TC-06: Shuffle Only Upcoming Songs
Field	Detail
Scenario	Shuffle Only Upcoming Songs
Precondition	Wait List = A <-> B <-> C <-> D <-> E. currentTrackPointer = Song B.
Steps	- Turn on Shuffle.
- Export songs after Song B to DynamicArrayList.
- Apply Fisher-Yates Shuffle and write the shuffled data back to the nodes after Song B.
Expected	- currentTrackPointer remains Song B.
- Song A and Song B keep their current playback positions.
- The upcoming set is still {Song C, Song D, Song E}, but the order may change.
- No song is lost or duplicated.
- The player continues playing Song B.
   
TC-07: Drag Song E From Position 5 to Position 1
Field	Detail
Scenario	Drag Song E From Position 5 to Position 1
Precondition	Wait List = A <-> B <-> C <-> D <-> E. head = A and tail = E.
Steps	- Drag Song E from position 5 to position 1.
- Detach Song E from the old tail position.
- Insert Song E before the old head, Song A.
Expected	- The new order is E <-> A <-> B <-> C <-> D.
- The main pointer updates are D.next = null, E.prev = null, E.next = A, and A.prev = E.
- head becomes Song E.
- tail becomes Song D.
- songIndex still maps each songId to the correct node.
- No node is lost or duplicated.
   
4.3. Note
Field	Detail
Scope	These cases are written based on the Report 2 algorithm design: IndexedDoublyLinkedList, HashMap songIndex, HistoryStack, DynamicArrayList, and AudioPlayEngine.
Implementation	Repeat One and Auto-Queue are included as expected behavior from the algorithm design. If the current code is not fully implemented yet, the actual result can be marked as Failed or Partially Passed during manual execution.
 
 
5. AI vs. Personal Implementation Comparison 
Here is the updated comparison table template tailored exactly to the final requirement of Report 2. It incorporates the core technical challenges your team faced, contrasting the AI's simplified textbook approaches with your team's practical, production-ready implementations.
Feature / Component	AI-Generated Logic / Code Snippet Approach	Personal Implementation / Refinement	Evaluation / Reason for Change
Playlist Editing (Lookup & Remove)	Suggested standard Doubly Linked List (DLL) pointer traversal starting from head to find the target SongID (O(n) time complexity).	Implemented IndexedDoublyLinkedList by integrating a HashMap<Integer, Node> to map SongID directly to memory addresses.	Accepted with Major Modification. The AI's pure DLL approach causes severe bottlenecks during drag-and-drop or deletion. The HashMap upgrade forces these operations into true O(1) constant time.
Shuffle Algorithm	Proposed directly swapping next and prev pointers of nodes inside the Doubly Linked List to randomize the queue.	Exported future nodes to DynamicArrayList, applied Fisher-Yates Shuffle on the array, and wrote only the randomized data back to existing DLL nodes.	Rejected AI Code. Manipulating DLL pointers directly during runtime is highly prone to pointer corruption and UI desync. The array-based data rewrite is safer, unbiased (O(n)), and preserves the active currentTrackPointer.
Previous Track Navigation	Suggested moving the pointer backward using currentTrackPointer = currentTrackPointer.prev.	Ignored the .prev pointer for history routing. Implemented HistoryStack.pop() to retrieve the actual last played song.	Rejected AI Code. The AI failed to account for Shuffle and Auto-Queue runtime state changes. The physical .prev node no longer reflects the true chronological listening history, making the Stack implementation mandatory.
Repeat One Mode	Suggested executing the standard nextTrack() logic, which continuously pushes the currently playing song into the HistoryStack.	Modified nextTrack() to explicitly bypass playbackHistory.push() when repeatOne == true.	Rejected AI Code. The AI's logic would cause "stack pollution" by filling the 30-element history limit with identical tracks, completely breaking the user's rollback capability.
Auto-Queue Expansion	Suggested querying 10 songs by genre and appending them directly to the waitingList, assuming database fetches are inherently unique.	Implemented a strict validation check using !waitingList.contains(song.getSongId()) via the internal HashMap index before appending.	Modified AI Code. The AI lacked real-world context; database queries don't know what is currently in the active RAM queue. The O(1) duplicate check prevents infinite playback loops and queue pollution.

