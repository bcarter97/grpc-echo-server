addCommandAlias("checkFix", "scalafixAll --check OrganizeImports; scalafixAll --check")
addCommandAlias("runFix", "scalafixAll OrganizeImports; scalafixAll")
addCommandAlias("checkFmt", "scalafmtCheckAll; scalafmtSbtCheck")
addCommandAlias("runFmt", "scalafmtAll; scalafmtSbt")
addCommandAlias("checkLint", "checkFmt; checkFix")
addCommandAlias("runLint", "runFmt; runFix")

addCommandAlias("ciBuild", "runLint; test")
addCommandAlias("ciRelease", "Docker/publish")
