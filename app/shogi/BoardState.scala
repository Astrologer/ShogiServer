package shogi

object BoardState {
  //val board = Array.fill[Option[String]](10, 10) { None }
  def fromSFEN(sfen: String): BoardState = {
    val parts = sfen.split(" ")
    val board = parseBoard(parts(0))
    val isBlackMove = parts(1) == "b"
    val hands: Seq[String] = if (parts.length == 3) parts(2).split("").toSeq else Seq()

    new BoardState(board, isBlackMove, hands)
  }

  def parseBoard(sfen: String): Array[Array[Option[String]]] = {
    def isNum(s: String) = s forall Character.isDigit
    def getPiecesWithCol(row: String): Array[(String, Int)] = {
      val items = row.reverse.split("(?!\\+)").reverse
      val ind = items.map(x => if (isNum(x)) x.toInt else 1).scanLeft(1)(_ + _)
      items
        .zip(ind)
        .filter(i => !isNum(i._1))
    }

    val board = Array.fill[Option[String]](9, 9) { None }

    sfen
      .split(" ")(0)
      .split("/")
      .zipWithIndex
      .foreach{ case(x, row) =>
        getPiecesWithCol(x)
          .foreach{ case(p, col) => board(row)(9 - col) = Some(p) }
      }

    board
  }
}

class BoardState(board: Array[Array[Option[String]]],
                isBlackMove: Boolean,
                hands: Seq[String]) {

  def isBlack(piece: String): Boolean = piece.forall(!_.isLower)
  def getBoard = board
  // TODO 
  // - check promotion restriction
  // - check movable piece name
  def noCollisions(m: Move): Boolean = {
    val dst = board(m.toRow - 1)(m.toCol - 1)
    val src = if (m.isDrop) None else  board(m.fromRow - 1)(m.fromCol - 1)
    (m.isCapture, m.isDrop) match {
      // drop
      case (false, true) => dst.isEmpty && hands.contains(m.piece) && isBlack(m.piece) == isBlackMove
      // capture
      case (true, false) => src.nonEmpty && isBlack(src.get) == isBlackMove &&
                            dst.nonEmpty && isBlack(dst.get) != isBlackMove
      //regular move
      case (false, false) => src.nonEmpty && isBlack(src.get) == isBlackMove &&
                             dst.isEmpty
      case _ => throw new Exception("oops")
    }
  }

  def isLegalMove(move: String): Boolean = {
    val m = Move(move, isBlackMove)
    m.isLigal && noCollisions(m)
  }

  def makeMove(m: Move) {
    (m.isCapture, m.isDrop) match {
      // drop
      case (false, true) => throw new Exception("oops, drop")
      // capture
      case (true, false) => throw new Exception("oops, capture")
      //regular move
      case (false, false) =>
        board(m.toRow - 1)(m.toCol - 1) = board(m.fromRow - 1)(m.fromCol - 1)
        board(m.fromRow - 1)(m.fromCol - 1) = None

      case _ => throw new Exception("oops")
    }

  }

  def toSFEN(move: String): String = {
    makeMove(Move(move, isBlackMove))

    val sfenBoard = board
      .map(_
        .map(_ match {
          case None => "1"
          case Some(p) => p}
        )
        .reverse
        .foldLeft[(String, Int)](("", 0)) {
          case ((s, n), p) =>
            if (p == "1") (s, n + 1)
            else (s + n.toString + p, 0)
        }
      )
      .map(i => s"${i._1}${i._2}")
      .mkString("/")
      .filter(_ != '0')
    val sfenMove = if (isBlackMove) "w" else "b"
    val sfenHands = hands.sorted.mkString

    s"${sfenBoard} ${sfenMove} ${sfenHands}"
  }
}
