package com.sksamuel.kt.extensions.system

import io.kotlintest.*
import io.kotlintest.extensions.TopLevelTest
import io.kotlintest.extensions.system.OverrideMode
import io.kotlintest.extensions.system.SystemEnvironmentTestListener
import io.kotlintest.extensions.system.withEnvironment
import io.kotlintest.inspectors.forAll
import io.kotlintest.specs.FreeSpec
import io.kotlintest.specs.WordSpec
import io.mockk.mockk

class SystemEnvironmentExtensionTest : FreeSpec() {

  private val key = "SystemEnvironmentExtensionTestFoo"
  private val value = "SystemEnvironmentExtensionTestBar"

  private val mode: OverrideMode = mockk {
    every { override(any(), any()) } answers { firstArg<Map<String, String>>().plus(secondArg<Map<String,String>>()).toMutableMap() }
  }

  init {
    "Should set environment to specific map" - {
      executeOnAllEnvironmentOverloads {
        System.getenv(key) shouldBe value
      }
    }

    "Should return original environment to its place after execution" - {
      val before = System.getenv().toMap()

      executeOnAllEnvironmentOverloads {
        System.getenv() shouldNotBe before
      }
      System.getenv() shouldBe before

    }

    "Should return the computed value" - {
      val results = executeOnAllEnvironmentOverloads { "RETURNED" }

      results.forAll {
        it shouldBe "RETURNED"
      }
    }
  }

  private suspend fun <T> FreeSpecScope.executeOnAllEnvironmentOverloads(block: suspend () -> T): List<T> {
    val results = mutableListOf<T>()

    "String String overload" {
      results += withEnvironment(key, value, mode) { block() }
    }

    "Pair overload" {
      results += withEnvironment(key to value, mode) { block() }
    }

    "Map overload" {
      results += withEnvironment(mapOf(key to value), mode) { block() }
    }

    return results
  }

}

class SystemEnvironmentTestListenerTest : WordSpec() {

  override fun listeners() = listOf(SystemEnvironmentTestListener("wibble", "wobble"))

  override fun beforeSpecClass(spec: Spec, tests: List<TopLevelTest>) {
    System.getenv("wibble") shouldBe null
  }

  override fun afterSpecClass(spec: Spec, results: Map<TestCase, TestResult>) {
    System.getenv("wibble") shouldBe null
  }

  init {
    "sys environment extension" should {
      "set environment variable" {
        System.getenv("wibble") shouldBe "wobble"
      }
    }
  }
}
