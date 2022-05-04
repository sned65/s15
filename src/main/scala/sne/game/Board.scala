package sne.game

import scala.util.Random

class Board {

    private val _board: Array[Int] = Array.fill(16)(0)
    private var _emptyIndex = 15

    def setEmpty(idx: Int): Unit = {
        _emptyIndex = idx
        _board(idx) = 0
    }

    def emptyIndex: Int = _emptyIndex
    def apply(idx: Int): Int = _board(idx)
    def update(idx: Int, v: Int): Unit = _board(idx) = v


    /**
     * Fills the board with random numbers.
     */
    def reset(): Unit = {
        val rnd: Vector[Int] = Board.random15()

        for (i <- 0 until 15) {
            _board(i) = rnd(i)
        }

        setEmpty(15)
    }
    
    reset()

    /**
     * @return Vector of draggable ids
     */
    def draggableFields: Vector[Int] = {
        val draggable = collection.mutable.ArrayBuffer.empty[Int]

        def good(x: Int, y: Int): Boolean = x >= 1 && x <= 4 && y >= 1 && y <= 4

        val empty = Board.indexToId(emptyIndex)
        val empty_x = empty / 10
        val empty_y = empty % 10
        var x = 0
        var y = 0

        x = empty_x + 1
        y = empty_y
        if (good(x, y)) draggable.addOne(x * 10 + y)

        x = empty_x - 1
        y = empty_y
        if (good(x, y)) draggable.addOne(x * 10 + y)

        x = empty_x
        y = empty_y + 1
        if (good(x, y)) draggable.addOne(x * 10 + y)

        x = empty_x
        y = empty_y - 1
        if (good(x, y)) draggable.addOne(x * 10 + y)

        draggable.toVector
    }

    def checkSuccess: Int = {
        if (_board sameElements Board.success) Board.RESULT_SUCCESS
        else if (_board sameElements Board.impossible) Board.RESULT_IMPOSSIBLE
        else Board.RESULT_NOT_FINISHED
    }
}

object Board {
    
    val RESULT_SUCCESS = 0    // all cells are in place
    val RESULT_IMPOSSIBLE = 1 // all cells are in place except 14 and 15
    val RESULT_NOT_FINISHED = 2
    val success = Vector(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,0)
    val impossible = Vector(1,2,3,4,5,6,7,8,9,10,11,12,13,15,14,0)

    /**
     * @return 15 unique random integers from 1 to 15
     */
    def random15(): Vector[Int] = {
        val rnd =
            for (i <- 0 until 15) yield {
                Random.nextInt() -> (i+1)
            }

        rnd.sortBy(_._1).map(_._2).toVector
    }

    def indexToId(idx: Int): Int = {
        require(idx >= 0 && idx < 16)
        (idx / 4 + 1) * 10 + (idx % 4 + 1)
    }

    def idToIndex(id: Int): Int = {
        (id / 10 - 1) * 4 + id % 10 - 1
    }
    
    def idToXY(id: Int): (Int, Int) = (id % 10, id / 10)
    
    def xyToId(x: Int, y: Int): Int = y * 10 + x
}
