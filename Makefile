.PHONY: help dev build release-app release-cli package-app-linux package-app-macos package-cli-linux package-cli-macos package-app-windows package-cli-windows chmod-scripts

VERSION ?=

help:
	@echo "Available targets:"
	@echo "  make release-app VERSION=0.1.0"
	@echo "  make release-cli VERSION=0.1.0"
	@echo "  make package-app-linux VERSION=0.1.0"
	@echo "  make package-app-macos VERSION=0.1.0"
	@echo "  make package-cli-linux VERSION=0.1.0"
	@echo "  make package-cli-macos VERSION=0.1.0"
	@echo "  make chmod-scripts"

release-app:
	@test -n "$(VERSION)" || (echo "VERSION is required"; exit 1)
	./scripts/publish-app-release.sh $(VERSION)

release-cli:
	@test -n "$(VERSION)" || (echo "VERSION is required"; exit 1)
	./scripts/publish-cli-release.sh $(VERSION)

package-app-linux:
	@test -n "$(VERSION)" || (echo "VERSION is required"; exit 1)
	./packaging/app/linux/build.sh $(VERSION) deb

package-app-macos:
	@test -n "$(VERSION)" || (echo "VERSION is required"; exit 1)
	./packaging/app/macos/build.sh $(VERSION) dmg

package-cli-linux:
	@test -n "$(VERSION)" || (echo "VERSION is required"; exit 1)
	./packaging/cli/linux/build.sh $(VERSION) app-image

package-cli-macos:
	@test -n "$(VERSION)" || (echo "VERSION is required"; exit 1)
	./packaging/cli/macos/build.sh $(VERSION) app-image

package-app-windows:
	@echo "Run this from PowerShell on Windows:"
	@echo ".\\packaging\\app\\windows\\build.ps1 -Version $(VERSION) -Type msi"

package-cli-windows:
	@echo "Run this from PowerShell on Windows:"
	@echo ".\\packaging\\cli\\windows\\build.ps1 -Version $(VERSION) -Type exe"

chmod-scripts:
	chmod +x scripts/publish-app-release.sh
	chmod +x scripts/publish-cli-release.sh
	chmod +x packaging/app/linux/build.sh || true
	chmod +x packaging/app/macos/build.sh || true
	chmod +x packaging/cli/linux/build.sh || true
	chmod +x packaging/cli/linux/install.sh || true
	chmod +x packaging/cli/macos/build.sh || true
	chmod +x packaging/cli/macos/install.sh || true

dev:
	mvn -pl core,backend,app -am clean install -DskipTests
	mvn -f app/pom.xml clean javafx:run

build:
	mvn -pl core,backend,app -am clean install -DskipTests
	mvn -f app/pom.xml clean