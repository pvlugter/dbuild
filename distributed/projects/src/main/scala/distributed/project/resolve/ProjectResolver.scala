package distributed
package project
package resolve

import model._

/** Represents an interface that is used
 * to resolve a given project locally so that
 * we can run its build system.
 */
trait ProjectResolver {
  /** returns whether or not a resolver can resolve a particular
   * type of project.
   */
  def canResolve(config: BuildConfig): Boolean
  /** Resolves a remote project into the given local directory.
   * Returns a new repeatable Scm configuration that can be used
   * to retrieve the *exact* same code retrieved by this resolve call.
   */
  def resolve(config: BuildConfig, dir: java.io.File, log: logging.Logger): BuildConfig
}

/** Helper that uses all known project resolvers. */
class AggregateProjectResolver(resolvers: Seq[ProjectResolver]) extends ProjectResolver {
  def canResolve(config: BuildConfig): Boolean = 
    resolvers exists (_ canResolve config)
  def resolve(config: BuildConfig, dir: java.io.File, log: logging.Logger): BuildConfig = {
    resolvers find (_ canResolve config) match {
      case Some(r) => r.resolve(config, dir, log)
      case _       => sys.error("Could not find a resolver for: " + BuildConfig)
    }
  }
  
}
