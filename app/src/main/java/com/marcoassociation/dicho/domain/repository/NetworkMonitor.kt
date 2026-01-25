package com.marcoassociation.dicho.domain.repository

interface NetworkMonitor {
    fun hasInternetConnection(): Boolean
}
