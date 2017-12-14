package service

import cats.implicits._
import cats.data.OptionT
import freestyle._
import freestyle.implicits._
import infrastructure.{ Database, Persistence }
import model.Goods

object Service {
  def findById[F[_]](id: Long)(implicit persistence: Persistence[F]): FreeS[F, Option[Goods]] = {
    import persistence._
    OptionT[FreeS[F, ?], Goods](cache.findById(id).freeS).orElse {
      for {
        goods <- OptionT[FreeS[F, ?], Goods](database.findById(id).freeS)
        _ <- OptionT.liftF(cache.put(goods).freeS)
      } yield goods
    }.value
  }
}
