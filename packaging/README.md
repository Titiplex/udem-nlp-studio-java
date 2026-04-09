# Native packaging

This directory contains the scripts used to build native installers for:

- the desktop application (`app`)
- the CLI tool (`core`)

## Overview

The build is split in two layers:

1. Maven builds the shaded jars
2. `jpackage` builds native packages from those jars
3. GitHub Actions publishes native GitHub Releases

## Components

### Desktop application

Main class:

- `org.titiplex.app.DesktopApp`

Expected shaded jar:

- `app/target/nlp-studio-app-<version>-all.jar`

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

Launcher name:

- `nlpstudio`

---

## Requirements

### Common

- JDK 21+ with `jpackage`
- Maven
- Git

### Desktop app build

The app build also triggers the frontend build through Maven.

### Platform-specific notes

#### Windows

For `msi` or `exe`, install WiX Toolset if your environment requires it.

The Windows CLI installer uses a custom `jpackage` resource directory to override the default WiX templates.

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

After installation, the CLI launcher should be available from a new terminal as:

````powershell
nlpstudio
````

#### Linux

````shell
./packaging/cli/linux/build.sh 0.1.0 app-image
./packaging/cli/linux/install.sh
````

The Linux install script copies the packaged CLI into:

- ``~/.local/opt/nlpstudio``

and creates a launcher script in:

- ``~/.local/bin/nlpstudio``

If needed, the install script can also offer to add ``~/.local/bin`` to the user's `PATH`.

#### macOS

````shell
./packaging/cli/macos/build.sh 0.1.0 app-image
./packaging/cli/macos/install.sh
````

The macOS install script copies the packaged CLI into:

- ``~/Applications/nlpstudio``

and creates a launcher script in:

- ``~/.local/bin/nlpstudio``

If needed, the install script can also offer to add ``~/.local/bin`` to the user's `PATH`.

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

Legacy fallback names are still accepted by the build scripts:

````
packaging/resources/cli/
    nlp-studio-cli.ico
    nlp-studio-cli.icns
    nlp-studio-cli.png
````

If the icon files are missing, packaging still works.

---

## Windows CLI resource override

The Windows CLI installer uses these custom files:

````
packaging/cli/windows/resources/
    main.wxs
    overrides.wxi
    ShortcutPromptDlg.wxs
````

These files override the default ``jpackage`` WiX resources so the installer can ask whether `nlpstudio` should be added
to the user `PATH`.

---

## Release model

This repository uses two independent release tracks:

- app releases use tags matching app-v*
- CLI releases use tags matching cli-v*

Examples:

- `app-v0.1.0`
- `cli-v0.1.0`

## One-command release triggers

### App release

````shell
./scripts/publish-app-release.sh 0.1.0
````

### CLI release

````shell
./scripts/publish-cli-release.sh 0.1.0
````

### With Make

````shell
make release-app VERSION=0.1.0
make release-cli VERSION=0.1.0
````

## Published assets

### App release assets

- `nlp-studio-windows-<version>.msi`
- `nlp-studio-linux-<version>.deb`
- `nlp-studio-macos-<version>.dmg`

### CLI release assets

- `nlp-studio-cli-windows-<version>.exe`
- `nlp-studio-cli-linux-<version>.tar.gz`
- `nlp-studio-cli-macos-<version>.tar.gz`

--- 

## Notes

- App and CLI releases are published independently.
- Workflow artifacts are only used internally during CI.
- Final binaries are attached to GitHub Releases.
- `workflow_dispatch` is available once the workflows are present on the default branch.