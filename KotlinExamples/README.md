# Kotlin Learning Examples

A comprehensive Kotlin learning project demonstrating modern Kotlin features, best practices, and design patterns through practical, well-organized examples.

## 📚 Table of Contents

- [Overview](#-overview)
- [Getting Started](#-getting-started)
- [Learning Modules](#-learning-modules)
- [Running Examples](#-running-examples)
- [Running Tests](#-running-tests)
- [Key Concepts Covered](#-key-concepts-covered)
- [Project Organization Principles](#-project-organization-principles)
- [Contributing](#-contributing)

## 🎯 Overview

This project serves as a comprehensive learning resource for Kotlin, organized into logical modules that progressively introduce concepts from basics to advanced topics. Each module contains both executable examples and comprehensive unit tests.

### What You'll Learn

- **Kotlin Basics**: Lambdas, receivers, and functional programming
- **Concurrency**: Coroutines, Flows, and async programming patterns
- **OOP Concepts**: Inheritance, polymorphism, sealed classes, and design patterns
- **Kotlin Features**: Companion objects, reified generics, extension functions, and more

## 🚀 Getting Started

### Prerequisites

- **JDK 11 or higher** - Required for running Kotlin
- **Gradle** (optional) - Build tool (wrapper included)
- **IDE** - IntelliJ IDEA recommended (with Kotlin plugin)

### Clone and Build

```bash
# Clone the repository
git clone <repository-url>
cd KotlinExamples

# Build the project
./gradlew build

# Run all tests
./gradlew test

# Run the main application
./gradlew run
```

## 📖 Learning Modules

### 1. Basics

#### Lambdas (`basics.lambdas`)
Learn about Kotlin's lambda expressions and functional programming features.

**Topics:**
- Lambda syntax variations
- SAM (Single Abstract Method) interfaces with `fun interface`
- Higher-order functions
- Function types and type inference
- Collection operations (map, filter, fold, etc.)

**Files:**
- `LambdasConceptsPlayground.kt` - Comprehensive lambda examples
- `LambdasConceptsPlaygroundTests.kt` - 65+ test cases

**Run:**
```bash
# Run the playground
./gradlew run -PmainClass=com.playground.kotlin.basics.lambdas.LambdasConceptsPlaygroundKt
```

#### Receivers (`basics.receivers`)
Explore receiver-style functions and scope functions.

**Topics:**
- Extension lambdas (`T.() -> R`)
- Scope functions: `run`, `with`, `apply`, `also`, `let`
- Builder patterns with receivers
- Resource management patterns

**Files:**
- `ReceiverStyleFunctionsPlayground.kt` - Receiver function examples
- `ReceiverStyleFunctionsPlaygroundTests.kt` - 40+ test cases

### 2. Concurrency

#### Coroutines (`concurrency.coroutines`)
Master Kotlin coroutines for asynchronous programming.

**Topics:**
- Coroutine lifecycle and cancellation
- Dispatchers (Default, IO, Main, Unconfined)
- Sequential vs concurrent execution with `async`/`await`
- Timeouts and error handling
- Structured concurrency

**Files:**
- `CoroutinesPlayground.kt` - Coroutine demonstrations

**Key Functions:**
- `runCoroutineLifecycleDemo()` - Job management
- `runCoroutineDispatchersDemo()` - Dispatcher behavior
- `runCoroutineConcurrentExecutionDemo()` - Async patterns
- `runCoroutineWithTimeoutDemo()` - Timeout handling

#### Flows (`concurrency.flows`)
Learn about Kotlin Flows for reactive programming.

**Topics:**
- Cold vs Hot flows
- SharedFlow and StateFlow
- Flow operators (map, filter, transform, etc.)
- Backpressure handling
- Exception handling in flows
- Flow testing with Turbine

**Files:**
- `FlowsPlayground.kt` - Comprehensive flow examples
- `CoroutinesAndFlowsTest.kt` - Advanced testing patterns

**Key Functions:**
- `runBaseFlowDemo()` - Cold flow basics
- `runHotFlowDemo()` - Hot flow broadcasting
- `runStateFlowAtomicUpdateDemo()` - Thread-safe state
- `runFlowsTransformationsDemo()` - Flow operators
- `runFlowsExceptionHandlingDemo()` - Error handling

### 3. OOP (Object-Oriented Programming)

#### Core (`oop.core`)
Fundamental OOP concepts in Kotlin.

**Topics:**
- Interfaces with default implementations
- Inheritance and polymorphism
- Abstract and open classes
- Sealed classes for type hierarchies
- Data classes
- Inner and nested classes
- Value classes (inline classes)

**Files:**
- `OopConceptsPlayground.kt` - Main OOP demonstrations
- `Printable.kt` - Interface example
- `Users.kt` - Inheritance hierarchy
- `Project.kt` - Nested/inner classes
- `Issues.kt` - Sealed class hierarchy
- `AuditedList.kt` - Class delegation

#### Patterns (`oop.patterns`)
Common design patterns in Kotlin.

**Files:**
- `Utils.kt` - Singleton objects (IdGenerator, Logger)

#### Examples - Devices (`oop.core.devices`)
Practical OOP implementations using a device management system.

**Version 1 (v1)** - Basic implementation:
- Abstract `Device` class
- Concrete implementations: `Lamp`, `Speaker`
- Singleton `DeviceRegistry`
- Extension functions

**Version 2 (v2)** - Enhanced with sealed classes:
- Sealed `Device` class (compile-time exhaustiveness)
- Additional device: `Thermostat`
- Improved type safety
- Builder pattern with `findByName` extension

### 4. Kotlin Features

#### Companion Objects (`features.companionobjects`)
Deep dive into companion objects.

**Topics:**
- Factory pattern with companion objects
- Named companion objects
- Nested object declarations
- Custom property getters/setters
- Companion object extensions

**Tests:**
- `CompanionObjectsPlaygroundTests.kt` - 4 focused tests

#### Reified Generics (`features.reified`)
Understanding reified type parameters.

**Topics:**
- `inline` functions with `reified`
- Runtime type checking with generics
- Generic type preservation

**Tests:**
- `ReifiedGenericsPlaygroundTests.kt` - 2 comprehensive tests

#### Extension Functions (`features.extensions`)
Master extension functions in Kotlin.

**Topics:**
- Extension functions on classes
- Extension functions on companion objects
- Member extension functions
- Extension function precedence rules
- Top-level vs member extensions

**Tests:**
- `ExtensionFunctionsPlaygroundTests.kt` - Companion extensions
- `ExtensionFunctionPrecedenceTests.kt` - Precedence demonstrations

## 🏃 Running Examples

### Run Main Application

```bash
./gradlew run
```

This executes `Main.kt` which demonstrates device management with both v1 and v2 implementations.

### Run Specific Playground

Each playground file has a `main()` function that can be executed directly:

**In IDE**: Right-click on the file → Run 'FileNameKt'

**Or use Gradle**:
```bash
./gradlew run -PmainClass=com.playground.kotlin.basics.lambdas.LambdasConceptsPlaygroundKt
```

## 🧪 Running Tests

### Run All Tests

```bash
./gradlew test
```

### Run Specific Test Class

```bash
./gradlew test --tests CompanionObjectsPlaygroundTests
./gradlew test --tests LambdasConceptsPlaygroundTests
./gradlew test --tests ReceiverStyleFunctionsPlaygroundTests
```

### Run Tests by Package

```bash
# Run all basics tests
./gradlew test --tests "com.playground.kotlin.basics.*"

# Run all feature tests
./gradlew test --tests "com.playground.kotlin.features.*"

# Run all concurrency tests
./gradlew test --tests "com.playground.kotlin.concurrency.*"
```

### Test Coverage

The project includes **150+ unit tests** covering:
- ✅ All lambda patterns and variations
- ✅ All scope functions and receiver patterns
- ✅ Companion objects and object declarations
- ✅ Reified generics with runtime type checking
- ✅ Extension functions and precedence rules
- ✅ Coroutines lifecycle and patterns
- ✅ Flows with cold/hot semantics

## 🔑 Key Concepts Covered

### Kotlin Language Features

| Feature                          | Location                    | Tests    |
|----------------------------------|-----------------------------|----------|
| Lambdas & Higher-Order Functions | `basics.lambdas`            | 65 tests |
| Extension Functions              | `features.extensions`       | 5 tests  |
| Receiver-Style Functions         | `basics.receivers`          | 40 tests |
| Companion Objects                | `features.companionobjects` | 4 tests  |
| Reified Generics                 | `features.reified`          | 2 tests  |
| Sealed Classes                   | `oop.core.devices.v2`       | -        |
| Value Classes                    | `oop.core`                  | -        |
| Data Classes                     | `oop.core.devices`          | -        |

### Concurrency & Async

| Feature     | Location                 | Description                             |
|-------------|--------------------------|-----------------------------------------|
| Coroutines  | `concurrency.coroutines` | Lifecycle, cancellation, dispatchers    |
| Flows       | `concurrency.flows`      | Cold/hot flows, operators, backpressure |
| SharedFlow  | `concurrency.flows`      | Hot flow broadcasting                   |
| StateFlow   | `concurrency.flows`      | State management with flows             |
| Async/Await | `concurrency.coroutines` | Concurrent execution patterns           |

### OOP Patterns

| Pattern          | Location           | Example                      |
|------------------|--------------------|------------------------------|
| Factory Pattern  | `oop.core.devices` | Companion object factories   |
| Singleton        | `oop.patterns`     | Object declarations          |
| Delegation       | `oop.core`         | AuditedList with `by`        |
| Builder Pattern  | `basics.receivers` | buildString, buildList, etc. |
| Registry Pattern | `oop.core.devices` | DeviceRegistry               |

## 📐 Project Organization Principles

### Package Structure Philosophy

1. **basics/** - Fundamental Kotlin language features
2. **features/** - Specific Kotlin language feature demonstrations
3. **concurrency/** - Asynchronous programming patterns
4. **oop/** - Object-oriented programming concepts
   - **core/** - Fundamental OOP principles
   - **patterns/** - Reusable design patterns

### Why This Structure?

- ✅ **Clear learning path** - Progress from basics to advanced
- ✅ **Concept-focused** - Each package teaches specific concepts
- ✅ **Scalable** - Easy to add new modules
- ✅ **Discoverable** - Intuitive package names
- ✅ **Professional** - Follows Kotlin community best practices

## 🛠️ Technologies & Dependencies

### Core Technologies
- **Kotlin**: 2.2.10
- **JVM Target**: 11
- **Gradle**: 8.5

### Dependencies
- **kotlinx-coroutines-core**: 1.10.2 - Coroutine support
- **kotlinx-coroutines-test**: 1.10.2 - Testing coroutines
- **Turbine**: 1.2.1 - Flow testing library
- **kotlin-test**: 2.2.10 - Testing framework

### Build Configuration

See `build.gradle.kts` for complete configuration.

## 📝 Code Style & Conventions

- **Package naming**: lowercase, descriptive
- **Class naming**: PascalCase
- **Function naming**: camelCase
- **Test naming**: `test[ConceptBeingTested]()`
- **Documentation**: KDoc comments for public APIs
- **Organization**: One concept per file when practical

## 🎓 Learning Path Recommendation

For best learning experience, follow this order:

1. **Start with Basics**
   - `LambdasConceptsPlayground.kt` - Understand functional programming
   - `ReceiverStyleFunctionsPlayground.kt` - Learn scope functions

2. **Explore Features**
   - `CompanionObjectsPlaygroundTests.kt` - Factory patterns
   - `ExtensionFunctionsPlaygroundTests.kt` - Extending classes
   - `ReifiedGenericsPlaygroundTests.kt` - Generic type safety

3. **Master OOP**
   - `OopConceptsPlayground.kt` - Core OOP principles
   - Device examples (v1 & v2) - Practical implementations

4. **Advanced Topics**
   - `CoroutinesPlayground.kt` - Async programming
   - `FlowsPlayground.kt` - Reactive patterns

## 🤝 Contributing

This is a learning project. Feel free to:
- Add new examples
- Improve existing documentation
- Add more test cases
- Suggest better organizations

## 📄 License

This project is for educational purposes.

## 🔗 Resources

### Official Documentation
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [Kotlin Flow Guide](https://kotlinlang.org/docs/flow.html)

### Recommended Reading
- **Kotlin in Action** - Dmitry Jemerov & Svetlana Isakova
- **Kotlin Coroutines** - Marcin Moskała
- **Effective Kotlin** - Marcin Moskała

---

**Happy Learning!** 🚀

For questions or issues, please refer to the comprehensive test suites which serve as executable documentation for each concept.

