package com.tessera.puzzle.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tessera.puzzle.data.db.dao.BoardDao
import com.tessera.puzzle.data.db.dao.PuzzleDao
import com.tessera.puzzle.data.db.dao.StatsDao
import com.tessera.puzzle.data.db.entity.BestScoreEntity
import com.tessera.puzzle.data.db.entity.PuzzleEntity
import com.tessera.puzzle.data.db.entity.SavedBoardEntity

@Database(
    entities = [PuzzleEntity::class, SavedBoardEntity::class, BestScoreEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class TesseraDatabase : RoomDatabase() {
    abstract fun puzzleDao(): PuzzleDao
    abstract fun boardDao(): BoardDao
    abstract fun statsDao(): StatsDao

    companion object {
        const val NAME = "tessera.db"
    }
}
