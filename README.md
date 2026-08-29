# InfoRetrieve — Custom Java Search Engine & Recommendation System

**InfoRetrieve** is an advanced custom search engine with an integrated dual-recommendation system, built **entirely from scratch** using core Java and data structures (without external ML or search libraries like Lucene or Elasticsearch).

- 🔗 **GitHub Repository**: [Custom-Search-Engine-Recommendation-System-in-Java](https://github.com/Nithya-146/Custom-Search-Engine-Recommendation-System-in-Java)
- 🌐 **Web Dashboard Interface**: [http://localhost:8080](http://localhost:8080)

---

## 🏛️ System Architecture

```mermaid
graph TD
    Client[Web UI Dashboard / CLI Client] -->|HTTP / JSON| API[SearchController REST API]
    API --> QP[QueryProcessor]
    API --> RecService[RecommendationService]
    
    subgraph Core Indexing Engine
        DocIngest[Document Ingestion] --> Tokenizer[Tokenizer & Stopword Filter]
        Tokenizer --> Stemmer[Porter Stemmer Algorithm]
        Stemmer --> InvIndex[InvertedIndex: HashMap<String, List<Posting>>]
        InvIndex --> Storage[IndexSerializer Disk Persistence]
        InvIndex --> Trie[Autocomplete Trie O(k)]
    end
    
    subgraph Ranking & Query Processing
        QP --> BoolEval[BooleanQueryEvaluator: AND / OR / NOT Sets]
        QP --> PhraseMatch[PhraseQueryMatcher: Positional Index]
        QP --> FuzzyMatch[LevenshteinDistance: Dynamic Programming]
        QP --> TfIdf[TfIdfCalculator]
        TfIdf --> CosSim[CosineSimilarity Vector Model]
    end
    
    subgraph Dual Recommendation Engine
        RecService --> ContentRec[ContentBasedRecommender: TF-IDF Cosine Vector Space]
        RecService --> CollabRec[CollaborativeFilteringRecommender: Pearson Correlation Matrix]
    end
    
    subgraph Evaluation Suite
        Bench[EvaluationBenchmark] -->|Precision@K, Recall@K, MAP, Latency| QP
    end
```

---

## 🔑 Key Features & Design Decisions

### 1. Document Ingestion & Text Normalization Pipeline
- **Custom Tokenizer**: Normalizes characters, removes punctuation, and filters standard English stopwords.
- **Porter Stemming Algorithm (`PorterStemmer.java`)**: Reduces words to root morphological forms (e.g. `"connecting"` $\rightarrow$ `"connect"`, `"retrieval"` $\rightarrow$ `"retriev"`).

### 2. Core Inverted Index Data Structure (`InvertedIndex.java`)
- **`HashMap<String, List<Posting>>` vs `TreeMap`**:
  - `HashMap` (and `ConcurrentHashMap` for multithreaded indexing) was selected over `TreeMap` because term lookup is **$O(1)$ expected time** rather than $O(\log V)$ ($V$ = vocabulary size).
  - For prefix autocomplete and range lookup, a separate **Trie** structure (`AutocompleteTrie.java`) is populated in parallel.
- **Posting Structure**: Stores `docId`, `termFrequency`, and exact word `positions` (`List<Integer>`) for phrase matching.
- **Incremental Indexing**: New documents can be added on-the-fly (`addDocument`) without rebuilding the index.
- **Disk Persistence**: `IndexSerializer` serializes the inverted index and document catalog to disk (`data/index_cache.dat`), skipping index rebuilds across restarts.

### 3. Vector Space Ranking & Scoring Engine
- **Logarithmic TF**: $TF(t, d) = 1 + \log_{10}(tf)$ if $tf > 0$, else $0$.
- **IDF Weighting**: $IDF(t) = \log_{10}\left(1 + \frac{N}{df(t)}\right)$.
- **Cosine Similarity Scoring**: Computes dot product over vector L2 norms:
  $$\text{CosineSimilarity}(\vec{u}, \vec{v}) = \frac{\sum_{t} u_t \times v_t}{\|\vec{u}\|_2 \|\vec{v}\|_2}$$

### 4. Advanced Search Features
- **Boolean Queries (`BooleanQueryEvaluator.java`)**: Evaluates `AND` (set intersection), `OR` (set union), and `NOT` (set difference).
- **Exact Phrase Matching (`PhraseQueryMatcher.java`)**: Uses positional offset verification ($pos_{i+1} = pos_i + 1$).
- **Typo Tolerance & Fuzzy Search (`LevenshteinDistance.java`)**: Dynamic programming edit distance algorithm falling back when zero exact hits occur.
- **Autocomplete (`AutocompleteTrie.java`)**: Prefix trie yielding top suggestions sorted by frequency.

### 5. Recommendation Engine
- **Content-Based Filtering (`ContentBasedRecommender.java`)**: Recommends top-$K$ content-similar documents based on TF-IDF Cosine Similarity.
- **Collaborative Filtering (`CollaborativeFilteringRecommender.java`)**: Manual implementation of User-Based Collaborative Filtering using **Pearson Correlation Coefficient**:
  $$r(u, v) = \frac{\sum_{i \in I_{uv}} (R_{u,i} - \bar{R}_u)(R_{v,i} - \bar{R}_v)}{\sqrt{\sum_{i \in I_{uv}} (R_{u,i} - \bar{R}_u)^2} \sqrt{\sum_{i \in I_{uv}} (R_{v,i} - \bar{R}_v)^2}}$$

### 6. Benchmarking & Evaluation Suite (`EvaluationBenchmark.java`)
Calculates Information Retrieval performance metrics across ground-truth test queries:
- **Precision@K**, **Recall@K**, **F1-Score**, **MAP (Mean Average Precision)**, and **Query Latency (ms)**.

---

## 🛠️ Project Structure

```
d:/Custom Search Engine + Recommendation System in Java
├── pom.xml
├── README.md
├── src
│   ├── main
│   │   ├── java/com/inforetrieve
│   │   │   ├── InfoRetrieveApplication.java
│   │   │   ├── api/SearchController.java
│   │   │   ├── cli/InteractiveCLI.java
│   │   │   ├── eval/EvaluationBenchmark.java
│   │   │   ├── indexer
│   │   │   │   ├── Document.java
│   │   │   │   ├── Posting.java
│   │   │   │   ├── PorterStemmer.java
│   │   │   │   ├── Tokenizer.java
│   │   │   │   ├── InvertedIndex.java
│   │   │   │   ├── ConcurrentDocumentIndexer.java
│   │   │   │   └── IndexSerializer.java
│   │   │   ├── query
│   │   │   │   ├── AutocompleteTrie.java
│   │   │   │   ├── BooleanQueryEvaluator.java
│   │   │   │   ├── PhraseQueryMatcher.java
│   │   │   │   ├── QueryProcessor.java
│   │   │   │   └── SearchResult.java
│   │   │   ├── ranking
│   │   │   │   ├── TfIdfCalculator.java
│   │   │   │   ├── CosineSimilarity.java
│   │   │   │   └── LevenshteinDistance.java
│   │   │   └── recommender
│   │   │       ├── ContentBasedRecommender.java
│   │   │       ├── CollaborativeFilteringRecommender.java
│   │   │       └── RecommendationService.java
│   │   └── resources
│   │       ├── dataset/movies.json
│   │       └── static/index.html
│   └── test/java/com/inforetrieve
│       ├── indexer/TokenizerAndStemmerTest.java
│       ├── indexer/InvertedIndexTest.java
│       ├── ranking/TfIdfAndCosineTest.java
│       ├── query/QueryProcessorTest.java
│       └── recommender/RecommenderTest.java
```

---

## 🚀 Setup & Execution Instructions

### Prerequisites
- **Java 17 JDK** or higher
- **Maven 3.6+**

### 1. Build and Run Unit Tests
```bash
mvn clean test
```

### 2. Launch Spring Boot REST API & Web Dashboard
```bash
mvn spring-boot:run
```
Access the interactive web UI at: **`http://localhost:8080`**

### 3. Launch Interactive Command-Line Interface (CLI)
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--cli"
```

---

## 📡 Sample REST API Queries

| Endpoint | Description | Example Query |
| :--- | :--- | :--- |
| `GET /api/search` | Relevance-ranked search | `http://localhost:8080/api/search?q=dream+technology` |
| `GET /api/search` | Boolean Query | `http://localhost:8080/api/search?q=sci-fi+AND+intelligence+NOT+robot` |
| `GET /api/search` | Exact Phrase Search | `http://localhost:8080/api/search?q=%22virtual+reality%22` |
| `GET /api/autocomplete` | Trie Autocomplete | `http://localhost:8080/api/autocomplete?prefix=sci` |
| `GET /api/recommend/content` | Content-Based Recs | `http://localhost:8080/api/recommend/content?docId=DOC-101` |
| `GET /api/recommend/collaborative` | Collaborative Recs | `http://localhost:8080/api/recommend/collaborative?userId=USER-1` |
| `GET /api/eval` | Benchmark Metrics | `http://localhost:8080/api/eval` |

---

## 📊 Sample Benchmark Results Output

```
=======================================================
               BENCHMARK EVALUATION RESULTS            
=======================================================
Total Test Queries: 5
Precision@5:        1.0000
Recall@5:           1.0000
F1-Score:           1.0000
Mean Avg Precision: 1.0000
Avg Query Latency:  1.40 ms
=======================================================
```
