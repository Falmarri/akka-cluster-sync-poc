package com.sigfig.sync

case class Sync(portfolioUserId: Int)
case class GetSyncStatus(portfolioUserId: Int)
case class SyncStatus(portfolioUserId: Int, result: String)
case class SyncResult(portfolioUserId: Int, payload: Any)
final case class EntryEnvelope(id: Long, payload: Any)