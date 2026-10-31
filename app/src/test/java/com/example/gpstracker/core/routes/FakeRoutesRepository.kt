package com.example.gpstracker.core.routes

class FakeRoutesRepository(
    var result: RoutesResult = RoutesResult.Empty,
) : RoutesRepository {
    var calls = 0
    var lastRequest: RouteComputeRequest? = null

    override suspend fun computeRoute(request: RouteComputeRequest): RoutesResult {
        calls++
        lastRequest = request
        return result
    }
}
