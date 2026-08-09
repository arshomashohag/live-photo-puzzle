# Tessera — build helpers.
# The JDK is pinned in gradle.properties and the SDK path in local.properties,
# so `./gradlew` works without extra env. ANDROID_HOME is exported here only so
# `adb` (install/uninstall targets) is found.

ANDROID_HOME ?= /opt/homebrew/share/android-commandlinetools
ADB := $(ANDROID_HOME)/platform-tools/adb
GRADLEW := ./gradlew
DEBUG_APK := app/build/outputs/apk/debug/app-debug.apk
RELEASE_APK := app/build/outputs/apk/release/app-release-unsigned.apk

export ANDROID_HOME

.DEFAULT_GOAL := apk

.PHONY: help
help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) \
		| awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-14s\033[0m %s\n", $$1, $$2}'

.PHONY: apk
apk: ## Build the debug APK (default) and print its path
	$(GRADLEW) :app:assembleDebug
	@echo "APK: $(DEBUG_APK)"

.PHONY: release-apk
release-apk: ## Build the unsigned release APK
	$(GRADLEW) :app:assembleRelease
	@echo "APK: $(RELEASE_APK)"

.PHONY: install
install: ## Build and install the debug APK on a connected device/emulator
	$(GRADLEW) :app:installDebug
	@echo "Installed com.tessera.puzzle"

.PHONY: uninstall
uninstall: ## Remove the app from the connected device
	-$(ADB) uninstall com.tessera.puzzle

.PHONY: run
run: install ## Install then launch the app on the device
	$(ADB) shell monkey -p com.tessera.puzzle -c android.intent.category.LAUNCHER 1

.PHONY: test
test: ## Run JVM unit + property-based tests
	$(GRADLEW) :app:testDebugUnitTest

.PHONY: androidTest
androidTest: ## Run instrumented (Room) tests — needs a device/emulator
	$(GRADLEW) :app:connectedDebugAndroidTest

.PHONY: lint
lint: ## Run Android lint
	$(GRADLEW) :app:lintDebug

.PHONY: check
check: test lint ## Run unit tests and lint

.PHONY: devices
devices: ## List connected devices/emulators
	$(ADB) devices -l

.PHONY: clean
clean: ## Remove build outputs
	$(GRADLEW) clean
