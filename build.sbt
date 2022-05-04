enablePlugins(ScalaJSPlugin)
jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv()

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "s15"
  )

// This is an application with a main method
scalaJSUseMainModuleInitializer := true

libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "2.1.0",
    "com.lihaoyi" %%% "scalatags" % "0.11.1"
)

