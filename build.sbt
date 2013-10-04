name := "play-ws-test"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-simple" % "1.7.5",
  "org.cometd.java" % "cometd-java-client" % "2.7.0-RC1"
)     

play.Project.playJavaSettings
