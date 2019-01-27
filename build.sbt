enablePlugins(PlayScala)

name := "shogi server"
version := "0.1-SNAPSHOT"
scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
    guice,
    "redis.clients" % "jedis" % "3.0.1",
    "org.json4s" %% "json4s-jackson" % "3.6.2"
)

dependencyOverrides ++= Set(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.7",
  "com.typesafe.akka" %% "akka-stream" % "2.5.19",
  "com.typesafe.akka" %% "akka-actor" % "2.5.19",
  "com.google.guava" % "guava" % "23.6.1-jre"
)
