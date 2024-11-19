package com.example.brockapp.singleton

import com.example.brockapp.BuildConfig

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.createSupabaseClient

class MySupabase private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: SupabaseClient? = null

        fun getInstance(): SupabaseClient {
            synchronized(this) {
                if (INSTANCE == null) createInstance()
            }

            return INSTANCE as SupabaseClient
        }

        private fun createInstance() {
            INSTANCE = createSupabaseClient(
                BuildConfig.SUPABASE_BASE_URL,
                BuildConfig.SUPABASE_API_KEY
            ) {
                install(Postgrest)
            }
        }
    }
}