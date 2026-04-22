# 🎓 w2153589 CSA-CW

---

## 🏗️ Part 1: Service Architecture & Setup

> **Question 1.1:** In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.

### 💡 Answer

By default, the JAX-RS runtime treats Resource classes as **request-scoped**. This means a brand-new instance of the resource class is instantiated for every single incoming HTTP request, and it is immediately garbage-collected after the response is sent back to the client.

This lifecycle design choice has a major impact on how I structured the backend for this Smart Campus API, especially since we are strictly forbidden from using an external database like SQL Server.

If I simply initialized a standard `ArrayList` or `HashMap` as a normal instance variable inside my resource classes, it would be wiped clean on every single request. To ensure data persists across multiple API calls, the data structures must exist outside the request scope. To solve this, I designed a centralized "Mock Database" (Data Access layer) to hold the collections in memory, ensuring all JAX-RS resource instances interact with the exact same shared data pool.

However, sharing in-memory data across multiple concurrent request threads introduces a critical risk: **race conditions**. If Campus System A attempts to add a new sensor reading while Campus System B is concurrently updating that sensor's status, a standard `HashMap` or `ArrayList` would either lose data, cause deadlocks, or throw a `ConcurrentModificationException`.

To prevent data corruption and ensure thread safety, I implemented the following synchronization strategies for my in-memory models:

* **Thread-Safe Collections:** Instead of standard `HashMap`, I utilized `ConcurrentHashMap`. This allows multiple threads to read and write concurrently without locking the entire map, maintaining high API performance while strictly guaranteeing data integrity.
* **Thread-Safe Lists:** For the lists of sensor IDs and historical readings, I used synchronized collections to ensure safe iteration and modification by concurrent requests.
* **Atomic Operations:** I relied on atomic methods (like `putIfAbsent()`) to ensure that multi-step validations (such as checking if a Room ID exists before inserting a new one) are executed as a single, uninterruptible operation.

---

> **Question 1.2:** Why is the provision of "Hypermedia" (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

### 💡 Answer

The provision of Hypermedia, formally known as **HATEOAS** (Hypermedia as the Engine of Application State), is considered the highest level of advanced RESTful design (Level 3 of the Richardson Maturity Model). It transforms an API from a simple data dictionary into a dynamic, self-documenting state machine.

This approach offers massive benefits to client developers compared to relying solely on static documentation (like PDFs or Swagger files):

* **Decoupling and Resilience:** Without HATEOAS, client applications must hardcode API URLs (e.g., `fetch("http://api.university.edu/api/v1/rooms")`). If the backend team decides to change the routing structure later, every client application breaks. With HATEOAS, the client simply navigates to the root API and follows the provided `_links` dynamically. The server can change URIs at any time without breaking the frontend.
* **Discoverability:** Just like a human navigates a website by clicking visible links rather than guessing URLs, a HATEOAS-compliant API guides the client. The client discovers what actions are currently available based on the links provided in the response payload.
* **Reduced Developer Friction:** Client developers do not need to constantly cross-reference external static documentation to figure out how to access related resources. The API tells them exactly where to go next directly within the JSON response, significantly speeding up frontend development and integration.
