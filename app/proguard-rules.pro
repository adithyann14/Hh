# Keep the tile service and root helper explicitly (also auto-kept since the
# service is referenced from the manifest, but explicit for clarity).
-keep class com.example.bypasstile.BypassTileService { *; }
-keep class com.example.bypasstile.RootShell { *; }

-dontwarn org.jetbrains.annotations.**
