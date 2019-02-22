version := "0.5-SNAPSHOT"

sbtPlugin := true

name := "xsbt-filter"

organization := "eu.edoardovencia.sbt"

seq(ScriptedPlugin.scriptedSettings: _*)

scalaSource in Compile <<= baseDirectory { (base) => base / "src" }

sbtTestDirectory <<= baseDirectory { (base) => base / "sbt-test" }

scalaVersion := "2.12.8"

crossScalaVersions := Seq("2.9.3", "2.10.7", "2.11.12", "2.12.8")

sbtVersion in Global := "1.0.3"

crossSbtVersions := Vector("0.13.17", "1.0.3")

scalaCompilerBridgeSource := {
  val sv = appConfiguration.value.provider.id.version
  ("org.scala-sbt" % "compiler-interface" % sv % "component").sources
}

scalacOptions ++= Seq("-deprecation", "-unchecked")

licenses := Seq("New BSD License" -> url("http://opensource.org/licenses/BSD-3-Clause"))

homepage := Some(url("http://edoardovencia.github.com/xsbt-filter/"))

publishTo <<= version { v: String =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) 
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else                             
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { x => false }

pomExtra := (
  <scm>
    <url>git@github.com:edoardovencia/xsbt-filter.git</url>
    <connection>scm:git:git@github.com:edoardovencia/xsbt-filter.git</connection>
  </scm>
  <developers>
    <developer>
      <id>edoardovencia</id>
      <name>Edoardo Vencia</name>
      <url>https://github.com/edoardovencia</url>
    </developer>
  </developers>
)
