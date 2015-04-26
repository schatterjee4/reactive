import sbt._
import Keys._

object ReactiveBuild extends Build {
  val sonatypeSnapshots = "https://oss.sonatype.org/content/repositories/snapshots/"
  val sonatypeStaging = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"

  val defaults = Seq(
    organization := "cc.co.scala-reactive",
    resolvers ++= List(
      "Sonatype snapshots" at sonatypeSnapshots,
      "Java.net Maven2 Repository" at "http://download.java.net/maven/2/"
    ),
    checksums in update := Nil,
    scalacOptions in (Compile, compile) += "-deprecation",
    scalacOptions in (Compile, doc) ++= Seq(
      "-sourcepath",
      baseDirectory.value.getAbsolutePath,
      "-doc-source-url",
      s"http://github.com/nafg/reactive/blob/master/${ baseDirectory.value.getName }€{FILE_PATH}.scala",
      "-doc-title",
      "Scaladocs - scala-reactive", "-groups"
    ),
    libraryDependencies ++= List(
      "org.scalatest" %% "scalatest" % "2.2.5" % "test",
      "org.scalacheck" %% "scalacheck" % "1.12.1" % "test"
    ),
    testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oF")
  )

  val publishingSettings = defaults ++ Seq(
    publishTo := (
      if (version.value.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at sonatypeSnapshots)
      else
        Some("staging" at sonatypeStaging)
    ),
    publishMavenStyle := true,
    credentials ++= {
      val f = file("/private/nafg/.credentials")
      if(f.exists) Seq(Credentials(f))
      else Nil
    },
    pomExtra := <developers><developer><id>nafg</id></developer></developers>,
    homepage := Some(url("http://scalareactive.org")),
    licenses := Seq(("Modified Apache", url("https://github.com/nafg/reactive/blob/master/LICENSE.txt"))),
    scmInfo := Some(ScmInfo(
      url("https://github.com/nafg/reactive"),
      "scm:git:git://github.com/nafg/reactive.git",
      Some("scm:git:git@github.com:nafg/reactive.git")
    ))
  )

  val nonPublishingSettings = defaults :+ (publish := ())

  lazy val reactive_core = Project("core", file("reactive-core"))
    .settings(publishingSettings: _*)

  lazy val reactive_routing = Project("routing", file("reactive-routing"))
    .settings(publishingSettings: _*)

  lazy val reactive_transport = Project("transport", file("reactive-transport"))
    .settings(publishingSettings: _*)
    .dependsOn(reactive_core)

  lazy val reactive_web = Project("web", file("reactive-web"))
    .settings(publishingSettings: _*)
    .dependsOn(reactive_core, reactive_transport)

  lazy val reactive_web_lift = Project("web-lift", file("reactive-web-lift"))
    .settings(publishingSettings: _*)
    .dependsOn(reactive_web, reactive_routing)

  lazy val reactive_web_demo = Project("demo", file("reactive-web-demo"))
    .settings(nonPublishingSettings: _*)
    .dependsOn(reactive_web_lift)

  lazy val root = Project("scala-reactive", file("."))
    .settings(nonPublishingSettings: _*)
    .settings(sbtunidoc.Plugin.unidocSettings: _*)
    .aggregate(
      reactive_core,
      reactive_transport,
      reactive_web,
      reactive_routing,
      reactive_web_lift,
      reactive_web_demo
    )
}
