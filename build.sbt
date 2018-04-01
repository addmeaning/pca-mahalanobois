lazy val sparkVersion = "2.3.0"
lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "PCA Mahalanobis for Stackoverflow answer",
      scalaVersion := "2.11.12",
      version := "1.0"
    )),
    name := "PCA-Mahalanobis",
    description := "PCA-Mahalanobis",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
      "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
      "org.apache.spark" %% "spark-mllib" % sparkVersion % "provided",
      "com.github.fommil.netlib" % "all" % "1.1.2" % "provided" pomOnly(),
      "org.scalatest" %% "scalatest" % "3.0.4" % Test
    )
  )
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", _ @ _*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}

run in Compile <<= Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run))