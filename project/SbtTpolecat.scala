import org.typelevel.sbt.tpolecat.TpolecatPlugin.autoImport.tpolecatScalacOptions
import org.typelevel.scalacoptions.ScalacOptions
import sbt.Def

object SbtTpolecat {
  lazy val options: Seq[Def.Setting[?]] =
    Seq(
      tpolecatScalacOptions ++= Set(
        ScalacOptions.other("-no-indent"),
        ScalacOptions.other("-old-syntax"),
        ScalacOptions.other("-Wconf:src=src_managed/.*:silent")
      )
    )
}
