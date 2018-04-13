import com.lightbend.lagom.sbt.{Internal, LagomImport}

organization in ThisBuild := "com.example"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.4"

lagomCassandraEnabled in ThisBuild := false

val lombok = "org.projectlombok" % "lombok" % "1.16.12"

lazy val `holiday-listing` = (project in file("."))
  .aggregate(`reservation-api`, `reservation-impl`, `search-api`, `search-impl`, `web-gateway`)

lazy val `reservation-api` = (project in file("reservation-api"))
  .settings(
    libraryDependencies ++= Seq(
      lombok,
      lagomJavadslApi
    )
  )

lazy val `reservation-impl` = (project in file("reservation-impl"))
  .enablePlugins(LagomJava)
  .settings(
    libraryDependencies ++= Seq(
      lombok,
      lagomJavadslPersistenceJpa,
      "org.hibernate" % "hibernate-core" % "5.2.16.Final",
      "org.hibernate" % "hibernate-jpamodelgen" % "5.2.16.Final",
      "com.h2database" % "h2" % "1.4.196",
      lagomJavadslKafkaBroker
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`reservation-api`, `search-api`)

lazy val `search-api` = (project in file("search-api"))
  .settings(
    libraryDependencies ++= Seq(
      lombok,
      lagomJavadslApi
    )
  ).dependsOn(`reservation-api`)

lazy val `search-impl` = (project in file("search-impl"))
  .enablePlugins(LagomJava)
  .settings(
    libraryDependencies ++= Seq(
      lombok,
      lagomJavadslKafkaClient
    )
  )
  .dependsOn(`search-api`, `reservation-api`)

lazy val `web-gateway` = (project in file("web-gateway"))
  .enablePlugins(PlayScala && LagomPlay)
  .disablePlugins(PlayLayoutPlugin)
  .dependsOn(`reservation-api`, `search-api`)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslClient,
      LagomImport.component("lagom-javadsl-play-integration") % Internal.Configs.DevRuntime,
      lombok,
      "org.webjars" % "foundation" % "6.2.3",
      "org.webjars" % "foundation-icon-fonts" % "d596a3cfb3"
    ),
    lagomWatchDirectories ++= (sourceDirectories in (Compile, TwirlKeys.compileTemplates)).value
  )
