
libraryDependencies <+= sbtVersion(v => v.split('.') match {
    case Array("1", "0", _) => "org.scala-sbt" % "scripted-plugin" % v
    case Array("0", "13", _) => "org.scala-sbt" % "scripted-plugin" % v
    case Array("0", "12", _) => "org.scala-sbt" % "scripted-plugin" % v
    case _                   => "org.scala-sbt" %% "scripted-plugin" % v
})

// Sbt Eclipse
// https://github.com/typesafehub/sbteclipse
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "5.2.4")

// Sbt Pgp
// https://github.com/sbt/sbt-pgp
addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.2")
