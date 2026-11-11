plugins { base }

for (id in listOf("sanrio", "marvel")) {
    val pack = tasks.register<Zip>("pack" + id.replaceFirstChar { it.uppercase() }) {
        from(id)
        archiveFileName.set("nookscreen-theme-$id.zip")
        destinationDirectory.set(layout.buildDirectory.dir("distributions"))
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
    tasks.assemble { dependsOn(pack) }
}
