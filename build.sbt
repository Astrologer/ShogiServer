enablePlugins(PlayScala)

name := "shogi server"
version := "0.1-SNAPSHOT"
scalaVersion := "2.12.6"

libraryDependencies += guice
libraryDependencies += "redis.clients" % "jedis" % "3.0.1"
libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.6.2"
