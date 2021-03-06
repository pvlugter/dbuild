package com.typesafe.dbuild.repo

import org.specs2.mutable.Specification
import com.typesafe.dbuild.model._
import java.io.File
import com.typesafe.dbuild.model.SeqDepsModifiers.OptToSeqDM

object PomHelperSpec extends Specification {
  
  def makeBuildArts: (RepeatableDBuildConfig, BuildArtifactsIn) = {
    val build = RepeatableDBuildConfig(
            Seq(ProjectConfigAndExtracted(
                config = ProjectBuildConfig("", "", "", None, None, None, None, None, None, Some(new Space("default")), None),
                extracted = ExtractedBuildMeta(
                  projInfo = Seq(ProjMeta("", Seq(
                    Project(
                      name = "scala-arm",
                      organization = "com.jsuereth",
                      artifacts = Seq(ProjectRef("scala-arm", "com.jsuereth")),
                      dependencies = Seq(ProjectRef("scala-library", "org.scala-lang"))
                    )
                  )))
                )
            ))
          )
      val arts = BuildArtifactsIn(Seq(
        ArtifactLocation(ProjectRef("scala-arm", "com.jsuereth"), "1.2", "_2.10", None)    
      ), "default", new File("."))
      
    (build,arts)
  }
  
  "A PomHelper" should {
    "create pom models" in {
      val (build,arts) = makeBuildArts
      val poms = PomHelper.makePoms(build, arts)
      
      poms must haveSize(1)
      val pom = poms.head
      pom.getArtifactId must equalTo("scala-arm")
      pom.getDependencies.size must equalTo(1)
    }
    "create pom strings" in {
      val (build,arts) = makeBuildArts
      val poms = PomHelper.makePomStrings(build, arts)
      
      poms must haveSize(1)
      val pom = poms.head
      println(pom)
      pom must contain("<artifactId>scala-arm</artifactId>")
      pom must contain("<dependencies>")
      pom must contain("<dependency>")
    }
  }
}
