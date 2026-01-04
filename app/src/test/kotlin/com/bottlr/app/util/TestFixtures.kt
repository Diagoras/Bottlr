package com.bottlr.app.util

import com.bottlr.app.data.local.entities.BottleEntity
import com.bottlr.app.data.local.entities.CocktailEntity

/**
 * Factory methods for creating test data.
 * Use these instead of constructing entities directly in tests.
 */
object TestFixtures {

    fun bottle(
        id: Long = 0L,
        name: String = "Test Whisky",
        distillery: String = "Test Distillery",
        type: String = "Single Malt",
        abv: Float? = 46.0f,
        age: Int? = 12,
        photoUri: String? = null,
        notes: String = "Smoky with hints of vanilla",
        region: String = "Islay",
        keywords: String = "peaty, smoky",
        rating: Float? = 8.5f,
        firestoreId: String? = null
    ) = BottleEntity(
        id = id,
        name = name,
        distillery = distillery,
        type = type,
        abv = abv,
        age = age,
        photoUri = photoUri,
        notes = notes,
        region = region,
        keywords = keywords,
        rating = rating,
        firestoreId = firestoreId
    )

    fun cocktail(
        id: Long = 0L,
        name: String = "Test Cocktail",
        base: String = "Whisky",
        mixer: String = "Ginger Ale",
        juice: String = "",
        liqueur: String = "",
        garnish: String = "Lime wedge",
        extra: String = "",
        photoUri: String? = null,
        notes: String = "Refreshing",
        keywords: String = "refreshing, simple",
        rating: Float? = 7.0f,
        firestoreId: String? = null
    ) = CocktailEntity(
        id = id,
        name = name,
        base = base,
        mixer = mixer,
        juice = juice,
        liqueur = liqueur,
        garnish = garnish,
        extra = extra,
        photoUri = photoUri,
        notes = notes,
        keywords = keywords,
        rating = rating,
        firestoreId = firestoreId
    )

    // Lists for bulk testing
    fun bottles(count: Int = 5) = (1..count).map { i ->
        bottle(
            id = i.toLong(),
            name = "Whisky $i",
            distillery = "Distillery $i"
        )
    }

    fun cocktails(count: Int = 5) = (1..count).map { i ->
        cocktail(
            id = i.toLong(),
            name = "Cocktail $i"
        )
    }
}
