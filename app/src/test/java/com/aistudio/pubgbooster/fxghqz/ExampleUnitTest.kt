package com.aistudio.pubgbooster.fxghqz

import org.junit.Test
import java.io.File

class ExampleUnitTest {
  @Test
  fun listFiles() {
    println("--- RECURSIVE FILE LISTING ---")
    File("/app").walkTopDown().forEach { file ->
      if (!file.absolutePath.contains("/.gradle") && !file.absolutePath.contains("/build") && !file.absolutePath.contains("/.git/")) {
        println(file.absolutePath)
      }
    }
    println("--- END RECURSIVE FILE LISTING ---")
  }
}
