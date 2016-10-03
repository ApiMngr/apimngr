name := """ApiMngr"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

routesGenerator := StaticRoutesGenerator

PlayKeys.devSettings := Seq("play.server.http.port" -> "9090")

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  filters,
  "net.spy" % "spymemcached" % "2.12.1",
  "io.dropwizard.metrics" % "metrics-core" % "3.1.2",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)
