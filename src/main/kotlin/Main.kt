import HexGridDirection.*
import PlayerAction.*
import kotlin.reflect.KClass

fun main() {
    val players = listOf(
        HumanPlayer(id = PlayerId(1), name = "Stuart"),
        HumanPlayer(id = PlayerId(2), name = "Rebecca")
    )

    val gameState = GameData.new(players.map(Player::id))

}

enum class Color {
    Red, Yellow, Green, Blue, Purple, Orange
}

class Tile(val anchorColor: Color, val tailColor: Color)

// Flat representation of hexagon
data class HexGridPoint(val x: Int, val y: Int, val z: Int) {
    fun step(direction: HexGridDirection): HexGridPoint {
        return this // todo: implement
    }
}

// Flat representation of hexagon
enum class HexGridDirection {
    N, NE, SE, S, SW, NW
}

data class TilePlacement(val anchorPoint: HexGridPoint, val direction: HexGridDirection) {
    val coveredGridPoints: List<HexGridPoint> = anchorPoint.let { listOf(it, it.step(direction)) }
}

data class Play(val tileIndexInHand: Int, val placement: TilePlacement)

interface Player {
    val id: PlayerId
    val name: String
    fun requestPlacement(hand: List<Tile>): Play
    fun offerWipe(hand: List<Tile>): Boolean
}

class HumanPlayer(override val id: PlayerId, override val name: String) : Player {
    override fun requestPlacement(hand: List<Tile>): Play {
        return Play(0, TilePlacement(HexGridPoint(0, 0, 0), NW))
    }

    override fun offerWipe(hand: List<Tile>): Boolean {
        return false
    }
}

sealed class GameResult {
    object Tie : GameResult()
    class Win(val winningPlayerName: String) : GameResult()
}

//class Board {
//
//    companion object {
//        private const val tileRadius = 5
//    }
//
//    val placedTiles: MutableList<Pair<TilePlacement, Tile>> = mutableListOf()
//
//    fun canPlaceTile(proposedPlacement: TilePlacement, isFirstRound: Boolean): Boolean {
//        // Check placement lies on the board:
//        val placementGridPoints = proposedPlacement.coveredGridPoints
//        if (placementGridPoints.any { abs(it.x) + abs(it.y) + abs(it.z) > 2 * tileRadius }) {
//            return false
//        }
//
//        // Check grid points are free:
//        val occupiedGridPoints = placedTiles
//            .map { (placement, _) -> placement }
//            .flatMap { it.coveredGridPoints }
//
//        if (proposedPlacement.coveredGridPoints.any { it in occupiedGridPoints }) {
//            return false
//        }
//
//        // If first round, check play is next to a unique starter space:
//        // todo
//
//        return true
//    }
//
//    fun placeTile(placement: TilePlacement, tile: Tile): Map<Color, Int> {
//        placedTiles += placement to tile
//        return emptyMap()
//    }
//}
//
//class Game(val players: List<Player>) {
//
//    lateinit var result: GameResult
//
//    private var roundNumber = 0
//    private var playState: PlayState = AwaitingTilePlacement(playerIndex = 0, pendingPlacements = 1)
//    private var board = Board()
//    private val bagTiles = mutableListOf<Tile>()
//    private var playerHands = players.associateBy(Player::id) { emptyList<Tile>() }.toMutableMap()
//    private var playerScores = players.associateBy(Player::id) { mapOf<Color, Int>() }.toMutableMap()
//
//    fun isNotOver() = !::result.isInitialized
//
//    fun advance() {
//        printBoard()
//
//        val initialState = playState
//        val playerIndex = initialState.playerIndex
//        val player = players[initialState.playerIndex]
//        val playerId = player.id
//        val playerHand = playerHands[playerId]?.toMutableList() ?: error("Hand not found for player $playerId.")
//        val playerInitialScores = playerScores[playerId] ?: error("Scores not found for player $playerId.")
//
//        when (initialState) {
//            is AwaitingTilePlacement -> {
//                val (tileIndexInHand, proposedPlacement) = player.requestPlacement(playerHand)
//
//                if (board.canPlaceTile(proposedPlacement = proposedPlacement, isFirstRound = roundNumber == 0)) {
//                    val playedTile = playerHand.removeAt(tileIndexInHand)
//                    val earnedPoints = board.placeTile(proposedPlacement, playedTile)
//
//                    val playerUpdatedScores = Color.values().associate { color ->
//                        color to playerInitialScores.getValue(color) + earnedPoints.getValue(color)
//                    }
//
//                    val extraPlaysEarned = Color.values().count { color ->
//                        playerUpdatedScores.getValue(color) == 18 && playerInitialScores.getValue(color) < 18
//                    }
//
//                    val remainingPlays = initialState.pendingPlacements - 1 + extraPlaysEarned
//
//                    when {
//                        remainingPlays > 0 -> {
//                            playState = AwaitingTilePlacement(playerIndex, pendingPlacements = remainingPlays)
//                        }
//
//                        canWipe(playerHand, emptyMap()) -> {
//                            playState = AwaitingWipeAnswer(playerIndex)
//                        }
//
//                        else -> {
//                            repeat(6 - playerHand.size) {
//                                val drawnTile = bagTiles.removeRandom()
//                                playerHand += drawnTile
//                            }
//
//                            val nextPlayerIndex = (playerIndex + 1).rem(players.size)
//                            if (nextPlayerIndex == 0) roundNumber++
//                            playState = AwaitingTilePlacement(nextPlayerIndex)
//                        }
//                    }
//                } else {
//                    println("Invalid tile placement.")
//                    return
//                }
//            }
//
//            is AwaitingWipeAnswer -> {
//                val didWipe = player.offerWipe(playerHand)
//
//                if (didWipe) {
//                    val tilesToDiscard = playerHand.toMutableList()
//                    playerHand.clear()
//
//                    repeat(6) {
//                        val drawnTile = bagTiles.removeRandom()
//                        playerHand += drawnTile
//                    }
//
//                    bagTiles.addAll(tilesToDiscard)
//                } else {
//                    repeat(6 - playerHand.size) {
//                        val drawnTile = bagTiles.removeRandom()
//                        playerHand += drawnTile
//                    }
//                }
//
//                val nextPlayerIndex = (playerIndex + 1).rem(players.size)
//                if (nextPlayerIndex == 0) roundNumber++
//                playState = AwaitingTilePlacement(nextPlayerIndex)
//            }
//        }
//    }
//
//    private fun canWipe(playerHand: List<Tile>, playerScores: Map<Color, Int>): Boolean {
//        val playerLowestScoringColors = playerScores
//            .entries
//            .groupBy { (_, score) -> score }
//            .minBy { (score, _) -> score }!!
//            .value
//            .map { (color, _) -> color }
//            .toSet()
//
//        val playerHandColors = playerHand.flatMap { tile -> setOf(tile.anchorColor, tile.tailColor) }.toSet()
//
//        return (playerHandColors intersect playerLowestScoringColors).isEmpty()
//    }
//
//    private fun printBoard() {
//
//    }
//
//}
//
//sealed class PlayState(val playerIndex: Int) {
//    class AwaitingTilePlacement(playerIndex: Int, val pendingPlacements: Int = 1) : PlayState(playerIndex)
//    class AwaitingWipeAnswer(playerIndex: Int) : PlayState(playerIndex)
//}
//
//
private fun <T> MutableList<T>.removeRandom(): T {
    val randomIndex = indices.random()
    return removeAt(randomIndex)
}

// experimental

@OptIn(ExperimentalStdlibApi::class)
private val allTiles: List<Tile> = buildList {
    for (firstColor in Color.values()) {
        for (secondColor in Color.values()) {
            val numberOfTiles = if (firstColor == secondColor) 5 else 6
            repeat(numberOfTiles) { add(Tile(firstColor, secondColor)) }
        }
    }
}

inline class PlayerId(val id: Int)

class GameState(
    val data: GameData,
    val requiredAction: RequiredAction
) {

    fun apply(playerId: PlayerId, action: PlayerAction): GameState {
        if (playerId != requiredAction.playerId) error("Action received from wrong player.")

        @Suppress("LiftReturnOrAssignment")
        when (action) {
            is PlacedTile -> {
                if (requiredAction !is RequiredAction.TilePlacement) error("Invalid action received.")
                return this
            }

            is DecidedRefresh -> {
                if (requiredAction !is RequiredAction.RefreshDecision) error("Invalid action received.")
                return this
            }
        }
    }

}

sealed class RequiredAction(val playerId: PlayerId) {
    class TilePlacement(playerId: PlayerId, pendingPlacements: Int = 1) : RequiredAction(playerId)
    class RefreshDecision(playerId: PlayerId) : RequiredAction(playerId)
}

data class GameData(
    val bagTiles: List<Tile>,
    val hands: Map<PlayerId, List<Tile>>,
    val placedTiles: List<Pair<Tile, TilePlacement>>,
    val playerScores: Map<PlayerId, Map<Color, Int>>,
    val turn: Int
) {

    companion object {
        fun new(playerIds: List<PlayerId>): GameData {
            return GameData(
                bagTiles = allTiles,
                hands = emptyMap(),
                placedTiles = emptyList(),
                playerScores = emptyMap(),
                turn = 0
            )
        }
    }
}

sealed class PlayerAction {
    class PlacedTile(val tile: Tile, val placement: TilePlacement) : PlayerAction()
    class DecidedRefresh(val doRefresh: Boolean) : PlayerAction()
}

sealed class GameEvent() {

}


/*
 maintain a list of game events
 for a given list, function : list<game event> -> game state(= game data + required action)
 apply action to game state and return a new list of game events?
 */