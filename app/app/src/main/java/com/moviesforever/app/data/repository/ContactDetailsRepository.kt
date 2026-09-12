package com.moviesforever.app.data.repository

import com.moviesforever.app.data.model.ContactDetails
import kotlinx.coroutines.flow.Flow

interface ContactDetailsRepository {
    /**
     * Live view of the `settings/contact` doc, so an admin panel change is
     * reflected in the app immediately -- no new build needed.
     */
    fun observeContactDetails(): Flow<ContactDetails>
}
