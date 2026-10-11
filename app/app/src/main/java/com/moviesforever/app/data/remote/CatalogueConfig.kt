package com.moviesforever.app.data.remote

/** Where the catalogue JSON lives (Cloudflare R2, public URL). */
object CatalogueConfig {
    /**
     * Base URL of the bucket. Keep the trailing slash. This must be the same public URL the admin
     * panel uploads to (`VITE_R2_PUBLIC_URL`).
     * >>> CHANGE THIS if your file lives somewhere else. <<<
     */
    const val BASE_URL = "https://videos.moviesforever.online/"

    /** The single catalogue file. Must match `CATALOGUE_KEY` in the admin panel. */
    const val FILE_NAME = "movies.json"
}