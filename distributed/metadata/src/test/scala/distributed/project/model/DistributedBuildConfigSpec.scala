package distributed
package project
package model

import Utils._
import org.specs2.mutable.Specification
import project.model._
import Utils.{writeValue,readValue}

object DistributedBuildConfigSpec extends Specification {
  "DistributedBuildConfig" should {
    "parse project with defaults" in {

      readValue[DistributedBuildConfig]("""{
  projects = [{
          name = "p1"
          uri = "uri"
    }]
}""") must equalTo(DistributedBuildConfig(
      Seq(ProjectBuildConfig(
          name = "p1",
          uri = "uri",
          system = "sbt",
          setVersion = None,
          notifications = Seq.empty,
          extra = None
      )), None, None
    ))
    }
    "parse project" in {
      
      
      readValue[DistributedBuildConfig](
"""{
  projects = [{
          name = "p1"
          uri = "uri"
          system = "sbt"
          set-version = "3.9.43"
          extra = { directory = "ZOMG" }
    }]
    deploy = [{
          uri = "file://localhost:8088/some/path"
          credentials = "/credentials/file"
          projects = ["p1","p2",{
    from:"aaa"
    publish:["a","b"]
    }]
    }]
}""") must equalTo(DistributedBuildConfig(
      Seq(ProjectBuildConfig(
          name = "p1",
          uri = "uri",
          system = "sbt",
          setVersion = Some("3.9.43"),
          notifications = Seq.empty,
          extra = readValue[Option[SbtExtraConfig]]("{directory = ZOMG}")
      )),Some(Seq(DeployOptions("file://localhost:8088/some/path",Some("/credentials/file"),
       Seq(SelectorProject("p1"),SelectorProject("p2"),SelectorSubProjects(SubProjects("aaa",Seq("a","b")))),None))), None
    ))
    }
  }
}