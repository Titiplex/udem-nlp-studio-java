# Correction and Annotation Engine

## Introduction

This motor is made to correct a document consisting of entries with glosses.
Then, it produces a CoNLL-U file.

It is based on the [CoNLL-U format](https://universaldependencies.org/format.html).
This project is part of the Chuj project taking place in Université de Montréal.

## Execution

Supposing a file `input.docx` and a file `rules.yaml`.

```bash
mvn test
mvn package
java -cp target/chuj-nlp-core-0.1.0.jar org.titiplex.Main input.docx rules.yaml output.conllu
```

It will generate a file `output.conllu`.

## Development

```bash
cd frontend
npm install
npm run build
```

Dev run :
```bash
mvn -pl core,backend,app -am clean install -DskipTests
mvn -f app/pom.xml clean javafx:run
```

## Production

``mvn -pl app -am -Pdesktop-prod clean package``
Generated jar file : ``app/target/nlp-studio-app-0.1.0-all.jar``

## Installer
Windows : ``scripts\package-windows.bat``