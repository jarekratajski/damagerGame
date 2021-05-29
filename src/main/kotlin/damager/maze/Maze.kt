package damager.maze

import io.vavr.collection.List
import io.vavr.collection.HashSet
import io.vavr.collection.Seq


data class Maze(val rows: List<MazeRow>) {
    fun width() = if (rows.size() > 0) rows[0].cells.size() else 0
    fun height() = rows.size()

    fun replaceCell(x: Int, y: Int, cell: Cell) =
        copy(rows = rows.update(y,
            rows[y].replaceCell(x, cell)))

    fun replaceCell(coord: Coord, cell: Cell) = replaceCell(coord.x, coord.y, cell)

    fun getCell(coord: Coord) = this.rows[coord.y].cells[coord.x]

    fun getLocatedCell(coord: Coord) = LocatedCell(coord, getCell(coord))

    fun neighbours(coord: Coord) = run {
        val height = height()
        val width = width()
        HashSet.of(
            coord.copy(y = coord.y - 1).cap(width, height),
            coord.copy(y = coord.y + 1).cap(width, height),
            coord.copy(x = coord.x - 1).cap(width, height),
            coord.copy(x = coord.x + 1).cap(width, height),
        ).remove(coord)
    }

    fun openDoor(cell: LocatedCell, other: LocatedCell): Maze = run {
        val diffx = cell.coord.x - other.coord.x
        val diffy = cell.coord.y - other.coord.y

        fun modifyVerticalDoors(cell: Cell, diffx: Int) =
            when {
                diffx < 0 -> cell.copy(doorRight = true)
                diffx > 0 -> cell.copy(doorLeft = true)
                else -> cell
            }

        fun modifyHorizontalDoors(cell: Cell, diffy: Int) =
            when {
                diffy < 0 -> cell.copy(doorDown = true)
                diffy > 0 -> cell.copy(doorUp = true)
                else -> cell
            }

        val newCell = modifyHorizontalDoors(modifyVerticalDoors(cell.cell, diffx), diffy)
        val newOther = modifyHorizontalDoors(modifyVerticalDoors(other.cell, -diffx), -diffy)
        this.replaceCell(cell.coord, newCell).replaceCell(other.coord, newOther)
    }

    companion object {
        internal fun generateEmptyMaze(width: Int, height: Int): Maze = run {
            val initial = Maze(List.range(0, height).map { y ->
                MazeRow(List.range(0, width).map { x ->
                    Cell()
                })
            })
            openRandomWalls(Coord(0, 0), initial).maze
        }

        private fun openRandomWalls(cellCoord: Coord, maze: Maze) =
            damager.maze.generateMaze(cellCoord, damager.maze.MazeGeneration(maze, HashSet.empty()))
    }
}


data class MazeRow(val cells: List<Cell>) {
    fun replaceCell(x: Int, cell: Cell): MazeRow =
        this.copy(cells = cells.update(x, cell))
}


data class Coord(val x: Int, val y: Int) {
    fun cap(width: Int, height: Int) = copy(
        x = x.coerceIn(0, width-1),
        y = y.coerceIn(0, height-1)
    )

    override fun toString(): String = "($x,$y)"
    fun up() = this.copy(y = y - 1)
    fun down() = this.copy(y = y + 1)
    fun left() = this.copy(x = x - 1)
    fun right() = this.copy(x = x + 1)

}

data class Cell(
    val doorUp: Boolean = false,
    val doorDown: Boolean = false,
    val doorLeft: Boolean = false,
    var doorRight: Boolean = false
)

data class LocatedCell(val coord: Coord, val cell: Cell)


data class RenderedRows(val rows: Seq<String>) {
    operator fun plus(next: RenderedRows): RenderedRows =
        RenderedRows(this.rows.zipWith(next.rows) { a, b -> a + b })

}

fun Cell.render(): RenderedRows = run {
    val hDoor = { open: Boolean -> if (open) " " else "-" }
    val vDoor = { open: Boolean -> if (open) " " else "|" }
    RenderedRows(
        List.of("/-${hDoor(this.doorUp)}-\\", "|   |", "${vDoor(this.doorLeft)}   ${vDoor(this.doorRight)}", "|   |", "\\-${hDoor(this.doorDown)}-/")
    )
}


fun MazeRow.render(): RenderedRows =
    this.cells.foldLeft(RenderedRows(List.of("", "", "", "", ""))) { render, cell ->
        render + cell.render()
    }

fun Maze.render(): RenderedRows =
    this.rows.foldLeft(RenderedRows(List.empty())) { render, row ->
        RenderedRows(render.rows.appendAll(row.render().rows))
    }


fun RenderedRows.show() =
    rows.mkString("\n")

//generation

data class MazeGeneration(val maze: Maze, val visited: HashSet<Coord>) {
    fun visit(coord: Coord) = copy(visited = this.visited.add(coord))
}


fun generateMaze(cellCoord: Coord, mazeGeneration: MazeGeneration): MazeGeneration = run {
    val newVisitedMaze = mazeGeneration.visit(cellCoord)
    val neighbours = mazeGeneration.maze.neighbours(cellCoord)
    val unvisited = neighbours.removeAll(newVisitedMaze.visited).toList().shuffle()
    unvisited.fold(newVisitedMaze) { prevMaze, randomCellCoord ->

        val orignalCell = prevMaze.maze.getLocatedCell(cellCoord)
        val newCell = prevMaze.maze.getLocatedCell(randomCellCoord)
        if (!prevMaze.visited.contains(randomCellCoord)) {

            val newMaze = prevMaze.maze.openDoor(orignalCell, newCell)
            val newMazeGeneration = prevMaze.copy(maze = newMaze)
            generateMaze(randomCellCoord, newMazeGeneration)
        } else {
            prevMaze
        }
    }
}

fun main() {
    val maze = Maze.generateEmptyMaze(9, 9)
    val rend = maze.render()
    println(rend.show())


}
