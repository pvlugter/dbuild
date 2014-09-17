package com.typesafe.dbuild.project

import com.typesafe.dbuild.model._
import com.typesafe.dbuild.logging.Logger
import java.io.File
import sbt.Path._

case class BuildData(log: Logger, debug: Boolean)

/** An abstraction representing a "hook" into the builder that understands how
 * to extract dependencies and run builds for a  given type of "build system".
 * 
 * This allows customized build execution if needed.
 * 
 * We use a type parameter for Extractor and LocalBuildRunner, in order to be able to keep
 * this trait more abstract, in the 'd-core' subproject (without tying it to the actual classes). 
 */
abstract class BuildSystem[Extractor, LocalBuildRunner] {
  /** The name of the build system used in configuration. */
  def name: String  
  /** Extract build dependencies of a given project that uses this build system.
   *  While extracting, it also xpands the build options (the 'extra' field) so
   *  that the defaults that apply for this build system are taken into account.
   *  
   * @param config    The project configuration.
   * @param dir       A local checkout of the project.
   * @param extractor The extractor currently in use (for nested calls)
   * @param log       The logger to send output to for this build.
   * @param debug     If true, print more debugging information
   * 
   * @return The dependencies the local project requires.
   */
  def extractDependencies(config: ExtractionConfig, dir: java.io.File, extractor: Extractor, log: Logger, debug: Boolean): ExtractedBuildMeta
  /**
   * Runs this build system on a project.
   * 
   * @param project       The build configuration for this project
   * @param dir           The local checkout of the project to run.
   * @param info          The locally hosted dependencies and output repository for a build.
   * @param localBuildRunner  The localBuildRunner currently in use (for nested calls)
   * @param log           The logger to send output into.
   * @param debug     If true, print more debugging information
   * 
   * @return The BuildArtifacts generated by this build.
   */
  def runBuild(project: RepeatableProjectBuild, dir: java.io.File, info: BuildInput, localBuildRunner: LocalBuildRunner,
      buildData:BuildData): BuildArtifactsOut
  
  /**
   * Before extractDependencies() is called, the ProjectBuildConfig must be resolved: this entails
   * fetching the code from the specified URI, and replacing the URI (which may contain moving
   * references to a branch tip, or a moving tag) with a fixed commit, or other fixed reference
   * to a precise snapshot.

   * For each URI, the actual resolution is demanded to an instance of ProjectResolver (actually
   * an AggregateProjectResolver), which can be found via the passed Extractor.
   * At the end, the rewritten ProjectBuildConfig, now repeatable, is returned.
   * 
   * The default implementation (BuildSystemCore) simply resolves the main URI. In case the build system 
   * supports nested projects, the implementation should be overridden so that, in addition,
   * all of the nested projects are recursively resolved in turn.
   * 
   * @param config    The project configuration.
   * @param opts      The associated build options
   * @param dir       The directory that will receive the code checkout
   * @param extractor The extractor currently in use (for nested calls)
   * @param log       The logger to send output to for this build.
   * 
   * @return The updated configuration with resolved URIs, now in a repeatable form.
   * 
   */
  def resolve(config: ProjectBuildConfig, dir: java.io.File, extractor: Extractor, log: Logger): ProjectBuildConfig
  /**
   * Expand the "extra" record, generating another "extra" in which all optional values have been replaced by
   * concrete values.
   * If the initial extra was None, expand to a suitable default record; use the values from the supplied defaults
   * if appropriate.
   * If the initial extra contained undefiled fields, use the vaules the supplied defaults instead.
   */
  def expandExtra(extra: Option[ExtraConfig], systems: Seq[BuildSystem[Extractor, LocalBuildRunner]], defaults: ExtraOptions): ExtraType
  type ExtraType <: ExtraConfig
}
object BuildSystem {
  /**
   * Find the build system for this name (if any)
   */
  def forName[A,B](systemName: String, systems: Seq[BuildSystem[A, B]]) = {
    systems find (_.name == systemName) getOrElse sys.error("Could not find a build system for " + systemName)
  }

  /**
   * Expand all the project descriptions in the build configuration. That entails replacing the
   * defaults into the corresponding values of the project, if not overridden, and expanding the
   * "extra" component of each with suitable defaults.
   * After this substitution is complete for all projects, the BuildOptions are no longer used.
   */
  def expandDBuildConfig[A, B](build: Seq[DBuildConfig], systems: Seq[BuildSystem[A, B]]) = {
    def expandProject(config: ProjectBuildConfig, defaults: BuildOptions): ProjectBuildConfig = {
      val system = BuildSystem.forName(config.system, systems)
      config.expandDefaults(defaults).copy(extra = Some(system.expandExtra(config.extra, systems, defaults)))
    }
    build.flatMap {b => b.projects.map { expandProject(_, b) }}
  }
}