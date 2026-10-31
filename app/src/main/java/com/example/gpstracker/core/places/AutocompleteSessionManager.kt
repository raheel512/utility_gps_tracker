package com.example.gpstracker.core.places

import javax.inject.Inject

/**
 * Testable autocomplete session lifecycle: one token per typing session,
 * reset after selection or when the screen closes.
 */
interface AutocompleteSessionManager {
    fun current(): AutocompleteSession
    fun reset()
    /** Monotonic generation for unit tests. */
    fun generation(): Int
}

class DefaultAutocompleteSessionManager @Inject constructor(
    private val placesRepository: PlacesRepository,
) : AutocompleteSessionManager {
    private var session: AutocompleteSession = placesRepository.newAutocompleteSession()
    private var generation: Int = 0

    override fun current(): AutocompleteSession = session

    override fun reset() {
        session = placesRepository.newAutocompleteSession()
        generation++
    }

    override fun generation(): Int = generation
}
