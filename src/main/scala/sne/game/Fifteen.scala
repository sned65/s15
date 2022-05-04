package sne.game

import org.scalajs.dom
import org.scalajs.dom._

import scala.scalajs.js.annotation.JSExportTopLevel
import scalatags.JsDom._
import scalatags.JsDom.all._
import sne.game.Board.indexToId


object Fifteen {
    
    val board = new Board
    
    private def el(id: String): Element = document.getElementById(id)
    
    private val t: HTMLTableElement = el("t").asInstanceOf[HTMLTableElement]
    private val cellList: HTMLCollection[Element] = t.getElementsByClassName("cell")
    // Element int id -> Element 
    private val cells: Map[Int, Element] = cellList.map(c => (c.id.substring(1).toInt, c)).toMap
    
    private def cellValue(id: Int): HTMLSpanElement =
        el(s"cv$id").asInstanceOf[HTMLSpanElement]

    def *(id: Int): HTMLSpanElement = cellValue(id)

    def &(id: Int): HTMLDivElement = {
        el(s"c$id").asInstanceOf[HTMLDivElement]
    }
    
    def alert(s: String): Unit = dom.window.alert(s)

    @JSExportTopLevel("main")
    def main(args: Array[String]): Unit = {
        initBoard()
    }

    /**
     * Create spans for values and initialize DnD.
     */
    def initBoard(): Unit = {
        
        for (i <- 0 until 15) {
            val cvId = indexToId(i)
            //cells(id).innerHTML = s"<span class=\"g15-value\" id=\"cv$id\">${board(i)}</span>"
            val sp = span(
                id := s"cv$cvId",
                `class` := "g15-value",
                draggable := false,
                opacity := "0.35",
                s"${board(i)}"
            ).render
            cells(cvId).appendChild(sp)
        }
        
        val emptyIdx = indexToId(board.emptyIndex)
        cells(emptyIdx).innerHTML = s"<span class=\"g15-value\" id=\"cv$emptyIdx\" draggable=\"false\"></span>"

        enableDrag()

        val emptyCell = cells(emptyIdx)
        enableDrop(emptyCell)
        
        initKeyboard()
        
        val newGameBtn = el("new").asInstanceOf[HTMLButtonElement]
        newGameBtn.addEventListener("click", (_: Event) => newGame())
    }
    
    def newGame(): Unit = {
        val oldDraggableFields = board.draggableFields
        board.reset()

        for (i <- 0 until 15) {
            val id = Board.indexToId(i)
            val valueElem = *(id)
            valueElem.textContent = board(i).toString
        }

        val empty_id = Board.indexToId(board.emptyIndex)
        val emptyElem = *(empty_id)
        emptyElem.textContent = null
        val divEmpty = &(empty_id)

        disableDrag(oldDraggableFields)
        disableDrop(divEmpty)
        enableDrag()
        enableDrop(&(indexToId(board.emptyIndex)))
        
        clearInfo()
    }
    
    def enableDrag(): Unit = {
        for (id <- board.draggableFields) {
            val e = *(id)
            e.style.opacity  = "1"
            e.style.cursor = "pointer"
            e.draggable = true
            e.addEventListener("dragstart", (ev: DragEvent) => {
                val dragging = ev.target.asInstanceOf[HTMLSpanElement]
                ev.dataTransfer.setData("text/plain", dragging.id)
                //alert(s"Drag starts: ${dragging.textContent}")
            })
        }
    }
    
    def enableDrop(cell: Element): Unit = {
        //println(s"enableDrop(${cell.id})")
        cell.addEventListener("dragover", (ev: DragEvent) => { ev.preventDefault() })
        cell.addEventListener("dragenter", (ev: DragEvent) => { ev.preventDefault() })
        cell.addEventListener("drop", (ev: DragEvent) => {
            ev.preventDefault()
            val from = ev.dataTransfer.getData("text/plain")
            //println(s"Drop: from $from")
            doMove(from.substring(2).toInt)
        })
    }
    
    def disableDrop(cell: Element): Unit = {
        //println(s"disableDrop(${cell.id})")
        // Note:
        // Scala.js always generates a lambda-function for addEventListener
        // (even if an explicit method is written).
        // As a result, the handler can not be removed by removeEventListener.
        val cellClone = cell.cloneNode(true)
        cell.parentNode.replaceChild(cellClone, cell)
    }
    
    def disableDrag(draggableIds: Vector[Int]): Unit = {
        //for (i <- 0 to 15) {
        for (id <- draggableIds) {
            //val id = indexToId(i)
            val e = *(id)
            e.style.opacity = "0.35"
            e.style.cursor = null
            e.draggable = false
            //val eClone = e.cloneNode(true)
            //e.parentNode.replaceChild(eClone, e)
        }
    }
    
    def doMove(id: Int): Unit = {
        //val divFrom = &(id)
        val spanFrom = *(id)

        val emptyId = Board.indexToId(board.emptyIndex)
        val divEmpty = &(emptyId)
        val spanEmpty = *(emptyId)

        val oldDraggableFields = board.draggableFields

        val idxFrom = Board.idToIndex(id)
        val value = board(idxFrom)
        board(board.emptyIndex) = value
        board.setEmpty(idxFrom)
        
        spanEmpty.textContent = spanFrom.textContent
        spanFrom.textContent = null

        disableDrag(oldDraggableFields)
        disableDrop(divEmpty)
        enableDrag()
        enableDrop(&(indexToId(board.emptyIndex)))

        board.checkSuccess match {
            case Board.RESULT_SUCCESS => showInfo("Победа! &#x1F60A;", "green")
            case Board.RESULT_IMPOSSIBLE => showInfo("Однако не повезло &#x1F61E;", "red")
            case _ =>
        }
    }
    
    def clearInfo(): Unit = {
        val result = el("result").asInstanceOf[HTMLDivElement]
        result.removeChild(el("info"))
    }

    def showInfo(msg: String, colour: String): Node = {
        val result = el("result").asInstanceOf[HTMLDivElement]

        val msgElem = span(
            id := "info",
            color := colour,
            fontSize := "20px",
            fontWeight := "bold",
            p(scalatags.JsDom.all.raw(msg))
        )

        result.appendChild(msgElem.render)
    }

    def initKeyboard(): Unit = {
        document.body.addEventListener("keyup", (event: KeyboardEvent) => {
            val empty_id = Board.indexToId(board.emptyIndex)
            val (empty_x, empty_y) = Board.idToXY(empty_id)

            val (x, y) =
                event.key match {
                    case "ArrowUp" =>
                        (empty_x, empty_y + 1)
                    case "ArrowDown" =>
                        (empty_x, empty_y - 1)
                    case "ArrowRight" =>
                        (empty_x - 1, empty_y)
                    case "ArrowLeft" =>
                        (empty_x + 1, empty_y)
                    case _ =>
                        (0, 0)
                }

            val id = Board.xyToId(x, y)
            //println(s"empty_id = $empty_id, x = $x, y = $y")
            //println(s"draggable=${board.draggableFields}; move=$id")
            if (board.draggableFields.contains(id)) {
                doMove(id)
            }
        })
    }
}
