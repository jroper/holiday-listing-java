package com.example.search.impl

import com.example.reservation.api.ReservationService
import com.example.search.api.SearchService
import com.google.inject.AbstractModule
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport

class Module extends AbstractModule with ServiceGuiceSupport{
  override def configure(): Unit = {
    bindService(classOf[SearchService], classOf[SearchServiceImpl])
    bindClient(classOf[ReservationService])
  }
}
