name := "airlineapp"

organization := "com.datasenseanalytics.pluto"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-deprecation", "-feature")

val sparkVersion = "2.1.1"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql"  % sparkVersion % "provided",
  "org.apache.spark" %% "spark-hive" % sparkVersion % "provided",
  "org.tresamigos" %% "smv" % "2.1-SNAPSHOT",
  "org.scalatest" %% "scalatest" % "2.2.0" % "test",
  //"org.apache.spark" % "spark-core_2.10" % "1.5.2" % "provided",
  "org.apache.spark" %% "spark-mllib" % "2.1.1"
)

parallelExecution in Test := false

mainClass in assembly := Some("org.tresamigos.smv.SmvApp")

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

assemblyJarName in assembly := s"${name.value}-${version.value}-jar-with-dependencies.jar"

//mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
//  {
//    case PathList("org", "apache", xs @ _*) => MergeStrategy.last
//    case PathList("javax", "servlet", xs @ _*) => MergeStrategy.last
//    case PathList("javax", "xml", xs @ _*) => MergeStrategy.last
//    case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.last
//    case PathList("com", "google", xs @ _*) => MergeStrategy.last
//    case x => old(x)
//  }
//}

// allow Ctrl-C to interrupt long-running tasks without exiting sbt,
// if the task implementation correctly handles the signal
cancelable in Global := true

val smvInit = if (sys.props.contains("smvInit")) {
    val files = sys.props.get("smvInit").get.split(",")
    files.map{f=> IO.read(new File(f))}.mkString("\n")
  } else ""

initialCommands in console := s"""
val sc = new org.apache.spark.SparkContext("local", "shell")
val sqlContext = new org.apache.spark.sql.SQLContext(sc)
${smvInit}
"""

// clean up spark context
cleanupCommands in console := "sc.stop"
