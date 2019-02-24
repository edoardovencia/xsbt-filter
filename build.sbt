name := "xsbt-filter"

organization := "eu.edoardovencia.sbt"

version := "0.5"

scalaVersion := "2.12.8"

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-encoding", "UTF-8"
)

sbtPlugin := true

sbtVersion in Global := "1.0.3"

crossSbtVersions := Vector("0.13.17", "1.0.3")

scalaCompilerBridgeSource := {
  val sv = appConfiguration.value.provider.id.version
  ("org.scala-sbt" % "compiler-interface" % sv % "component").sources
}

licenses := Seq("New BSD License" -> url("http://opensource.org/licenses/BSD-3-Clause"))

homepage := Some(url("http://edoardovencia.github.com/xsbt-filter/"))

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.trim.endsWith("SNAPSHOT")) 
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
