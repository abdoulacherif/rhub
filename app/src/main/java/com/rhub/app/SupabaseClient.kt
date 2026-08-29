package com.rhub.app

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClientProvider {

    private const val SUPABASE_URL = "https://veihgdzozylysfzmvjvi.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InZlaWhnZHpvenlseXNmem12anZpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc5MzYyNDQsImV4cCI6MjEwMzUxMjI0NH0.OgmmACC1_nFVyEDh4dA1ek5N3JlQUJmiRmACq-w2yao"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(GoTrue)
        install(Postgrest)
    }
}