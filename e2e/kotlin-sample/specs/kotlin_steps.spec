# Kotlin step patterns

Each scenario checks whether the gauge-java execution layer can run one way of
writing a step in Kotlin. Tags name the pattern. Scenarios tagged known-issue do
not work yet and are excluded by default (see gauge.tags in pom.xml).

## A step in a regular class
Tags: class

* Greet "Gauge" from a regular class

## A top-level function
Tags: top-level, known-issue

* Run a top-level function step

## A function in an object
Tags: object, known-issue

* Run a step in an object

## A function in a companion object
Tags: companion, known-issue

* Run a step in a companion object

## A function in a companion object with JvmStatic
Tags: companion-jvmstatic, known-issue

* Run a JvmStatic step in a companion object

## A function with a default argument
Tags: default-argument, known-issue

* Run a step with a default argument "given"

## A suspend function
Tags: suspend, known-issue

* Run a suspend function step
