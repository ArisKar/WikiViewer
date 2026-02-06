# WikiViewer Project Design

## 1. Project Overview
The WikiViewer is a Java application that allows users to search for Wikipedia articles via the MediaWiki API, save them to a local database, enrich them with metadata (category, comments, rating), and view statistics.

## 2. Technologies
- **Language:** Java 11+
- **GUI:** Swing
- **Database:** Apache Derby
- **Persistence:** JPA (EclipseLink or Hibernate)
- **HTTP Client:** OkHttp
- **JSON Parsing:** GSON
- **PDF Generation:** iText or PDFBox
- **Build Tool:** Maven (to manage dependencies easily)

## 3. Architecture
The project will follow a layered architecture:
- **`gr.eap.wikiviewer.model`**: Entity classes (Article, Category, SearchKeyword).
- **`gr.eap.wikiviewer.service`**: Business logic (API calls, PDF generation, Database operations).
- **`gr.eap.wikiviewer.gui`**: Swing components and windows.
- **`gr.eap.wikiviewer.util`**: Helper classes (JSON utils, String cleaners).

## 4. Database Schema (E-R)
- **Article**: id (PK), title, snippet, timestamp, category_id (FK), comments, rating.
- **Category**: id (PK), name.
- **SearchKeyword**: id (PK), keyword, search_count.

## 5. Implementation Steps
1. Initialize Maven project.
2. Define JPA Entities.
3. Implement Wikipedia API client using OkHttp and GSON.
4. Implement Database Service (CRUD for Articles and Categories).
5. Implement PDF Export Service.
6. Develop Swing GUI:
    - Main Window with menu/tabs.
    - Search View (Live API).
    - Local Articles View (with category filtering).
    - Article Details/Edit View.
    - Statistics View with PDF export button.
7. Generate Javadoc.
