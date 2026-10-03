# Talk outline — EclipseStore as Vector DB & Graph DB replacement

> Working doc. Edit freely, then hand this file to Claude (or another model) with:
> "turn this into slides" — one `##` heading = one slide, `>` lines are speaker
> notes, `[IMAGE]` / `[DEMO]` / `[SCREENSHOT]` / `[DIAGRAM]` are placeholders for visuals.
>
> **Core message of the talk:** next to using EclipseStore as your plain
> persistence layer (instead of SQL/NoSQL), you can use it **directly as a
> vector database** — and, even more interestingly, as a **replacement for a
> graph database / knowledge graph**. Not because EclipseStore *is* a graph
> database (it isn't — no Cypher, no graph query engine), but because you
> can expose MCP functions that start at the root aggregate (`Hotel`) and
> follow live object references out to `Guest`s, `Booking`s, `Room`s,
> `Payment`s — fast, root-anchored traversal instead of a declarative graph
> query. The killer benefit: when an entity is saved, that change is
> reflected **immediately** on the next traversal — no knowledge-graph
> refresh/reindex job, ever.
>
> **Demo project:** `sb-hotel` — a Spring Boot hotel booking app persisted
> with EclipseStore, with an MCP (Model Context Protocol) layer exposing
> guest/booking/payment tools to an LLM chat client (`eclipsestore-client`).
> It's an intentionally small/dummy project built for this talk.
>
> **Assumed slot:** ~35–40 min conference talk, ~20–24 slides. Adjust the
> `[TIMING]` markers if your slot differs.

---

## 1. Title slide

**Beyond Rows and Documents**
EclipseStore as a Vector Database — and a Graph Database Killer

> Speaker notes: One-line hook while people settle in — "What if the database
> you already use for everyday persistence could also be your vector DB and
> your graph DB — with zero extra infrastructure?"

[TIMING] 0:30

---

## 2. Who am I

- Miki

> Speaker notes: Keep this under 30 seconds. Conference audiences want you to
> get to the point.

[TIMING] 0:30

---

## 3. The problem: polyglot persistence tax

- A "modern" app today often needs **three different databases**:
  - A system of record (Postgres, MongoDB, ...)
  - A vector database for similarity/semantic search (Pinecone, pgvector, Qdrant, ...)
  - A graph database for relationship-heavy queries (Neo4j, ...)
- Each one means: a new client, a new query language, a new ops burden, and
  **data duplicated and kept in sync across all three**
- The sync problem is the real tax: ETL jobs, eventual consistency bugs,
  "which copy is correct?"

> Speaker notes: This is the pain everyone in the room has felt. Don't over-
> explain — a knowing nod from the audience is the goal here, not a lecture.

[TIMING] 1:30

---

## 4. The thesis

> **Next to plain persistence, EclipseStore can BE your vector database.**
> **And root-anchored traversal of its live object graph can replace your
> graph database / knowledge graph outright — no separate system, no sync,
> and no refresh job, because there's no derived copy to go stale.**

- One storage engine. Three jobs.
- Today's talk: prove it with a running hotel-booking app and an AI agent
  that queries it via MCP tools.

> Speaker notes: State the thesis plainly and move on — this slide is the
> anchor you'll refer back to at the end.

[TIMING] 1:00

---

## 5. 60-second primer: what is EclipseStore

- A **native Java object graph persistence** engine — no ORM, no mapping
  layer, no SQL
- You store your actual domain objects (`Guest`, `Booking`, `Room`, ...) as-is
- Lazy-loading subgraphs means you work with a graph far bigger than RAM, but
  navigate it with plain Java field/method access
- No impedance mismatch: the object model *is* the persistence model

> Speaker notes: Keep this tight if talk audience already knows EclipseStore;
> expand if this is a mixed/beginner audience. Consider cutting to 3 bullets
> if short on time.

[TIMING] 1:30

---

## 6. Meet the demo: `sb-hotel`

- Spring Boot hotel booking application
- Domain: `Hotel` → `Guest`, `Booking`, `Room`, `Payment`
- Persisted entirely with EclipseStore (no SQL database anywhere)
- Seeded with realistic dummy data: 100 rooms, 200 guests, 500 bookings,
  payments
- Exposes its domain to an LLM agent via **MCP tools** (`eclipsestore-client`
  is the chat front-end)

[DIAGRAM] High-level box diagram: sb-hotel (EclipseStore) ←→ MCP tools ←→ LLM
chat client

> Speaker notes: This is the app you'll demo from for the rest of the talk —
> get people oriented now so later demos need no re-explaining.

[TIMING] 1:30

---

## 7. Baseline: EclipseStore as "boring" persistence

- `Hotel` is the storage root; `Guest`/`Booking`/`Room`/`Payment` hang off it
  as plain Java objects and collections
- Relationships are just **object references** — a `Booking` holds its
  `Guest`s and `Room` directly, no foreign keys
- No repositories full of hand-written SQL, no JPA annotations, no N+1 query
  surprises
- [DEMO or SCREENSHOT] quick look at the domain model / a REST call creating
  a booking

> Speaker notes: This slide sets up the "already a graph" reveal later — plant
> the idea that relationships are just references, don't spell out the
> payoff yet.

[TIMING] 1:30

---

## 8. Act 1 — Vector DB: the problem

- Business question: *"Who are this guest's likely travel companions —
  family members, repeat co-travelers?"*
- Classic answer: stand up a vector DB, embed guest profiles, keep it in sync
  with the source of truth
- Our answer: **index guests inside EclipseStore itself**, using GigaMap's
  vector index

> Speaker notes: Frame this as a question a hotel concierge / revenue team
> would actually ask — makes the demo feel grounded, not academic.

[TIMING] 1:00

---

## 9. GigaMap + VectorIndex — how it works

- `GigaMap<Guest>` already holds every guest; we attach a **vector index
  category** to it
- A `Vectorizer<Guest>` turns a guest into a fixed-size `float[]` — no ML
  model, no external embedding API call
- The index is registered once, lives next to the data it indexes, and is
  queried in-process

```java
VectorIndexConfiguration configuration = VectorIndexConfiguration
        .builderForMediumDataset(GuestVectorizer.DIMENSION)
        .similarityFunction(VectorSimilarityFunction.COSINE)
        .build();

this.index = vectorIndices.ensure(INDEX_NAME, configuration, new GuestVectorizer());
```

[SCREENSHOT] `GuestSearchService.java`

> Speaker notes: Emphasize "no ML model, no external API call" — this is a
> deterministic, explainable feature vector, which matters for a conference
> audience skeptical of "just add embeddings" hand-waving.

[TIMING] 2:00

---

## 10. The vectorizer: deterministic, explainable features

- 64-dimensional vector built from: normalized age, "is the booker" flag,
  postal code, **hashed character trigrams of city/state**, **hashed
  trigrams of last name**
- Each block is L2-normalized so no single feature dominates cosine
  similarity
- Guests end up close together when they're plausibly family members or
  repeat travel companions — same last name, same hometown, similar age
- No GPU, no embedding model, no network call — pure Java, reproducible
  results every time

> Speaker notes: This is a deliberate design choice worth calling out
> explicitly: you don't need an LLM embedding model to get useful vector
> search. For many domain-specific problems, a hand-crafted feature vector
> is faster, cheaper, and fully explainable.

[TIMING] 1:30

---

## 11. Vector search, live

- MCP tools exposed to the chat agent:
  - `findSimilarGuests(guestId, topK)` — "who's similar to *this* guest?"
  - `searchGuestsByProfile(age, city, lastName, ...)` — "find guests like
    *this description*"
- [DEMO] Ask the chat UI: *"Find guests similar to this 34-year-old guest
  from Đakovo named Newman"* and watch it call `searchGuestsByProfile`
- [SCREENSHOT] chat UI showing the MCP tool call badge + results

> Speaker notes: This is your first live-demo moment. Have a backup
> screenshot/recording ready in case of network/demo gremlins.

[TIMING] 2:30

---

## 12. Act 1 takeaway

- **No separate vector database.** No Pinecone account, no pgvector
  extension, no sync job.
- The index lives in the same JVM, same transaction boundary, same backup,
  as the data it indexes
- Good fit when: your "vector search" is really "structured similarity
  search" over domain attributes you already understand

> Speaker notes: Be honest here that this isn't "replace Pinecone for RAG
> over PDFs" — it's "replace an extra moving part for domain-object
> similarity search." Sets up the honest trade-offs slide later.

[TIMING] 1:00

---

## 13. Act 2 — Graph DB: the problem

- Business questions that are naturally *graph* questions:
  - "Which bookings in March had the most guests?"
  - "Which bookings are still unpaid, and who booked them?"
  - "Show me the guest, their booking, their room, and the payment status —
    together"
- Classic answer: model guests/bookings/rooms/payments as nodes and edges in
  Neo4j (or similar) — or, in the AI-agent world, build a **knowledge graph**
  the agent retrieves from (GraphRAG-style)
- Either way: a second system, a second model of your data, and **a sync /
  refresh problem** on top of the first one from Act 1

[DIAGRAM]/Insert an image that explains how complicated building a knowledge graph is (see LightRAG Youtube video)

> Speaker notes: Call out that this is the *second* time "keep it in sync"
> shows up — that repetition is the point of the whole talk.

[TIMING] 1:00

---

## 14. To be precise: EclipseStore is not a graph database

- No Cypher, no Gremlin, no graph query planner, no pattern-matching engine
  — **EclipseStore does not claim to be a graph database**
- What it *does* have: a native Java object graph, where every relationship
  (`Booking` → `Guest`s, `Booking` → `Room`, `Payment` → `Booking`) is a
  **direct, lazy-loaded object reference** — already there, already
  persisted, the moment you modeled your domain in Java
- The approach: **don't build a graph query engine — build root-anchored
  traversal functions.** Each MCP tool starts at the aggregate root
  (`Hotel`) and walks a specific, known set of references outward to fetch
  exactly the slice an agent needs

[DIAGRAM] `Hotel` at the center, arrows fanning out to `Guest` → `Booking` →
`Room`/`Payment`, labeled "object references, walked by hand-written MCP
functions — not a declarative graph query"

> Speaker notes: This is the honesty slide — say explicitly "this is not a
> graph database" before making the stronger claim on slide 16. Claiming
> less here is what makes the audience trust the bigger claim later.

[TIMING] 2:00

---

## 15. Graph traversal, live

- MCP tools, each a root-anchored fetch starting from `Hotel`:
  - `getBookings(from, to)` — `Hotel` → `Booking`s → `Guest`/`Room`, returns
    room name, guest names/count, dates, price, payment status in one shot
  - `getPayments(from, to)` — `Hotel` → `Payment`s → `Booking` → `Guest`/`Room`
- Performance: this is **in-heap reference chasing with lazy loading**, not
  a query planner doing joins — O(hops you actually take), no query
  compilation, no index lookup needed just to walk a relationship
- [DEMO] Ask the chat UI: *"Which bookings in October are still unpaid, and
  how many guests are on each?"*
- [SCREENSHOT] chat UI showing the MCP tool call + the multi-entity answer

> Speaker notes: Point out explicitly: there is no join happening at query
> time — the data was always structured this way, we're just reading it by
> following references the JVM already knows how to chase fast.

[TIMING] 2:30

---

## 16. The strongest claim: no refresh, ever

- A knowledge graph (GraphRAG-style or a dedicated graph DB fed from your
  real data) is a **derived, secondary copy** — it goes stale the moment
  the source data changes, until some job rebuilds or refreshes it
- Here there is no derived copy: the "graph" an MCP tool traverses *is* the
  live transactional object graph. The instant an entity is saved, the very
  next traversal sees it
- **Zero staleness window. No refresh job. No re-indexing. No re-embedding
  of graph structure.** Not "fast refresh" — no refresh step at all
- That's the headline takeaway: **EclipseStore doesn't give you a better
  graph database — it removes the need for a second, derived graph
  altogether, by making root-anchored traversal of your live data fast
  enough to query directly.**

> Speaker notes: Say this one slowly. This is the line people should tweet.
> Consider isolating "zero staleness window — no refresh job, ever" on its
> own line, large text.

[TIMING] 1:30

---

## 17. Head-to-head: vs. GraphRAG

- GraphRAG solves a genuinely different, harder problem: turning
  **unstructured text** (documents, emails, reports) into a graph, by
  having an LLM extract entities and relationships from it
- That's real, valuable work when you have no structured schema — but it
  comes with a pipeline: extraction → entity resolution → community
  summarization → indexing, usually re-run as a **batch job** whenever
  source documents change

| | GraphRAG | Root-anchored traversal (this talk) |
|---|---|---|
| Where the graph comes from | LLM-extracted from unstructured text | Already explicit — it's your domain model |
| How "edges" are found | Probabilistic extraction (can hallucinate or miss relationships) | Deterministic — a `Booking` really does reference this `Guest` |
| Freshness | Stale until the next extraction/indexing run | Always current — reads the live transactional graph |
| Cost | LLM calls at index time (scales with corpus) + at query time | No extraction cost; traversal is just object references |
| Query mechanism | Vector search over graph/community summaries, then LLM reasoning | Typed, hand-written MCP functions walking known references |
| Best fit | Unstructured knowledge with no existing schema | Structured operational data you already model as objects |

- The honest framing: **we're not claiming to out-perform GraphRAG at its
  own job** (making sense of unstructured text). We're claiming that if
  your data is *already* structured — an OLTP domain model like `sb-hotel`
  — running it through an extract-and-refresh pipeline solves a problem you
  don't have

[DIAGRAM] Side-by-side pipelines: GraphRAG's "Docs → LLM extraction →
Graph store → (refresh on change)" vs. this approach's "Domain objects →
(nothing) → MCP traversal, always live"

> Speaker notes: This is the slide for the audience members who've just sat
> through three GraphRAG talks this year. Land the distinction precisely —
> different problem, not a strictly better solution to the same one. If
> you have the LightRAG/GraphRAG pipeline-complexity image from slide 13,
> consider echoing it here for contrast.

[TIMING] 2:00

---

## 18. Architecture recap

[DIAGRAM] One box labeled "EclipseStore (single JVM heap + on-disk store)"
with three labeled capabilities radiating out of it:
  - "System of record" (plain object persistence)
  - "Vector DB" (GigaMap VectorIndex)
  - "Root-anchored graph traversal" (MCP functions walking live object
    references from `Hotel` — not a graph query engine)
with a crossed-out row below showing the "old way": 3 separate boxes
(Postgres / Pinecone / Neo4j-or-a-refreshed-knowledge-graph) connected by
dashed "sync / refresh" arrows

> Speaker notes: This single image is the one slide people will screenshot —
> make sure the diagram is clean and readable from the back of the room.

[TIMING] 1:00

---

## 19. Honest trade-offs (don't skip this)

- This is a single-JVM, heap-resident model — it scales differently than a
  horizontally-sharded graph/vector DB cluster
- GigaMap's vector index is great for domain-object similarity search; a
  dedicated vector DB may still win for massive, high-churn embedding
  workloads (e.g. RAG over millions of documents)
- No Cypher/Gremlin, no declarative graph query language — every traversal
  is a hand-written MCP function starting from a known root. Flexible,
  type-safe, fast — but someone has to write a new function for a
  genuinely new traversal shape, there's no ad-hoc pattern matching
- Best fit: **root-anchored lookups over domain-shaped data you already
  model as objects** (fetch-the-relevant-slice-from-`Hotel`) — not
  arbitrary, open-ended graph exploration across unknown paths

> Speaker notes: Credibility slide. A talk that only claims wins reads as
> marketing; naming the limits is what makes the big claim on slide 16
> believable.

[TIMING] 1:30

---

## 20. The MCP layer: why it matters for this demo

- MCP tools are how the LLM chat agent queries both the vector index and the
  graph — `findSimilarGuests`, `searchGuestsByProfile`, `getBookingKnowledge`,
  `getGuestKnowledge`, `getPayments`, `getBookings`
- The chat UI shows **which MCP tool was called** for each answer — makes
  the "vector search" vs "graph traversal" distinction visible to the
  audience in real time
- [SCREENSHOT] chat UI with tool-call badges visible

> Speaker notes: This slide is optional if you've already shown tool badges
> in the earlier demo screenshots — merge or cut if short on time.
> [OPTIONAL SLIDE]

[TIMING] 1:00

---

## 21. Demo time!

- [IMAGE]/insert a image: showing a graph. emotional, modern, AI attributes. 

> Speaker notes: Opening the IDE to start apps and the browser

[TIMING] 1:00

---

## 22. Recap

- EclipseStore as plain persistence: no ORM, no SQL, your objects as-is
- EclipseStore as a **vector database**: GigaMap vector index, no external
  service, deterministic feature vectors
- EclipseStore as a **graph database / knowledge graph replacement**:
  root-anchored MCP functions walk live object references — no re-modeling,
  no sync, and critically **no refresh job, ever**
- One engine, one transaction boundary, one mental model

> Speaker notes: Read the thesis from slide 4 again here almost verbatim —
> bookending reinforces retention.

[TIMING] 1:00

---

## 23. Try this yourself

- Add a `VectorIndex` to a `GigaMap` you already have — you likely don't
  need an embedding model, a hand-rolled feature vector may be enough
- Before standing up a graph database or a refreshed knowledge graph, ask:
  *"Is this relationship already a reference in my object model, and can I
  write a root-anchored MCP function to fetch it?"* — if yes, you may not
  need a second, derived graph at all
- [Repo link — TODO]
- [EclipseStore docs link — TODO]
- [GigaMap docs link — TODO]

> Speaker notes: Call-to-action slide — leave it up during Q&A so people can
> photograph the links.

[TIMING] 1:00

---

## 24. Thank you / Q&A

- [Your contact info / socials — TODO]
- [Repo link again — TODO]

> Speaker notes: Leave 5+ minutes for Q&A if your slot allows — this topic
> tends to draw "but what about scale / consistency / X database" questions.

[TIMING] 0:30 + Q&A buffer

---

## Appendix (cut from main deck, keep handy for Q&A)

- **"Why not just use pgvector + a graph extension?"** — valid alternative;
  the point isn't "never use a dedicated DB," it's "you may not need one as
  early as you think, especially for domain-shaped data already living as
  Java objects."
- **"What about write-heavy / high-concurrency workloads?"** — out of scope
  for this talk; worth a follow-up deep dive on EclipseStore's storage
  concurrency model.
- **"Does this work for unstructured knowledge graphs (e.g. documents,
  entities extracted by an LLM)?"** — not what's demoed here; the graph
  claim is specifically about domain object graphs you already model in
  code, not arbitrary extracted knowledge graphs.
- **"Isn't a knowledge graph's refresh job just an engineering detail you
  can optimize away too?"** — in principle yes, but it's an entire
  subsystem (extraction job, scheduling, staleness monitoring) that this
  approach deletes outright rather than speeds up. There is no job to
  schedule because there is no second copy of the data to keep warm.
- **"What if I need to query a relationship I didn't anticipate?"** — fair
  limit: a hand-written MCP function only answers the traversal shapes you
  wrote. A declarative graph query language lets you ask new questions
  without new code; here, a genuinely new traversal shape needs a new
  (small) function. Worth naming as a real trade-off if asked.
- Code references used in this talk (for your own navigation while
  rehearsing):
  - `sb-hotel/src/main/java/.../guest/search/GuestSearchService.java`
  - `sb-hotel/src/main/java/.../guest/search/GuestVectorizer.java`
  - `sb-hotel/src/main/java/.../guest/api/mcp/GuestMcpTools.java`
  - `sb-hotel/src/main/java/.../booking/api/mcp/BookingMcpTools.java`
  - `sb-hotel/src/main/java/.../payment/api/mcp/PaymentMcpTools.java`
