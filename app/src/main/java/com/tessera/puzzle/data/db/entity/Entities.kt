package com.tessera.puzzle.data.db.entity

import androidx.room.Entity
import androidx.room.Index

/**
 * Room entity for a puzzle (bundled or custom).
 * imageKind = "drawable" | "file"; for drawable, imagePrimary = resName;
 * for file, imagePrimary = imagePath and imageThumb = thumbPath.
 */
@Entity(tableName = "puzzles")
data class PuzzleEntity(
    @androidx.room.PrimaryKey val id: String,
    val name: String,
    val source: String,
    val imageKind: String,
    val imagePrimary: String,
    val imageThumb: String?,
    val createdAt: Long,
    val deletable: Boolean,
)

@Entity(
    tableName = "saved_boards",
    primaryKeys = ["puzzleId", "difficulty"],
    indices = [Index("updatedAt")],
)
data class SavedBoardEntity(
    val puzzleId: String,
    val difficulty: String,
    val orderCsv: String,
    val selected: Int?,
    val moves: Int,
    val elapsedMillis: Long,
    val updatedAt: Long,
)

@Entity(
    tableName = "best_scores",
    primaryKeys = ["puzzleId", "difficulty"],
)
data class BestScoreEntity(
    val puzzleId: String,
    val difficulty: String,
    val bestTimeMillis: Long,
    val bestMoves: Int,
    val solvedCount: Int,
    val updatedAt: Long,
)
