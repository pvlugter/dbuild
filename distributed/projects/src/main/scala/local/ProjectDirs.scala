package local

import distributed.project.model.{BuildConfig, DistributedBuildConfig}
import java.io.File

// TODO - Locally configured area for projects
// With some kind of locking to prevent more than one 
// person from using the same directory at the same time
// Either that or spawn an actor for every local project
// and send the function to run on the actor?
object ProjectDirs {
  // TODO - Pull from config!
  val builddir = new File("target")
  
  
  def logDir = new File(builddir, "logs")
  
  // TODO - Check lock file or something...
  def useDirFor[A](build: BuildConfig)(f: File => A) = {
    val dir = new File( builddir, "projects")
    val projdir = new File(dir, hashing.sha1Sum(build))
    projdir.mkdirs()
    f(projdir)
  }
  
  
  def userRepoDirFor[A](build:DistributedBuildConfig)(f: File => A) = {
    val dir = new File(builddir, "repositories")
    val repodir = new File(dir, hashing.sha1Sum(build))
    repodir.mkdirs()
    f(repodir)
  }
}