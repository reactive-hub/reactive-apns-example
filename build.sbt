lazy val commonSettings = Seq(
  organization := "com.reactivehub",
  scalaVersion := "2.11.8",

  scalacOptions := Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint",
    "-Xfuture",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused",
    "-Ywarn-unused-import"
  ),

  resolvers += Resolver.bintrayRepo("reactivehub", "maven")
)

lazy val root = (project in file("."))
  .aggregate(manager, pusher)
  .settings(commonSettings)

lazy val manager = (project in file("manager"))
  .dependsOn(`pusher-api`)
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(
    "com.typesafe.akka"  %% "akka-http-experimental"            % "2.4.7",
    "com.typesafe.akka"  %% "akka-http-spray-json-experimental" % "2.4.7",
    "com.typesafe.akka"  %% "akka-stream-kafka"                 % "0.11-M3",
    "com.typesafe.akka"  %% "akka-slf4j"                        % "2.4.7",
    "com.typesafe.slick" %% "slick"                             % "3.1.1",
    "com.h2database"     %  "h2"                                % "1.4.192",
    "ch.qos.logback"     %  "logback-classic"                   % "1.1.7" % Runtime
  )
)

lazy val `pusher-api` = (project in file("pusher-api"))
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(
    "org.apache.kafka" %  "kafka-clients" % "0.9.0.1",
    "io.spray"         %% "spray-json"    % "1.3.2"
  )
)

lazy val pusher = (project in file("pusher"))
  .dependsOn(`pusher-api`)
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-stream-kafka" % "0.11-M3",
    "com.typesafe.akka" %% "akka-slf4j"        % "2.4.7",
    "com.reactivehub"   %% "akka-stream-apns"  % "0.4",
    "ch.qos.logback"    %  "logback-classic"   % "1.1.7" % Runtime
  )
)
