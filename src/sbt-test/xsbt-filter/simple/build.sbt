version := "0.1"

name := "simple"

scalaVersion := "2.12.8"

sbtfilter.Plugin.filterSettings

TaskKey[Unit]("check-compile") := {
  val cd = (classDirectory in Compile).value
  val props = new java.util.Properties
  IO.load(props, cd / "sample.properties")
  if (props.getProperty("name") != "simple")
    sys.error("property not substituted")
  if (props.getProperty("homepage") != "http://localhost")
    sys.error("property not substituted")
  if (props.getProperty("anothername") != "${name}")
    sys.error("property substituted")
  if (IO.read(cd / "sample.txt") != "This ${name} shouldn't be substituted.\n")
    sys.error("file filtered")
  ()
}

TaskKey[Unit]("check-test") := {
  val cd = (classDirectory in Compile).value
  val props = new java.util.Properties
  IO.load(props, cd / "sample.properties")
  if (props.getProperty("name") != "simple")
    sys.error("property not substituted")
  ()
}
