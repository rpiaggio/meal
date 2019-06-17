package com.rpiaggio

package object meal {
  type EntryData = List[String]

  protected[meal] val PAGES_REQUEST = 10

  protected[meal] val MAX_REDIR_COUNT = 20
}
