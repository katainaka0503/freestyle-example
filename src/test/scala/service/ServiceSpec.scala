package service

import cats.Eval
import cats.arrow.Strong
import cats.data.{ EitherT, IndexedStateT, State }
import cats.syntax.strong._
import cats.data.IndexedStateT._
import infrastructure.{ Cache, Database, Persistence }
import model.Goods
import org.scalatest.{ MustMatchers, WordSpec }
import freestyle._
import freestyle.implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ServiceSpec extends WordSpec with MustMatchers {

  type DBState = Map[Long, Goods]
  type CacheState = Map[Long, Goods]

  type TestModel[A] = EitherT[State[(DBState, CacheState), ?], Exception, A]

  implicit val databaseHandler: Database.Handler[TestModel] = (id: Long) =>
    EitherT.liftF(State.inspect[DBState, Option[Goods]](_.get(id))
      .transformS(_._1, (r, s) => r.copy(_1 = s)))

  implicit val cacheHandler: Cache.Handler[TestModel] = new Cache.Handler[TestModel] {
    override protected[this] def findById(id: Long): TestModel[Option[Goods]] =
      EitherT.liftF(State.inspect[CacheState, Option[Goods]](_.get(id))
        .transformS(_._2, (r, s) => r.copy(_2 = s)))

    override protected[this] def put(goods: Goods): TestModel[Unit] =
      EitherT.liftF(State.modify[CacheState](_.updated(goods.id, goods))
        .transformS(_._2, (r, s) => r.copy(_2 = s)))
  }

  "findById()" when {
    "db has a value and cache is empty" should {
      val goodsInDB = Goods(1, "apple", 500)
      val initialDBState: DBState = Map(goodsInDB.id -> goodsInDB)
      val initialCacheState: CacheState = Map.empty
      val initialState = (initialDBState, initialCacheState)

      val program: TestModel[Option[Goods]] = Service.findById[Persistence.Op](goodsInDB.id).interpret[EitherT[State[(DBState, CacheState), ?], Exception, ?]]

      "return value in db" in {
        val result = program.value.runA(initialState).value

        result mustBe Right(Some(goodsInDB))
      }

      "cache value" in {
        val (_, resultCacheState) = program.value.runS(initialState).value

        resultCacheState mustBe Map(goodsInDB.id -> goodsInDB)
      }

      "not change value in DB" in {
        val (resultDBState, _) = program.value.runS(initialState).value

        resultDBState mustBe initialDBState
      }
    }

    "cache has a value" should {
      val goodsInCache = Goods(1, "apple", 500)
      val initialDBState: DBState = Map.empty
      val initialCacheState: CacheState = Map(goodsInCache.id -> goodsInCache)
      val initialState = (initialDBState, initialCacheState)

      val program: TestModel[Option[Goods]] = Service.findById[Persistence.Op](goodsInCache.id).interpret[EitherT[State[(DBState, CacheState), ?], Exception, ?]]

      "return value in cache" in {
        val result = program.value.runA(initialState).value

        result mustBe Right(Some(goodsInCache))
      }
    }

    "cache and db is empty" should {
      val initialDBState: DBState = Map.empty
      val initialCacheState: CacheState = Map.empty
      val initialState = (initialDBState, initialCacheState)

      val id = 1

      val program: TestModel[Option[Goods]] = Service.findById[Persistence.Op](id).interpret[EitherT[State[(DBState, CacheState), ?], Exception, ?]]

      "return None" in {
        val result = program.value.runA(initialState).value

        result mustBe Right(None)
      }
    }
  }
}
