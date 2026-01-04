package com.bottlr.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bottlr.app.data.local.dao.BottleDao
import com.bottlr.app.data.local.dao.CocktailDao
import com.bottlr.app.data.local.entities.BottleEntity
import com.bottlr.app.data.local.entities.CocktailEntity

@Database(
    entities = [BottleEntity::class, CocktailEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class BottlrDatabase : RoomDatabase() {
    abstract fun bottleDao(): BottleDao
    abstract fun cocktailDao(): CocktailDao
}
