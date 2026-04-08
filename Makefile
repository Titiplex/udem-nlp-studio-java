release-ps:
	powershell -ExecutionPolicy Bypass -File .\scripts\release-desktop.ps1 $(version)

release-win:
	./scripts/release-desktop.bat $(VERSION)

test:
	mvn -pl core,backend,app -am clean test

build:
	mvn -pl core,backend,app -am clean install -DskipTests
	mvn -f app/pom.xml clean

all:
	mvn -pl core,backend,app -am clean install -DskipTests
	mvn -f app/pom.xml clean javafx:run