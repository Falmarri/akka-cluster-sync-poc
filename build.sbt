organization  := "com.example"

version       := "0.1"

scalaVersion  := "2.11.2"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
                 "SigFig1" at "http://maven.sigfig.com/libs",
                 "SigFig2" at "http://maven.sigfig.com/libs-release-local",
                 "SigFig3" at "http://maven.sigfig.com/libs-snapshot-local",
                 "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
                )

libraryDependencies ++= {
  val akkaV = "2.4-SNAPSHOT"
  val sprayV = "1.3.2"
  Seq(
    "io.spray"            %%  "spray-can"     % sprayV withSources,
    "io.spray"            %%  "spray-routing-shapeless2" % sprayV withSources,
    "io.spray"            %%  "spray-testkit" % sprayV  % "test",
    "io.spray"            %%  "spray-json"    % "1.3.1",
    "org.parboiled"       %%  "parboiled"     % "2.0.1",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-contrib"  % akkaV,
    "com.typesafe.akka"   %%  "akka-cluster-metrics" % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "org.specs2"          %%  "specs2-core"   % "2.3.11" % "test",
    "com.typesafe.akka"   %% "akka-persistence-experimental" % akkaV,
    "com.github.tototoshi" %% "scala-csv" % "1.1.2",
    "org.iq80.leveldb"     % "leveldb"          % "0.7",
    "org.fusesource.leveldbjni"   % "leveldbjni-linux64"   % "1.8"
  )
}
