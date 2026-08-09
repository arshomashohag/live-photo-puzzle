package com.tessera.puzzle.data.mapper

import com.tessera.puzzle.data.db.entity.BestScoreEntity
import com.tessera.puzzle.data.db.entity.PuzzleEntity
import com.tessera.puzzle.data.db.entity.SavedBoardEntity
import com.tessera.puzzle.domain.model.Difficulty
import com.tessera.puzzle.domain.model.persistence.BestScore
import com.tessera.puzzle.domain.model.persistence.ImageRef
import com.tessera.puzzle.domain.model.persistence.PuzzleRecord
import com.tessera.puzzle.domain.model.persistence.PuzzleSource
import com.tessera.puzzle.domain.model.persistence.SavedBoard

/**
 * Pure entity↔domain conversions. Kept side-effect-free so they are
 * round-trip property-testable (PBT-02).
 */
object EntityMappers {

    // --- Puzzle ---

    fun PuzzleRecord.toEntity(): PuzzleEntity {
        val (kind, primary, thumb) = when (val ref = imageRef) {
            is ImageRef.DrawableRef -> Triple("drawable", ref.resName, null)
            is ImageRef.FileRef -> Triple("file", ref.imagePath, ref.thumbPath)
        }
        return PuzzleEntity(
            id = id,
            name = name,
            source = source.name,
            imageKind = kind,
            imagePrimary = primary,
            imageThumb = thumb,
            createdAt = createdAt,
            deletable = deletable,
        )
    }

    fun PuzzleEntity.toDomain(): PuzzleRecord {
        val ref = when (imageKind) {
            "drawable" -> ImageRef.DrawableRef(imagePrimary)
            "file" -> ImageRef.FileRef(imagePrimary, imageThumb.orEmpty())
            else -> ImageRef.DrawableRef(imagePrimary)
        }
        return PuzzleRecord(
            id = id,
            name = name,
            source = runCatching { PuzzleSource.valueOf(source) }
                .getOrDefault(PuzzleSource.CUSTOM),
            imageRef = ref,
            createdAt = createdAt,
            deletable = deletable,
        )
    }

    // --- SavedBoard ---

    fun SavedBoard.toEntity(): SavedBoardEntity = SavedBoardEntity(
        puzzleId = puzzleId,
        difficulty = difficulty.name,
        orderCsv = order.joinToString(","),
        selected = selected,
        moves = moves,
        elapsedMillis = elapsedMillis,
        updatedAt = updatedAt,
    )

    /**
     * @return the domain board, or null if the stored data is malformed
     * (e.g. non-numeric CSV or unknown difficulty) — caller discards (BR-8).
     */
    fun SavedBoardEntity.toDomainOrNull(): SavedBoard? {
        val diff = runCatching { Difficulty.valueOf(difficulty) }.getOrNull()
            ?: return null
        val order = runCatching {
            if (orderCsv.isEmpty()) IntArray(0)
            else orderCsv.split(",").map { it.toInt() }.toIntArray()
        }.getOrNull() ?: return null
        return SavedBoard(
            puzzleId = puzzleId,
            difficulty = diff,
            order = order,
            selected = selected,
            moves = moves,
            elapsedMillis = elapsedMillis,
            updatedAt = updatedAt,
        )
    }

    // --- BestScore ---

    fun BestScore.toEntity(): BestScoreEntity = BestScoreEntity(
        puzzleId = puzzleId,
        difficulty = difficulty.name,
        bestTimeMillis = bestTimeMillis,
        bestMoves = bestMoves,
        solvedCount = solvedCount,
        updatedAt = updatedAt,
    )

    fun BestScoreEntity.toDomainOrNull(): BestScore? {
        val diff = runCatching { Difficulty.valueOf(difficulty) }.getOrNull()
            ?: return null
        return BestScore(
            puzzleId = puzzleId,
            difficulty = diff,
            bestTimeMillis = bestTimeMillis,
            bestMoves = bestMoves,
            solvedCount = solvedCount,
            updatedAt = updatedAt,
        )
    }
}
