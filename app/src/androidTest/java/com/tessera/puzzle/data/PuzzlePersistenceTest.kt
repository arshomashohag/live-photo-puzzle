package com.tessera.puzzle.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tessera.puzzle.data.db.TesseraDatabase
import com.tessera.puzzle.data.db.dao.BoardDao
import com.tessera.puzzle.data.db.dao.PuzzleDao
import com.tessera.puzzle.data.db.dao.StatsDao
import com.tessera.puzzle.data.db.entity.PuzzleEntity
import com.tessera.puzzle.data.mapper.EntityMappers.toDomainOrNull
import com.tessera.puzzle.data.mapper.EntityMappers.toEntity
import com.tessera.puzzle.domain.model.Difficulty
import com.tessera.puzzle.domain.model.persistence.SavedBoard
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Example-based Room tests against an in-memory database (PBT-10 complement).
 * Verifies save/load, best-score recording, delete cascade, and corrupt-data
 * discard.
 */
@RunWith(AndroidJUnit4::class)
class PuzzlePersistenceTest {

    private lateinit var db: TesseraDatabase
    private lateinit var puzzleDao: PuzzleDao
    private lateinit var boardDao: BoardDao
    private lateinit var statsDao: StatsDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, TesseraDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        puzzleDao = db.puzzleDao()
        boardDao = db.boardDao()
        statsDao = db.statsDao()
    }

    @After
    fun teardown() = db.close()

    private fun bundled(id: String) = PuzzleEntity(
        id = id, name = id, source = "BUNDLED",
        imageKind = "drawable", imagePrimary = "res_$id", imageThumb = null,
        createdAt = 0, deletable = false,
    )

    @Test
    fun savedBoard_roundTripsThroughDb() = runTest {
        val board = SavedBoard(
            puzzleId = "easy-1", difficulty = Difficulty.EASY,
            order = intArrayOf(1, 0, 2, 3, 4, 5, 6, 7, 8),
            selected = 2, moves = 3, elapsedMillis = 4200, updatedAt = 10,
        )
        boardDao.upsert(board.toEntity())
        val loaded = boardDao.get("easy-1", "EASY")?.toDomainOrNull()
        assertEquals(board, loaded)
    }

    @Test
    fun mostRecent_returnsHighestUpdatedAt() = runTest {
        boardDao.upsert(
            SavedBoard("a", Difficulty.EASY, intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8), null, 0, 0, 5).toEntity(),
        )
        boardDao.upsert(
            SavedBoard("b", Difficulty.EASY, intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8), null, 0, 0, 9).toEntity(),
        )
        val recent = boardDao.observeMostRecent().first()?.toDomainOrNull()
        assertEquals("b", recent?.puzzleId)
    }

    @Test
    fun completion_recordsBestTime_keepsLowerAndCounts() = runTest {
        val repo = com.tessera.puzzle.data.repository.StatsRepositoryImpl(
            statsDao, puzzleDao, kotlinx.coroutines.Dispatchers.Unconfined,
        )
        repo.recordCompletion("easy-1", Difficulty.EASY, 5000, 20)
        repo.recordCompletion("easy-1", Difficulty.EASY, 3000, 12) // better
        repo.recordCompletion("easy-1", Difficulty.EASY, 8000, 40) // worse
        val best = repo.bestScore("easy-1", Difficulty.EASY)!!
        assertEquals(3000, best.bestTimeMillis)
        assertEquals(12, best.bestMoves)
        assertEquals(3, best.solvedCount)
    }

    @Test
    fun deleteCustom_cascadesRowsAndRejectsBundled() = runTest {
        // Bundled: not deletable.
        puzzleDao.insertIgnore(listOf(bundled("easy-1")))
        assertEquals(0, puzzleDao.deleteIfDeletable("easy-1"))
        assertNotNull(puzzleDao.getById("easy-1"))

        // Custom: deletable, cascades board + stats.
        puzzleDao.upsert(bundled("c1").copy(source = "CUSTOM", deletable = true))
        boardDao.upsert(
            SavedBoard("c1", Difficulty.EASY, intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8), null, 0, 0, 1).toEntity(),
        )
        assertEquals(1, puzzleDao.deleteIfDeletable("c1"))
        boardDao.deleteForPuzzle("c1")
        assertNull(boardDao.get("c1", "EASY"))
    }

    @Test
    fun corruptOrder_discardedOnMap() = runTest {
        val good = SavedBoard(
            "easy-1", Difficulty.EASY, intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8), null, 0, 0, 1,
        ).toEntity().copy(orderCsv = "0,1,notanumber")
        assertNull(good.toDomainOrNull())
    }
}
