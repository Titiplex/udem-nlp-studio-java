# NLP Studio for Corpora Correction and Annotation

A Java-based toolkit for correcting interlinear glossed text and generating CoNLL-U output, with both:

- a **CLI pipeline** for document processing,
- and a **desktop application** for interactive rule editing, annotation settings, workspace management, and preview.

This project was originally made for the **Chuj project at Université de Montréal** and is designed for low-resource
language
workflows based on glossed data and CoNLL-U export.

---

## Overview

The repository is organized as a multi-module Maven project:

- `core` — correction, alignment, parsing, annotation, CLI, and CoNLL-U generation
- `backend` — Spring Boot services, persistence, rule/settings management
- `app` — JavaFX desktop wrapper embedding the frontend and backend
- `frontend` — Vue/Vite frontend bundled into the desktop application

---

## Features

- correction of glossed interlinear entries using YAML rules
- annotation to **CoNLL-U**
- YAML-driven annotation configuration
- lexicon- and extractor-based annotation rules
- desktop UI for:
    - workspace entry management
    - correction preview
    - CoNLL-U preview
    - rule editing
    - annotation settings editing

---

## Requirements

- **Java 21**
- **Maven 3.9+**
- For desktop packaging on Windows:
    - a JDK including `jpackage`
- For frontend-only development:
    - **Node.js** and **npm**

---

## Repository structure

```text
.
├── app/        # JavaFX desktop application
├── backend/    # Spring Boot backend
├── core/       # CLI + core NLP pipeline
├── docs/       # Documentation for the project
├── frontend/   # Vue/Vite frontend
└── scripts/    # packaging helpers
```

## CLI usage

The `core` module provides a command-line interface.

### Build

```shell
mvn -pl core -am clean package
```

This produces the following file :

````
core/target/nlp-studio-core-0.1.0-all.jar
````

### Available commands

1. Prepare CoNLL-U from an input document

````shell
java -cp core/target/nlp-studio-core-0.1.0.jar org.titiplex.Main prepare input.docx correction.yaml annotation.yaml output.conllu
````

This command:

- reads a .docx or .txt input file,
- applies correction rules from correction.yaml,
- applies annotation settings from annotation.yaml,
- writes the resulting output.conllu.

2. Generate a corrected DOCX

````shell
java -cp core/target/nlp-studio-core-0.1.0-all.jar org.titiplex.Main correct-docx input.docx correction.yaml corrected.docx
````

3. Generate corpus statistics

````shell
java -cp core/target/nlp-studio-core-0.1.0-all.jar org.titiplex.Main stats input.docx correction.yaml stats.txt
````

### Backward-compatible mode

The legacy 4-argument mode is still supported:

````shell
java -cp core/target/nlp-studio-core-0.1.0-all.jar org.titiplex.Main input.docx correction.yaml annotation.yaml output.conllu
````

---

## Desktop application

The desktop application is a **JavaFX container** that starts an embedded Spring Boot backend and loads the bundled Vue
frontend.

### Run in development

From the repository root:

````shell
mvn -pl core,backend,app -am clean install -DskipTests
mvn -f app/pom.xml javafx:run
````

### Frontend-only development

If you want to work on the frontend separately:

````shell
cd frontend
npm install
npm run dev
````

Useful frontend commands:

````shell
npm run build
npm run test
npm run typecheck
````

---

## Packaging

Verify that the project builds correctly:

````shell
mvn clean verify
````

### Build the desktop jar

````shell
mvn -pl app -am -Pdesktop-prod clean package
````

Generated file:

````
app/target/nlp-studio-app-0.1.0-all.jar
````

### Create a Windows installer

After the production package is built:

````shell
scripts\package-windows.bat
````

This script uses `jpackage` to generate a Windows installer in:

````
app\target\installer
````

---

## Notes on the desktop build

The app build automatically:

- installs Node.js and npm through Maven,
- runs npm ci,
- runs the frontend build,
- copies the built frontend into the desktop application resources.

So in most cases, you do not need to build the frontend manually before packaging the desktop application.

---

## Data and configuration

The pipeline relies on YAML-based resources such as:

- correction rules
- annotation definitions
- POS and feature definitions
- lexicons
- extractors
- gloss mapping

This makes the system extensible and suitable for iterative linguistic work without hardcoding every rule in Java.

---

## Testing

Run all tests:

````shell
mvn test
````

Run the full project build:

````shell
mvn clean package
````

--- 

## Status

This branch focuses on an integrated **NLP studio** workflow rather than only a standalone converter:

- CLI processing remains available
- desktop editing and preview are first-class
- backend-managed rules and annotation settings are part of the current architecture

---

## Documentation

The documentation is built using [MkDocs](https://www.mkdocs.org) and is available at this repository's [GitHub Pages](https://titiplex.github.io/udem-nlp-studio-java/).

To preview the doc and edit it live:

````shell
mkdocs serve --livereload
````

To build the doc (generates files in [site/](site)) :

````shell
mkdocs build
````

To publish the doc in your repository under github pages, in the **gh-pages** branch :

````shell
mkdocs gh-deploy
````

---

## License

This projects runs under the GPL-v3 license, please see [LICENSE](LICENSE)