# Native packaging

This directory contains the scripts used to build native installers for:

- the desktop application (`app`)
- the CLI tool (`core`)

## Overview

The build is split in two layers:

1. Maven builds the shaded jars
2. `jpackage` builds native packages from those jars

### Desktop application

Main class:

- `org.titiplex.app.DesktopApp`

Expected shaded jar:

- `app/target/nlp-studio-app-<version>-all.jar`

### CLI application

Main class:

- `org.titiplex.Main`

Expected shaded jar:

- `core/target/nlp-studio-core-<version>-all.jar`

---

## Requirements

### Common

- JDK 21 with `jpackage`
- Maven
- Git

### Desktop app build

The app build also triggers the frontend build through Maven.

### Platform-specific notes

#### Windows

For `msi`, install WiX Toolset if your environment requires it.

#### Linux

For native packages, install packaging dependencies such as:

- `fakeroot`
- `rpm` / `rpmbuild` if building RPMs

#### macOS

For real public distribution, you will eventually need:

- signing
- notarization

---

## Local usage

### Desktop app

#### Windows

```powershell
.\packaging\app\windows\build.ps1 -Version 0.1.0 -Type msi
```

#### Linux

````shell
./packaging/app/linux/build.sh 0.1.0 deb
````

#### macOS

````shell
./packaging/app/macos/build.sh 0.1.0 dmg
````

### CLI

#### Windows

````powershell
.\packaging\cli\windows\build.ps1 -Version 0.1.0 -Type exe
````

#### Linux

````shell
./packaging/cli/linux/build.sh 0.1.0 app-image
./packaging/cli/linux/install.sh
````

#### macOS

````shell
./packaging/cli/macos/build.sh 0.1.0 app-image
./packaging/cli/macos/install.sh
````

---

## Recommended output types

### Desktop app

- Windows: `msi`
- Linux: `deb`
- macOS: `dmg`

### CLI

- Windows: `exe`
- Linux: `app-image` or `deb`
- macOS: `app-image` or `pkg`

---

## Icons

Optional icons can be placed in:

````
packaging/resources/app/
    nlp-studio.ico
    nlp-studio.icns
    nlp-studio.png

packaging/resources/cli/
    chuj.ico
    chuj.icns
    chuj.png
````

If the icon files are missing, packaging still works.

---

## Suggested release flow

1. Commit and push to main
2. Tag a version such as v0.1.0
3. GitHub Actions builds native artifacts on:
    - Windows
    - Linux
    - macOS
4. Download artifacts from the workflow run
5. Optionally publish them in a GitHub Release