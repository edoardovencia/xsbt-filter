package sbtfilter

import sbt._
import Keys._
import Defaults._
import java.io.File
import java.util.Properties
import sbt.Def.Initialize.joinInitialize
import sbt.Def.macroValueI
import sbt.Def.macroValueIT

import scala.collection.JavaConverters._
import scala.collection.Seq

/* TODO
- configurable variable delimiters
- support web plugin
- watchSources
*/
object Plugin extends sbt.AutoPlugin {

  import FilterKeys._
  import FileFilter.globFilter

  object FilterKeys {
    val filterDirectoryName =  Def.settingKey[String]("Default filter directory name.")
    val filterDirectory = Def.settingKey[File]("Default filter directory, used for filters.")
    val filters = Def.taskKey[Seq[File]]("All filters.")
    val extraProps = Def.settingKey[Seq[(String, String)]]("Extra filter properties.")
    val projectProps = Def.taskKey[Seq[(String, String)]]("Project filter properties.")
    val systemProps = Def.taskKey[Seq[(String, String)]]("System filter properties.")
    val envProps = Def.taskKey[Seq[(String, String)]]("Environment filter properties.")
    val managedProps = Def.taskKey[Seq[(String, String)]]("Managed filter properties.")
    val unmanagedProps = Def.taskKey[Seq[(String, String)]]("Filter properties defined in filters.")
    val props = Def.taskKey[Seq[(String, String)]]("All filter properties.")
    val filterResources = Def.taskKey[Seq[(File, File)]]("Filters all resources.")
  }

  lazy val filterResourcesTask = filterResources := (filter(copyResources, filterResources) triggeredBy copyResources).value

  def filter(resources: TaskKey[Seq[(File, File)]], task: TaskKey[Seq[(File, File)]]) = Def.task { 

    val streamsValue = streams.value
    val resourcesValue = (resources in task).value
    val inclValue = (includeFilter in task).value
    val exclValue = (excludeFilter in task).value
    val filterProps = props.value

    val newProps = Map.empty[String, String] ++ filterProps
    val filtered = resourcesValue filter (r => inclValue.accept(r._1) && !exclValue.accept(r._1) && !r._1.isDirectory)
    Filter(streamsValue.log, filtered map (_._2), newProps)
    resourcesValue
  }

  lazy val projectPropsTask = projectProps := {
      Seq(
        "organization" -> organization.value,
        "name" -> name.value,
        "description" -> description.value,
        "version" -> version.value,
        "scalaVersion" -> scalaVersion.value,
        "sbtVersion" -> sbtVersion.value
      )
  }

  lazy val unmanagedPropsTask = unmanagedProps := {
    val filtersValue = filters.value
    val streamsValue = streams.value
    (Seq.empty[(String, String)] /: filtersValue) { (acc, rf) => acc ++ properties(streamsValue.log, rf).asScala }
  }


  lazy val filterConfigPaths: Seq[Setting[_]] = Seq(
    filterDirectory := sourceDirectory.value / filterDirectoryName.value,
    sourceDirectories in filters := (Seq(filterDirectory).join).value,
    filters := collectFiles(sourceDirectories in filters, includeFilter in filters, excludeFilter in filters).value,
    includeFilter in filters := "*.properties" | "*.xml",
    excludeFilter in filters := HiddenFileFilter,
    includeFilter in filterResources := "*.properties" | "*.xml",
    excludeFilter in filterResources := HiddenFileFilter || ImageFileFilter)

  lazy val filterConfigTasks: Seq[Setting[_]] = Seq(
    filterResourcesTask,
    copyResources in filterResources := copyResources.value,
    managedProps := projectProps.value ++ systemProps.value ++ envProps.value,
    unmanagedPropsTask,
    props := extraProps.value ++ managedProps.value ++ unmanagedProps.value)

  lazy val filterConfigSettings: Seq[Setting[_]] = filterConfigTasks ++ filterConfigPaths

  lazy val baseFilterSettings = Seq(
    filterDirectoryName := "filters",
    extraProps := Nil,
    projectPropsTask,
    envProps := System.getenv.asScala.toSeq,
    systemProps := System.getProperties.stringPropertyNames.asScala.toSeq map (k => k -> System.getProperty(k)))

  lazy val filterSettings = baseFilterSettings ++ inConfig(Compile)(filterConfigSettings) ++ inConfig(Test)(filterConfigSettings)

  object ImageFileFilter extends FileFilter {
    val formats = Seq("jpg", "jpeg", "png", "gif", "bmp")
    def accept(file: File) = formats.exists(_ == file.ext.toLowerCase)
  }

  def properties(log: Logger, path: File) = {
    val props = new Properties
    IO.load(props, path)
    props
  }

  object Filter {
    import scala.util.matching.Regex._
    import java.io.{ FileReader, BufferedReader, PrintWriter }

    val pattern = """((?:\\?)\$\{.+?\})""".r
    def replacer(props: Map[String, String]) = (m: Match) => {
      m.matched match {
        case s if s.startsWith("\\") => Some("""\$\{%s\}""" format s.substring(3, s.length -1))
        case s => props.get(s.substring(2, s.length -1))
      }
    }
    def filter(line: String, props: Map[String, String]) = pattern.replaceSomeIn(line, replacer(props))

    def apply(log: Logger, files: Seq[File], props: Map[String, String]) {
      log debug ("Filter properties: %s" format (props.mkString("{", ", ", "}")))
      IO.withTemporaryDirectory { dir =>
        files foreach { src =>
          log debug ("Filtering %s" format src.absolutePath)
          val dest = new File(dir, src.getName)
          val out = new PrintWriter(dest)
          val in = new BufferedReader(new FileReader(src))
          IO.foreachLine(in) { line => IO.writeLines(out, Seq(filter(line, props))) }
          in.close()
          out.close()
          IO.copyFile(dest, src, true)
        }
      }
    }
  }

}
