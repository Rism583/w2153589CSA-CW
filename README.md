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

## 🚪 Part 2: Room Management

> **Question 2.1:** When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.

### 💡 Answer

The decision between returning just IDs or full objects comes down to a trade-off between initial bandwidth and the "N+1 query problem."

If I were to return only a list of Room IDs (e.g., `["LIB-301", "LEC-102"]`), the initial payload would be very small, conserving initial network bandwidth. However, this shifts a massive burden onto the client side. If the frontend campus dashboard needs to actually display the room names and capacities to the user, the client application would have to make one request to get the list of IDs, and then *N* additional asynchronous GET requests to fetch the details for every single room. This creates significant network latency overhead and complicates client-side processing.

In my implementation, I chose to return the **full room objects**. While this slightly increases the size of the initial JSON payload (using more bandwidth per request), it drastically improves overall efficiency. The client receives all the necessary rendering data in a single HTTP round-trip. This simplifies the frontend logic, reduces the total number of connections the server has to handle, and makes the application interface feel much faster and more responsive for the end user.

---

> **Question 2.2:** Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

### 💡 Answer

Yes, the `DELETE /{roomId}` operation in my `SensorRoomResource` is strictly **idempotent**. 

In RESTful API design, an operation is considered idempotent if sending the exact same request multiple times leaves the server's state exactly the same as if the request had only been executed once. 

Here is exactly what happens in my code if a client mistakenly double-clicks a delete button and sends the same DELETE request for "LIB-301" three times in a row:

* **Request 1:** The server finds "LIB-301" in the `MockDatabase`. It verifies the business logic constraint (ensuring the `sensorIds` list is empty), safely deletes the room from the `ConcurrentHashMap`, and returns a `204 No Content` status. The server state has now changed.
* **Requests 2 & 3:** When these duplicate requests arrive, the server searches the database for "LIB-301" and finds nothing. My code explicitly catches this `null` result and returns a `404 Not Found` error. 

Even though the HTTP response code sent back to the client changes (from a 204 success to a 404 error), the actual *data state on the server* does not change after the first successful request. No accidental deletions occur, the database doesn't crash, and no orphan data is created. Because the server state remains completely stable and unchanged during repeated identical requests, the operation perfectly satisfies the requirement of idempotency.

---

## 📡 Part 3: Sensor Operations & Linking

> **Question 3.1:** We explicitly use the `@Consumes(MediaType.APPLICATION_JSON)` annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as `text/plain` or `application/xml`. How does JAX-RS handle this mismatch?

### 💡 Answer

By adding `@Consumes(MediaType.APPLICATION_JSON)` to my POST method, I am establishing a strict "contract" between the client and my server regarding the expected data format. 

If a client ignores this contract and attempts to send a payload formatted as `text/plain` or `application/xml`, the JAX-RS runtime acts as a protective shield. It intercepts the incoming HTTP request *before* it even reaches my `registerSensor` Java method. 

Because the `Content-Type` header of the client's request does not match the `@Consumes` annotation, JAX-RS will automatically reject the request and return an **HTTP 415 Unsupported Media Type** error to the client. 

This architectural feature has two major technical consequences:
1. **Application Stability:** It prevents my Java code from throwing ugly, unhandled parsing exceptions (like trying to force XML data into a Jackson JSON parser). 
2. **Separation of Concerns:** The framework handles the repetitive content-negotiation and validation logic automatically, allowing my resource classes to focus purely on the actual business logic of registering the sensor.

---

> **Question 3.2:** You implemented this filtering using `@QueryParam`. Contrast this with an alternative design where the type is part of the URL path (e.g., `/api/v1/sensors/type/CO2`). Why is the query parameter approach generally considered superior for filtering and searching collections?

### 💡 Answer

While both approaches technically work to return filtered data, using `@QueryParam` is significantly superior because it adheres much closer to core RESTful design principles.

In REST, the **URL Path** should be used strictly to identify resources or sub-resources. For example, `/sensors/SENS-001` clearly identifies a specific, unique entity. If I used `/sensors/type/CO2`, I am incorrectly treating "type" and "CO2" as hierarchical sub-folders, which makes the API rigid. 

Conversely, **Query Parameters** (e.g., `/sensors?type=CO2`) are designed specifically for modifying, sorting, or filtering an established collection. This approach is superior for several reasons:

* **Optionality:** Query parameters are inherently optional. The base URL `/sensors` still makes perfect sense on its own (returning all sensors), while `?type=CO2` simply modifies that base request. Path parameters, however, are usually mandatory, forcing you to create multiple overlapping endpoint methods.
* **Scalability for Complex Searches:** If the university later wants to filter by multiple criteria—such as CO2 sensors that are currently active—query parameters handle this elegantly (`/sensors?type=CO2&status=active`). Trying to build complex, multi-variable searches using path parameters quickly devolves into long, unreadable, and brittle URLs (like `/sensors/type/CO2/status/active`). 
* **Standardization:** It follows industry-standard conventions, making the API more intuitive for frontend developers to consume without having to constantly check the documentation.

---

## 📊 Part 4: Historical Data & Sub-Resource Routing

> **Question 4.1:** Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., `sensors/{id}/readings/{rid}`) in one massive controller class?

### 💡 Answer

During the development of this API, I realized that routing every single endpoint through one controller is a major architectural flaw. If I had defined every nested path (like `/sensors/{id}/readings` and `/sensors/{id}/readings/{rid}`) directly inside my `SensorResource` class, it would have quickly bloated into an unmaintainable "God Class" that tries to handle too many responsibilities.

By implementing the **Sub-Resource Locator pattern**, I structured the API in a much smarter way and gained several key benefits:

1. **Separation of Concerns (Single Responsibility):** My `SensorResource` class is now strictly responsible for managing the root sensor objects. As soon as a request asks for a specific sensor's history, it immediately delegates the job to `SensorReadingResource`. Each class has one clear, focused job.
2. **Reduced Boilerplate Code:** Because my locator method extracts the `{sensorId}` from the URL and passes it directly into the constructor of `SensorReadingResource`, the new class inherently "knows" which sensor it is working with. I don't have to redundantly add `@PathParam("sensorId")` to every single method inside the historical controller, which makes the code much cleaner.
3. **Team Scalability:** If the university decides later that they want to add even deeper nested paths (for example, `/sensors/{id}/readings/{rid}/calibrations`), I can just chain another sub-resource locator. This modular approach means a team of developers could work on different sub-resources at the same time without constantly causing Git merge conflicts in one massive file.

---

## 🚦 Part 5: Exception Handling & Observability

> **Question 5.2:** Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

### 💡 Answer

When I initially started building the validation logic, my first instinct was to return an `HTTP 404 Not Found` or an `HTTP 400 Bad Request` if a sensor tried to connect to a non-existent room. However, after looking deeper into REST API standards, I realized that `HTTP 422 Unprocessable Entity` is far more precise for this specific scenario.

Here is my reasoning for this architectural choice:
* **Why not 400 Bad Request?** A 400 status generally implies that the client sent malformed syntax (like missing a comma or a bracket in the JSON). In this scenario, the client's JSON is perfectly formatted, so a 400 is technically misleading.
* **Why not 404 Not Found?** A 404 implies that the target URL itself does not exist. But the client is sending a POST request to `/api/v1/sensors`, which is a perfectly valid and active endpoint. Returning a 404 would confuse the client into thinking the sensor endpoint is broken or misspelled.
* **Why 422 is perfect:** HTTP 422 explicitly states that "the server understands the content type of the request entity, and the syntax of the request entity is correct, but was unable to process the contained instructions." This perfectly describes a foreign-key or dependency violation. The server successfully parsed the JSON body, but it cannot process my business logic because the `roomId` inside the payload references a dependency that does not exist in the system.

---

> **Question 5.4:** From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

### 💡 Answer

Allowing a framework like GlassFish to leak raw Java stack traces to the client is a severe security vulnerability known as **Information Disclosure**. While a stack trace is helpful for a developer debugging locally, it acts as a roadmap for a malicious attacker.

If my API returned a raw stack trace, an attacker could gather the following critical intelligence during the reconnaissance phase of a cyberattack:
1. **Underlying Technologies & Versions:** The trace reveals exactly what libraries the server is using (e.g., Jackson, Jersey, GlassFish). If an attacker sees an outdated library version in the stack trace, they can easily look up known CVEs (Common Vulnerabilities and Exposures) to exploit it.
2. **Database Schema Details:** If a database query fails, the stack trace often leaks the exact SQL syntax, table names, and column names, making the API highly susceptible to targeted SQL Injection attacks.
3. **Internal File Paths:** Stack traces often print the exact directory structure of the server (e.g., `C:/glassfish7/domains/domain1/applications/...`). This gives the attacker a map of the file system, aiding in directory traversal or local file inclusion attacks.

By implementing a `GlobalExceptionMapper` that catches `Throwable`, I ensure that the attacker only ever receives a sanitized `500 Internal Server Error` message, effectively blinding them to the backend architecture.

---

> **Question 5.5:** Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting `Logger.info()` statements inside every single resource method?

### 💡 Answer

Implementing logging via JAX-RS Filters is a textbook example of handling "cross-cutting concerns" (logic that affects the entire application) without cluttering the core architecture.

There are three major architectural advantages to this approach:
1. **DRY Principle (Don't Repeat Yourself):** If I manually placed `Logger.info()` in every controller method, I would have to write the exact same boilerplate code dozens of times. A filter writes the code once and automatically applies it to every endpoint globally.
2. **Separation of Concerns:** A controller's only job should be handling business logic (e.g., registering a sensor or deleting a room). By moving logging out of the controller and into a `@Provider` filter, the resource classes remain strictly focused on their primary objectives.
3. **Guaranteed Execution (Fail-Safe):** If I or another developer adds a new API endpoint in the future and forgets to manually type a logging statement, that endpoint goes completely unmonitored. A global JAX-RS filter acts as an invisible safety net, ensuring *every* request and response is strictly logged at the server level, even for routes that haven't been built yet.

---
