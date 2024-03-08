object Ci {

   // this is the version used for building snapshots
   // .GITHUB_RUN_NUMBER-snapshot will be appended
   private const val snapshotBase = "0.1.0"

   private val githubRunNumber = System.getenv("GITHUB_RUN_NUMBER")

   private val snapshotVersion = when (githubRunNumber) {
      null -> "$snapshotBase-LOCAL"
      else -> "$snapshotBase.$githubRunNumber-SNAPSHOT"
   }

   private val releaseVersion = System.getenv("RELEASE_VERSION")

   val isRelease = releaseVersion != null
   val version = releaseVersion ?: snapshotVersion

   /**
    * Property to flag the build as JVM only, can be used to run checks on local machine much faster.
    */
   const val JVM_ONLY = "jvmOnly"
}
