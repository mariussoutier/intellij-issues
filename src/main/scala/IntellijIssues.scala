package com.mariussoutier.example

// This is after Optimize Imports
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import com.typesafe.config.{Config, ConfigFactory}
import scala.util.Try

/*
But it should look like this:
import java.util.concurrent.TimeUnit

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.util.Try

import com.typesafe.config.{Config, ConfigFactory}
*/

/**
 * 'Type Hierarchy' for Config works
 */
trait WithConfig {

  lazy val config: Config = ConfigFactory.load()

  implicit class ConfigOps(config: Config) {
    def maybeString(path: String): Option[String] = Try {
      config.getString(path)
    }.toOption
  }

}

case class User(id: Long, name: String, email: String)

/**
 * 'Type Hierarchy' on IntellijIssues only shows java.lang.Object > IntellijIssues
 * instead of Any > Config > IntellijIssues
 */
class IntellijIssues extends WithConfig {

  val configVersion: Option[String] = config.maybeString(IntellijIssues.versionPath)

  /**
   * Simplifiable operation issue:
   * I have disabled the inspection "replace map/getOrElse with fold", but this code part is still underlined green.
   * See screenshot "IntelliJ-Simplifiable-...png"
   *
   */
  def majorVersion: String =
    configVersion
      .map(_.split(".")(0))
      .getOrElse("1")


  /**
   * This method was formatted by IntelliJ ("Reformat code..."). The indenting is not very readable.
   */
  def minorVersion: String =
    configVersion
      .map { version =>
      val components = version.split(".") // should be indented
      components(1) // should be indented
    }
      .getOrElse("0")


  // 'Fetch' a user
  def userById(id: Long): Future[Option[User]] = Future.successful(Some(User(id, "John Smith", "john@example.org")))

  /**
   * Here I want to apply a transformation on an Option inside a future.
   * I start to write `map` and then hit smart completion.
   * IntelliJ suggests a case statement named "option".
   * I'd find it helpful to have another suggestion that avoids the case statement and the type declaration.
   * Instead it should just prefix the name with "maybe" for Options, "eventual" for futures, "tried" for Try, and so on
   * (this is pretty common in Scala code).
   */
  userById(1234).map { case option: Option[User] =>
    option.map(_.name).getOrElse("Unknown")
  }

  /**
   * This is my ideal version:
   */
  userById(1234).map { maybeUserById =>
    maybeUserById.map(_.name).getOrElse("Unknown")
  }

  /**
   * A case statement in a map is useful when we are dealing with case classes. It'd be very cool if IntelliJ
   * destructured the class (via unapply).
   */
  userById(1234).foreach { maybeUserById =>
    // the case part should be auto-generated; and please leave whitespaces around @
    maybeUserById.foreach { case user@User(id, name, email) =>
      println(name)
    }
  }

}

/**
 * I'd like to have shortcut that jumps between class/trait and companion object.
 */
object IntellijIssues {

  val versionPath = "version"

  val defaultDuration = Duration(100, TimeUnit.SECONDS)

  implicit val ec = ExecutionContext.Implicits.global

}
