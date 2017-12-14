package infrastructure

import freestyle._

import model.Goods

@free trait Database {
  def findById(id: Long): FS[Option[Goods]]
}

@free trait Cache{
  def findById(id: Long): FS[Option[Goods]]
  def put(goods: Goods): FS[Unit]
}

@module trait Persistence {
  val database: Database
  val cache: Cache
}
