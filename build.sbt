enablePlugins(PlayScala)

name := "shogi server"
version := "0.1-SNAPSHOT"
scalaVersion := "2.12.6"

libraryDependencies += guice
libraryDependencies += "redis.clients" % "jedis" % "3.0.1"
